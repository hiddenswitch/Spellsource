package com.hiddenswitch.framework;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.GameUsersDao;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.GamesDao;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.MatchmakingQueuesDao;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.GameUsers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.Games;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets;
import com.hiddenswitch.framework.schema.spellsource.tables.records.GameUsersRecord;
import com.hiddenswitch.spellsource.rpc.*;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.grpc.*;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.sync.concurrent.SuspendableLock;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.SqlConnection;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;
import java.util.function.Function;

import static com.hiddenswitch.framework.schema.spellsource.Tables.*;
import static io.vertx.core.CompositeFuture.all;
import static io.vertx.core.CompositeFuture.any;
import static io.vertx.ext.sync.Sync.await;
import static io.vertx.ext.sync.Sync.fiber;
import static java.util.stream.Collectors.*;
import static org.jooq.impl.DSL.asterisk;

public class Matchmaking extends SyncVerticle {

	private final Promise<Strand> started = Promise.promise();

	public Matchmaking() {
	}

	private static <T> Handler<AsyncResult<T>> replyOrFail(Message<T> message) {
		return previous -> {
			if (!message.isSend()) {
				return;
			}

			if (previous.succeeded()) {
				message.reply("OK");
			} else {
				message.fail(500, previous.cause().getMessage());
			}
		};
	}

	private static boolean isCloseMessage(Message<String> message) {
		return message.body().equals("closed");
	}


	@Override
	@Suspendable
	protected void syncStop() throws SuspendExecution, InterruptedException {
		var strandFuture = strand();
		if (strandFuture.succeeded() && !strandFuture.result().isInterrupted()) {
			var result = strandFuture.result();
			result.interrupt();
			// spin wait until the queue strand is done
			while (result.isAlive()) {
				Strand.sleep(100L);
			}
		}
	}

	@Override
	@Suspendable
	protected void syncStart() throws SuspendExecution, InterruptedException {
		fiber(() -> runServerQueue(vertx));
	}

	public static Future<ServerServiceDefinition> services() {
		return Future.succeededFuture(new VertxMatchmakingGrpc.MatchmakingVertxImplBase() {

			@Override
			public void enqueue(ReadStream<MatchmakingQueuePutRequest> request, WriteStream<MatchmakingQueuePutResponse> response) {
				fiber(() -> runClientEnqueue(request, response));
			}
		}).compose(Accounts::requiresAuthorization);
	}

	public static void runClientEnqueue(ReadStream<MatchmakingQueuePutRequest> request, WriteStream<MatchmakingQueuePutResponse> response) throws SuspendExecution {
		var userId = Accounts.userId();
		var serverConfiguration = Environment.cachedConfigurationOrGet();
		Objects.requireNonNull(userId, "no user id attached to context");
		// this should only really run with a fiber dedicated for its purpose
		var context = Vertx.currentContext();
		// everything we need to clean up at the end of this
		var closeables = new ArrayList<Closeable>();
		// we can close the response no matter what at the end here.
		// closing will throw a StatusRuntimeException if it was already closed
		closeables.add(fut -> response.end().otherwiseEmpty().onComplete(fut));

		try {
			var enqueueStrand = Strand.currentStrand();
			var executor = Environment.queryExecutor();
			// the maximum amount of time to wait for a lock (user can only queue in one place on the cluster)
			var lockTimeout = serverConfiguration.getMatchmaking().getEnqueueLockTimeoutMillis();

			var vertx = Vertx.currentContext().owner();
			var eventBus = vertx.eventBus();
			var commandsFromUser = Sync.<MatchmakingQueuePutRequest>streamAdaptor();

			// TODO: check if the player is already in an unfinished game. if so, reply with its game ID.

			// only allow a user to be queued in one place on the cluster at a time
			// throws VertxException-wrapped TimeoutException if the user is in multiple queues
			var lock = SuspendableLock.lock("matchmaking:enqueue:" + userId, lockTimeout);
			closeables.add(lock);

			// if the user ends the request or some other exception occurs, close everything
			request.endHandler(fiber(v -> enqueueStrand.interrupt()));
			request.exceptionHandler(fiber(v -> enqueueStrand.interrupt()));

			// set up a way to be notified of responses or if the queue gets closed
			var matchmakingChannel = eventBus.<String>consumer("matchmaking:enqueue:" + userId);

			// we only expect game IDs to be communicated on this channel. reply once the response has been written
			// and the client has acknowledged it
			// run in a fiber so it's safe to interrupt another fiber
			matchmakingChannel.handler(fiber(message -> {
				// at this moment, the transaction has committed and the user's ticket should already be deleted or the queue
				// was closed, so no matter what we're interrupting.
				enqueueStrand.interrupt();

				// the queue was closed
				if (isCloseMessage(message)) {
					return;
				}

				// we have a game ID
				var gameId = message.body();
				replyGameId(response, gameId)
						.map(gameId)
						// acknowledge receipt
						.onComplete(replyOrFail(message));
			}));

			// register onto the cluster
			await(matchmakingChannel::completionHandler, lockTimeout);
			closeables.add(matchmakingChannel::unregister);

			// listen for events from the client
			request.handler(commandsFromUser);

			// the ticket is refreshed whenever the request is refreshed
			// many things can interrupt this, like a message from the queue that it is closed, the user closing their
			// request, exceptions on the channels, or database exceptions
			Long lastTicketId = null;
			while (!Strand.currentStrand().isInterrupted()) {
				// interrupted exception throws a VertxException here!
				var fromClient = commandsFromUser.receive();
				if (fromClient == null || fromClient.getCancel()) {
					// cancel
					return;
				}

				if (lastTicketId != null) {
					// delete the old ticket
					Long finalLastTicketId1 = lastTicketId;
					await(executor.execute(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS).where(MATCHMAKING_TICKETS.ID.eq(finalLastTicketId1))));
				}

				// insert the new ticket
				var record = MATCHMAKING_TICKETS.newRecord()
						.setUserId(userId)
						.setDeckId(fromClient.getDeckId())
						// protobufs always give non-null empty strings but sql expects null, "" is a valid identifier
						.setBotDeckId(fromClient.getBotDeckId().isEmpty() ? null : fromClient.getBotDeckId())
						.setQueueId(fromClient.getQueueId());
				if (!fromClient.getDeckId().isEmpty()) {
					record.set(MATCHMAKING_TICKETS.DECK_ID, fromClient.getDeckId());
				}
				if (!fromClient.getBotDeckId().isEmpty()) {
					record.set(MATCHMAKING_TICKETS.BOT_DECK_ID, fromClient.getBotDeckId());
				}
				if (!fromClient.getQueueId().isEmpty()) {
					record.set(MATCHMAKING_TICKETS.QUEUE_ID, fromClient.getQueueId());
				}

				var tickets = await(executor.executeAny(dsl -> dsl
						.insertInto(MATCHMAKING_TICKETS)
						.set(record)
						.returning(MATCHMAKING_TICKETS.ID)).map(rowSet -> Lists.newArrayList(rowSet.iterator())));

				if (tickets.isEmpty()) {
					throw new RuntimeException("failed to insert matchmaking ticket");
				}

				lastTicketId = tickets.get(0).getLong(MATCHMAKING_TICKETS.ID.getName());

				// delete any tickets queued by this user on close
				Long finalLastTicketId2 = lastTicketId;
				closeables.add(v -> executor.execute(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS).where(MATCHMAKING_TICKETS.ID.eq(finalLastTicketId2)))
						.map((Void) null)
						.onComplete(v));
			}
		} catch (Throwable throwable) {
			if (Throwables.getRootCause(throwable) instanceof InterruptedException) {
				// do nothing
				return;
			}
			throwable.printStackTrace();
			throw throwable;
		} finally {
			// goes here with all exceptions eventually
			// run in a separate context in order to not require this to be uninterrupted
			// do it in reverse order so that the actual ending of the connection happens last
			context.runOnContext(v1 -> Lists.reverse(closeables).stream()
					.map(closeable -> {
						var promise = Promise.<Void>promise();
						closeable.close(promise);
						return promise.future();
					})
					.reduce(Future.succeededFuture(), (f1, f2) -> f1.compose(v2 -> f2).onFailure(Environment.onFailure())));
		}
	}

	private static Condition gameStatusIsRunning() {
		return GAMES.TRACE.isNull();
	}

	@Suspendable
	private static Future<Void> replyGameId(WriteStream<MatchmakingQueuePutResponse> response, String gameId) {
		return response.write(MatchmakingQueuePutResponse.newBuilder()
				.setUnityConnection(MatchmakingQueuePutResponseUnityConnection.newBuilder()
						.setGameId(gameId).build()).build());
	}

	public void runServerQueue(Vertx vertx) throws SuspendExecution {
		var serverConfiguration = await(Environment.configuration());

		// dequeueing loop
		while (!Strand.currentStrand().isInterrupted()) {
			var scanFrequency = serverConfiguration.getMatchmaking().getScanFrequencyMillis();
			var maxTickets = serverConfiguration.getMatchmaking().getMaxTicketsToProcess();
			var connection = await(Environment.sqlPoolAkaDaoDelegate().getConnection());
			var transactionObj = await(connection.begin());
			var transaction = new ReactiveClassicGenericQueryExecutor(Environment.jooqAkaDaoConfiguration(), connection);
			try {
				started.tryComplete(Strand.currentStrand());

				// create assignments
				// TODO: Do real matchmaking based on player ELO
				var createGames = new ArrayList<Future>();
				var notifications = new ArrayList<Future>();

				// gather tickets
				// this will lock the rows that this matchmaker might match using the transaction, allowing other matchmakers
				// to skip these easily naturally.
				var records = await(transaction.executeAny(dsl ->
						dsl.deleteFrom(MATCHMAKING_TICKETS)
								.where(MATCHMAKING_TICKETS.ID.in(DSL.using(SQLDialect.POSTGRES)
										.select(MATCHMAKING_TICKETS.ID)
										.from(MATCHMAKING_TICKETS)
										// only retrieve maxTickets at a time
										.limit(maxTickets)
										// lock, prevent others from grabbing these tickets during iteration
										.forUpdate()
										.skipLocked()))
								.returning(asterisk()))
						.map(rowSet -> Lists.newArrayList(rowSet.iterator()))
						.onFailure(Environment.onFailure()))
						.stream().collect(groupingBy(r -> r.getString(MATCHMAKING_TICKETS.QUEUE_ID.getName())));

				// retrieve queues
				var queues = await(transaction.findManyRow(dsl -> dsl.selectFrom(MATCHMAKING_QUEUES)
						.where(MATCHMAKING_QUEUES.ID.in(records.keySet())))).stream().map(RowMappers.getMatchmakingQueuesMapper()).collect(toMap(MatchmakingQueues::getId, Function.identity()));
				for (var group : records.entrySet()) {
					var tickets = group.getValue().stream().map(RowMappers.getMatchmakingTicketsMapper()).collect(toList());
					var configuration = queues.get(group.getKey());
					// did we collect enough tickets to actually make a match?
					var isTooSmall = tickets.size() < configuration.getLobbySize();

					if (isTooSmall) {
						// can't match with not ci
						continue;
					}

					// iterate through each ticket
					// e.g          i < 10             - (2                            - 1); i += 2
					//              i < 9 ; i += 2
					// i will iterate an expected 5 times on 0,1 2,3 4,5 6,7 8,9
					for (var i = 0; i < tickets.size() - (configuration.getLobbySize() - 1); i += configuration.getLobbySize()) {
						// actual body of matchmaking function, this is responsible for determining which players will play against
						// each other
						var thisGameTickets = tickets.subList(i, i + configuration.getLobbySize()).toArray(MatchmakingTickets[]::new);
						// we will await creating the game separately since it has DB side effects
						var game = createGame(transaction, configuration, thisGameTickets);
						createGames.add(game);
						// later we will later notify the players of their assignments
						notifications.add(game
								// notify the players who were queued that they have been matchmade and the game is ready for them
								// as a side effect this closes the matchmaking stream the clients have opened
								.compose(gameId -> all(Arrays.stream(thisGameTickets).map(ticket -> {
									var userId = ticket.getUserId();
									var address = "matchmaking:enqueue:" + userId;

									// observe this is a request
									// the request will only return once the matchmaking stream has been written to, accommodating drops if
									// players close the client after a match has been made but before they have been notified
									// the protocol is to send over the event bus the game ID to the queued player, wherever they are, and
									// once the client has actually received the notification, reply to this event bus request
									return vertx.eventBus().<String>request(address, gameId.toString());
								}).collect(toList()))));
					}
				}

				// notify everyone at the same time
				if (!createGames.isEmpty()) {
					await(all(createGames));
				}
				await(transactionObj.commit());
				// notify all users of assignments now that it has committed
				// TODO: we don't actually want to throw here
				await(all(notifications));
			} catch (PgException t) {
				t.printStackTrace();
				// rolls back automatically
			} catch (Throwable t) {
				t.printStackTrace();
				// we don't need to wait for these cleanup actions, because it will be possibly interrupted
				transactionObj.rollback();
			} finally {
				connection.close();
			}

			// Keep looping otherwise
			try {
				Strand.sleep(scanFrequency);
			} catch (Throwable t) {
				// interrupted
				break;
			}
		}

		// exited the loop, possibly interrupted
		vertx.undeploy(deploymentID());
	}

	public Future<Long> createGame(ReactiveClassicGenericQueryExecutor executor, MatchmakingQueues configuration, MatchmakingTickets... tickets) {
		// TODO: set player index
		return executor.executeAny(dsl -> dsl.insertInto(GAMES).defaultValues().returning(GAMES.ID))
				.onFailure(Environment.onFailure())
				.map(rowSet -> Lists.newArrayList(rowSet.iterator()).get(0).getLong(GAMES.ID.getName()))
				.onFailure(Environment.onFailure())
				.compose(gameId -> {
					var rows = Arrays.stream(tickets).map(ticket -> GAME_USERS.newRecord().setGameId(gameId).setUserId(ticket.getUserId()));
					return CompositeFuture.join(rows.map(row -> executor.execute(dsl -> dsl.insertInto(GAME_USERS).set(row)).onFailure(Environment.onFailure())).collect(toList())).map(gameId);
				});
	}

	public static Future<Void> deleteQueue(String queueId) {
		// setup transaction
		return Environment.sqlPoolAkaDaoDelegate().getConnection().compose(connection -> connection.begin().compose(transactionObj -> {
			var transaction = new ReactiveClassicGenericQueryExecutor(Environment.jooqAkaDaoConfiguration(), connection);
			return transaction
					.executeAny(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS)
							.where(MATCHMAKING_TICKETS.QUEUE_ID.eq(queueId))
							.returningResult(MATCHMAKING_TICKETS.ID, MATCHMAKING_TICKETS.USER_ID))
					.map(tickets -> Streams.stream(tickets.iterator())
							.map(ticket -> ticket.getString(MATCHMAKING_TICKETS.USER_ID.getName()))
							.collect(toList()))
					.compose(forUsers -> {
						for (var userId : forUsers) {
							// send a message to all currently connected users awaiting this queue that the queue is closed
							Vertx.currentContext().owner().eventBus().publish("matchmaking:enqueue:" + userId, "closed");
						}
						return Future.succeededFuture();
					})
					.compose(v -> transaction.execute(dsl -> dsl.deleteFrom(MATCHMAKING_QUEUES).where(MATCHMAKING_QUEUES.ID.eq(queueId))).map((Void) null))
					.compose(v -> transactionObj.commit())
					.recover(t -> transactionObj.rollback())
					.compose(v -> connection.close());
		}));
	}

	public Future<Strand> strand() {
		return started.future();
	}

	public static Future<Closeable> createQueue(MatchmakingQueues configuration) {
		var matchmakingQueuesDao = new MatchmakingQueuesDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
		return matchmakingQueuesDao.insert(configuration)
				.compose(ignored -> Future.succeededFuture(fut -> Matchmaking.deleteQueue(configuration.getId()).onComplete(fut)));
	}
}
