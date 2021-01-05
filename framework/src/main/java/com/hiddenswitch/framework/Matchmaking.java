package com.hiddenswitch.framework;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.hiddenswitch.framework.impl.ConfigurationRequest;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.MatchmakingQueuesDao;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets;
import com.hiddenswitch.framework.schema.spellsource.tables.records.GameUsersRecord;
import com.hiddenswitch.spellsource.rpc.MatchmakingQueuePutRequest;
import com.hiddenswitch.spellsource.rpc.MatchmakingQueuePutResponse;
import com.hiddenswitch.spellsource.rpc.MatchmakingQueuePutResponseUnityConnection;
import com.hiddenswitch.spellsource.rpc.VertxMatchmakingGrpc;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.grpc.ServerServiceDefinition;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.sync.HandlerReceiverAdaptor;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.sync.concurrent.SuspendableLock;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.micrometer.backends.BackendRegistry;
import io.vertx.pgclient.PgException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.hiddenswitch.framework.schema.spellsource.Tables.*;
import static io.vertx.core.CompositeFuture.all;
import static io.vertx.ext.sync.Sync.await;
import static io.vertx.ext.sync.Sync.fiber;
import static java.util.stream.Collectors.*;
import static org.jooq.impl.DSL.asterisk;

public class Matchmaking extends SyncVerticle {
	private final static Counter TICKETS_TAKEN = Counter
			.builder("matchmaking.tickets.taken")
			.baseUnit(BaseUnits.ROWS)
			.description("The number of tickets taken during this evaluation of the matchmaker.")
			.register(Metrics.globalRegistry);
	private final static Timer CLIENT_WAITED_AND_ASSIGNED = Timer
			.builder("matchmaking.tickets.wait.assigned")
			.description("The duration the client waited until it was assigned to a match.")
			.register(Metrics.globalRegistry);
	private final static Timer CLIENT_WAITED_AND_CANCELLED = Timer
			.builder("matchmaking.tickets.wait.cancelled")
			.description("The duration the client waited until giving up and ending matchmaking.")
			.register(Metrics.globalRegistry);
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
				var userId = Accounts.userId();
				// do this as early as possible
				var commandsFromUser = Sync.<MatchmakingQueuePutRequest>streamAdaptor();
				// listen for events from the client
				request.handler(commandsFromUser);

				fiber(() -> runClientEnqueue(request, response, commandsFromUser, userId));
			}
		}).compose(Accounts::requiresAuthorization);
	}

	public static void runClientEnqueue(ReadStream<MatchmakingQueuePutRequest> request, WriteStream<MatchmakingQueuePutResponse> response, HandlerReceiverAdaptor<MatchmakingQueuePutRequest> commandsFromUser, String userId) throws SuspendExecution, InterruptedException {
		var serverConfiguration = Environment.cachedConfigurationOrGet();
		Objects.requireNonNull(userId, "no user id attached to context");
		// this should only really run with a fiber dedicated for its purpose
		var context = Vertx.currentContext();
		// everything we need to clean up at the end of this
		var closeables = new ArrayList<Closeable>();
		// we can close the response no matter what at the end here.
		// closing will throw a StatusRuntimeException if it was already closed
		closeables.add(fut -> {
			try {
				response.end();
			} catch (Throwable t) {
			} finally {
				fut.complete();
			}
		});

		try {
			var startTime = System.nanoTime();
			var enqueueStrand = Strand.currentStrand();
			var executor = Environment.queryExecutor();

			// check if the player is already in a running game by pinging a handler. if so, reply with its game ID.
			var existingGameId = await(Games.getGameId(userId));
			if (existingGameId != null) {
				writeGameId(response, existingGameId);
				return;
			}


			// the maximum amount of time to wait for a lock (user can only queue in one place on the cluster)
			var lockTimeout = serverConfiguration.getMatchmaking().getEnqueueLockTimeoutMillis();
			var vertx = Vertx.currentContext().owner();
			var eventBus = vertx.eventBus();

			// only allow a user to be queued in one place on the cluster at a time
			// throws VertxException-wrapped TimeoutException if the user is in multiple queues
			var lock = SuspendableLock.lock("matchmaking:enqueue:" + userId, lockTimeout);
			closeables.add(lock);

			// if the user ends the request or some other exception occurs, close everything
			Handler<Void> endOrExceptionHandler = (Void v) -> {
				CLIENT_WAITED_AND_CANCELLED.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
				enqueueStrand.interrupt();
			};

			request.endHandler(endOrExceptionHandler);
			request.exceptionHandler(t -> endOrExceptionHandler.handle(null));

			// set up a way to be notified of responses or if the queue gets closed
			var matchmakingChannel = eventBus.<String>consumer("matchmaking:enqueue:" + userId);

			// we only expect game IDs to be communicated on this channel. reply once the response has been written
			// and the client has acknowledged it
			// run in a fiber so it's safe to interrupt another fiber
			matchmakingChannel.handler(message -> {
				CLIENT_WAITED_AND_ASSIGNED.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
				// we have a game ID
				var gameId = message.body();
				writeGameId(response, gameId)
						.map(gameId)
						.onFailure(Environment.onFailure())
						// acknowledge receipt
						.onComplete(replyOrFail(message));

				// at this moment, the transaction has committed and the user's ticket should already be deleted or the queue
				// was closed, so no matter what we're interrupting.
				enqueueStrand.interrupt();
			});

			// register onto the cluster
			await(matchmakingChannel::completionHandler, lockTimeout);
			closeables.add(matchmakingChannel::unregister);

			// the ticket is refreshed whenever the request is refreshed
			// many things can interrupt this, like a message from the queue that it is closed, the user closing their
			// request, exceptions on the channels, or database exceptions
			Long lastTicketId = null;
			MessageConsumer<String> lastClosedListener = null;
			while (!Strand.currentStrand().isInterrupted()) {
				// interrupted exception throws a VertxException here!
				var fromClient = commandsFromUser.receive();
				if (fromClient == null || fromClient.getCancel()) {
					CLIENT_WAITED_AND_CANCELLED.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
					// cancel
					return;
				}

				// insert a new ticket always, delete the old ticket
				// this is because the ticket might currently be taken by a queue in its lock
				if (lastTicketId != null) {
					// delete the old ticket
					Long finalLastTicketId1 = lastTicketId;
					await(executor.execute(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS).where(MATCHMAKING_TICKETS.ID.eq(finalLastTicketId1))));
				}
				if (lastClosedListener != null) {
					closeables.remove((Closeable) lastClosedListener::unregister);
					lastClosedListener.unregister();
				}

				var queueId = fromClient.getQueueId();
				// register if the queue is closed
				var queueClosedChannel = eventBus.<String>consumer("matchmaking:queue-closed:" + queueId);
				closeables.add(queueClosedChannel::unregister);
				queueClosedChannel.handler(ignored -> {
					enqueueStrand.interrupt();
				});

				lastClosedListener = queueClosedChannel;

				// insert the new ticket
				var record = MATCHMAKING_TICKETS.newRecord()
						.setUserId(userId)
						.setDeckId(fromClient.getDeckId())
						// protobufs always give non-null empty strings but sql expects null, "" is a valid identifier
						.setBotDeckId(fromClient.getBotDeckId().isEmpty() ? null : fromClient.getBotDeckId())
						.setQueueId(queueId);
				if (!fromClient.getDeckId().isEmpty()) {
					record.set(MATCHMAKING_TICKETS.DECK_ID, fromClient.getDeckId());
				}
				if (!fromClient.getBotDeckId().isEmpty()) {
					record.set(MATCHMAKING_TICKETS.BOT_DECK_ID, fromClient.getBotDeckId());
				}
				if (!queueId.isEmpty()) {
					record.set(MATCHMAKING_TICKETS.QUEUE_ID, queueId);
				}

				var tickets = await(executor.executeAny(dsl -> dsl
						.insertInto(MATCHMAKING_TICKETS)
						.set(record)
						.returning(MATCHMAKING_TICKETS.ID))
						.map(rowSet -> Lists.newArrayList(rowSet.iterator())));

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
			Throwables.throwIfUnchecked(throwable);
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

	@Suspendable
	private static Future<Void> writeGameId(WriteStream<MatchmakingQueuePutResponse> response, String gameId) {
		var message = MatchmakingQueuePutResponse.newBuilder()
				.setUnityConnection(MatchmakingQueuePutResponseUnityConnection.newBuilder()
						.setGameId(gameId).build()).build();
		try {
			response.write(message);
		} catch (Throwable t) {
			return Future.failedFuture(t);
		}
		return Future.succeededFuture();
	}

	@Suspendable
	public void runServerQueue(Vertx vertx) throws SuspendExecution {
		var serverConfiguration = await(Environment.configuration());
		var smallTimeout = serverConfiguration.getMatchmaking().getEnqueueLockTimeoutMillis();
		var random = new Random();

		// dequeueing loop
		while (!Strand.currentStrand().isInterrupted()) {
			var scanFrequency = serverConfiguration.getMatchmaking().getScanFrequencyMillis();
			var maxTickets = serverConfiguration.getMatchmaking().getMaxTicketsToProcess();
			var transaction = await(Environment.queryExecutor().beginTransaction());
			try {
				started.tryComplete(Strand.currentStrand());

				// create assignments
				// TODO: Do real matchmaking based on player ELO
				var gameCreatedNotifications = new ArrayList<Future>();

				// gather tickets
				// this will lock the rows that this matchmaker might match using the transaction, allowing other matchmakers
				// to skip these easily naturally.
				var records = await(transaction.findManyRow(dsl -> dsl.select(asterisk())
						.from(MATCHMAKING_TICKETS)
						// order by the queue ID to reduce the odds of a deadlock where two matchmakers are each pulling one of
						// two players
						.orderBy(MATCHMAKING_TICKETS.QUEUE_ID)
						// only retrieve maxTickets at a time
						.limit(maxTickets)
						// lock, prevent others from grabbing these tickets during iteration
						.forUpdate()
						.skipLocked())
						.onFailure(Environment.onFailure()))
						.stream()
						.collect(groupingBy(r -> r.getString(MATCHMAKING_TICKETS.QUEUE_ID.getName())));

				var ticketsTaken = new ArrayList<Long>();
				// retrieve queues
				// we could do a join but usually this will be more stuff than just doing it here, because there are many more
				// tickets than queues
				var queues = await(Environment.queryExecutor()
						.findManyRow(dsl -> dsl.selectFrom(MATCHMAKING_QUEUES).where(MATCHMAKING_QUEUES.ID.in(records.keySet()))))
						.stream()
						.map(RowMappers.getMatchmakingQueuesMapper())
						.collect(toMap(MatchmakingQueues::getId, Function.identity()));
				for (var group : records.entrySet()) {
					var tickets = group.getValue().stream().map(RowMappers.getMatchmakingTicketsMapper()).collect(toList());
					var configuration = queues.get(group.getKey());
					// did we collect enough tickets to actually make a match?
					var isTooSmall = tickets.size() < configuration.getLobbySize();

					if (isTooSmall) {
						// can't match with not ci
						continue;
					}

					var i = 0;
					// iterate through each ticket
					// e.g          i < 10             - (2                            - 1); i += 2
					//              i < 9 ; i += 2
					// i will iterate an expected 5 times on 0,1 2,3 4,5 6,7 8,9
					for (; i < tickets.size() - (configuration.getLobbySize() - 1); i += configuration.getLobbySize()) {
						// actual body of matchmaking function, this is responsible for determining which players will play against
						// each other
						var thisGameTickets = tickets.subList(i, i + configuration.getLobbySize()).toArray(MatchmakingTickets[]::new);
						// we will not await creating the game separately since it has DB side effects and it cause too many locks
						// to process everything in a transaction
						gameCreatedNotifications.add(createGame(transaction, configuration, thisGameTickets)
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
									return vertx.eventBus().<String>request(address, gameId.toString(), new DeliveryOptions().setSendTimeout(smallTimeout));
								}).collect(toList()))));
						// later we will later notify the players of their assignments
						for (var ticket : thisGameTickets) {
							ticketsTaken.add(ticket.getId());
						}
					}
				}

				TICKETS_TAKEN.increment(ticketsTaken.size());

				// if a game fails to be created for some reason, log it. maybe we reinsert the ticket??
				// we do not await it though
				all(gameCreatedNotifications).onFailure(Environment.onFailure());

				// delete the tickets that were actually processed
				await(transaction.execute(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS).where(MATCHMAKING_TICKETS.ID.in(ticketsTaken))));
				// it's possible that some player's games never get started, we will not await them before committing though,
				// we want this to happen as fast as possible
				await(transaction.commit());
			} catch (PgException t) {
				// rolls back automatically
			} catch (Throwable t) {
				// we don't need to wait for these cleanup actions, because it will be possibly interrupted
				transaction.rollback();
			}

			// Keep looping otherwise
			// offset the amount of delay by a small random amount to reduce the odds of a deadlock
			try {
				Strand.sleep(scanFrequency + 100 + random.nextInt(Math.max(1, (int) scanFrequency - 100)));
			} catch (Throwable t) {
				// interrupted
				break;
			}
		}

		// exited the loop, possibly interrupted
		vertx.undeploy(deploymentID());
	}

	public Future<Long> createGame(ReactiveClassicGenericQueryExecutor transaction, MatchmakingQueues configuration, MatchmakingTickets... tickets) {
		// we do not have to run these transactionally, do we?
		var executor = Environment.queryExecutor();
		var gameIdRef = new AtomicLong(Long.MIN_VALUE);
		// TODO: set player index
		return executor.executeAny(dsl -> dsl.insertInto(GAMES).defaultValues().returning(GAMES.ID))
				.map(rowSet -> Lists.newArrayList(rowSet.iterator()).get(0).getLong(GAMES.ID.getName()))
				.compose(gameId -> {
					gameIdRef.set(gameId);
					var rows = new ArrayList<GameUsersRecord>();
					for (var i = 0; i < tickets.length; i++) {
						rows.add(GAME_USERS.newRecord().setGameId(gameId)
								.setUserId(tickets[i].getUserId())
								.setDeckId(tickets[i].getDeckId())
								.setPlayerIndex((short) i));
					}
					return CompositeFuture.all(rows.stream().map(row -> executor.execute(dsl -> dsl.insertInto(GAME_USERS).set(row))).collect(toList()))
							.map(gameId);
				})
				.compose(gameId -> {
					if (configuration.getBotOpponent()) {
						// retrieve a bot and choose a random deck for it if one is not specified by the opponent
						return Bots.bot()
								.compose(bot -> {
									var botDeckId = tickets[0].getBotDeckId();
									Future<String> botFut;
									if (botDeckId == null) {
										var random = new Random();
										botFut = Legacy.getAllDecks(bot.getId()).map(res -> res.getDecksList().get(random.nextInt(res.getDecksCount())).getCollection().getId());
									} else {
										botFut = Future.succeededFuture(botDeckId);
									}
									return botFut.compose(botDeckIdRes -> Future.succeededFuture(ConfigurationRequest.botMatch(gameId.toString(), tickets[0].getUserId(), bot.getId(), tickets[0].getDeckId(), botDeckIdRes)));
								});
					} else {
						return Future.succeededFuture(ConfigurationRequest.versusMatch(gameId.toString(), tickets[0].getUserId(), tickets[0].getDeckId(), tickets[1].getUserId(), tickets[1].getDeckId()));
					}
				})
				.compose(request -> {
					var gameId = Long.parseLong(request.getGameId());
					return Games.createGame(request).map(gameId);
				})
				.recover(t -> {
					// game id wsa not created
					if (gameIdRef.get() == Long.MIN_VALUE) {
						// continue
						return Future.failedFuture(t);
					}
					// delete the records if they were created
					// it's not that bad if they hang around due to a machine failure
					return executor.execute(dsl -> dsl.deleteFrom(GAMES).where(GAMES.ID.eq(gameIdRef.get())))
							.compose(v -> Future.failedFuture(t));
				});
	}

	public static Future<Void> deleteQueue(String queueId) {
		// setup transaction
		return Environment.queryExecutor()
				.execute(dsl -> dsl.deleteFrom(MATCHMAKING_QUEUES)
						.where(MATCHMAKING_QUEUES.ID.eq(queueId)))
				.compose(deleted -> {
					if (deleted == 0) {
						return Future.failedFuture("no queue found to delete with ID " + queueId);
					}

					// send a message to all currently connected users awaiting this queue that the queue is closed
					Vertx.currentContext().owner().eventBus().publish("matchmaking:queue-closed:" + queueId, "closed");

					return Future.succeededFuture();
				});
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
