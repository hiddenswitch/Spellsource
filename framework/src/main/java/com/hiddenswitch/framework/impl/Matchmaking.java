package com.hiddenswitch.framework.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.google.common.collect.Streams;
import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.GamesDao;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.Games;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets;
import com.hiddenswitch.spellsource.rpc.*;
import io.grpc.ServerServiceDefinition;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.sync.concurrent.SuspendableLock;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static com.hiddenswitch.framework.schema.spellsource.Tables.MATCHMAKING_TICKETS;
import static io.vertx.core.CompositeFuture.all;
import static io.vertx.ext.sync.Sync.await;
import static io.vertx.ext.sync.Sync.fiber;
import static java.util.stream.Collectors.toList;

public class Matchmaking extends SyncVerticle {

	private final MatchmakingQueueConfiguration configuration;

	protected Matchmaking(MatchmakingQueueConfiguration configuration) {
		this.configuration = configuration;
	}

	private static <T> Handler<AsyncResult<T>> replyOrFail(Message<T> message) {
		return previous -> {
			if (!message.isSend()) {
				return;
			}

			if (previous.succeeded()) {
				message.reply(previous);
			} else {
				message.fail(500, previous.cause().getMessage());
			}
		};
	}

	private static Future<Void> closeQueue(String queueId) {
		// dequeue anyone here and message that the queue is closed
		return Environment.queryExecutor().executeAny(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS)
				.where(MATCHMAKING_TICKETS.QUEUE_ID.eq(queueId), MATCHMAKING_TICKETS.GAME_ID.isNull())
				.returningResult(MATCHMAKING_TICKETS.ID, MATCHMAKING_TICKETS.USER_ID))
				.map(tickets -> Streams.stream(tickets.iterator())
						.map(RowMappers.getMatchmakingTicketsMapper())
						.map(MatchmakingTickets::getUserId).collect(toList()))
				.compose(forUsers -> {
					for (var userId : forUsers) {
						// send a message to all currently connected users awaiting this queue that the queue is closed
						Vertx.currentContext().owner().eventBus().publish("matchmaking:enqueue:" + userId, "closed");
					}
					return Future.succeededFuture();
				});
	}

	private static boolean isCloseMessage(Message<String> message) {
		return message.body().equals("closed");
	}

	private static <T> Handler<AsyncResult<T>> closeAll(List<Closeable> closeables) {
		return previous -> CompositeFuture.all(closeables.stream().map(closeable -> {
			var promise = Promise.<Void>promise();
			closeable.close(promise);
			return promise.future();
		}).collect(toList()));
	}


	@Override
	@Suspendable
	protected void syncStop() throws SuspendExecution, InterruptedException {
		// db side-effects for closing the queue primarily
		await(closeQueue(configuration.getId()));
	}

	@Override
	@Suspendable
	protected void syncStart() throws SuspendExecution, InterruptedException {
		var configuration = this.configuration;
		fiber(() -> runServerQueue(configuration, vertx));
	}

	public static Future<ServerServiceDefinition> services() {
		return Future.succeededFuture(new VertxMatchmakingGrpc.MatchmakingVertxImplBase() {

			@Override
			public void enqueue(ReadStream<MatchmakingQueuePutRequest> request, WriteStream<MatchmakingQueuePutResponse> response) {
				fiber(() -> {
					runClientEnqueue(request, response);
				});
			}
		}).compose(Accounts::requiresAuthorization);
	}

	public static void runClientEnqueue(ReadStream<MatchmakingQueuePutRequest> request, WriteStream<MatchmakingQueuePutResponse> response) throws SuspendExecution {
		// this should only really run with a fiber dedicated for its purpose
		var context = Vertx.currentContext();
		// everything we need to clean up at the end of this
		var closeables = new ArrayList<Closeable>();
		// we can close the response no matter what at the end here.
		closeables.add(response::end);

		try {
			var thisFiber = Strand.currentStrand();
			var executor = Environment.queryExecutor();
			// the maximum amount of time to wait for a lock (user can only queue in one place on the cluster)
			var lockTimeout = 400L;
			var userId = Accounts.userId();
			var ticketId = UUID.randomUUID().toString();
			var vertx = Vertx.currentContext().owner();
			var eventBus = vertx.eventBus();
			var commandsFromUser = Sync.<MatchmakingQueuePutRequest>streamAdaptor();
			// only allow a user to be queued in one place at one time
			// throws VertxException-wrapped TimeoutException if the user is in multiple queues
			var lock = SuspendableLock.lock("matchmaking:enqueue:" + userId, lockTimeout);
			closeables.add(lock);

			// if the user ends the request or some other exception occurs, close everything
			request.endHandler(fiber(v -> thisFiber.interrupt()));
			request.exceptionHandler(fiber(v -> thisFiber.interrupt()));

			// set up a way to be notified of responses or if the queue gets closed
			var matchmakingChannel = eventBus.<String>consumer("matchmaking:enqueue:" + userId);

			// we only expect game IDs to be communicated on this channel. reply once the response has been written
			// and the client has acknowledged it
			// run in a fiber so it's safe to interrupt another fiber
			matchmakingChannel.handler(fiber(message -> {
				// the queue was closed
				if (isCloseMessage(message)) {
					thisFiber.interrupt();
					return;
				}

				response.write(MatchmakingQueuePutResponse.newBuilder()
						.setUnityConnection(MatchmakingQueuePutResponseUnityConnection.newBuilder()
								.setGameId(message.body()).build()).build())
						.map(message.body())
						// acknowledge receipt
						.onComplete(replyOrFail(message))
						// shut down, the user has a game
						.onComplete(v -> thisFiber.interrupt());
			}));

			// register onto the cluster
			await(matchmakingChannel::completionHandler, lockTimeout);
			closeables.add(matchmakingChannel::unregister);

			// listen for events from the client
			request.handler(commandsFromUser);

			// delete any tickets queued by this user on close
			closeables.add(v -> executor.execute(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS).where(MATCHMAKING_TICKETS.USER_ID.eq(userId)))
					.map((Void) null)
					.onComplete(v));

			// delete all other unassigned tickets from this user (could occur due to an ungraceful shutdown)
			await(executor.execute(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS)
					.where(MATCHMAKING_TICKETS.ID.ne(ticketId),
							MATCHMAKING_TICKETS.USER_ID.eq(userId),
							MATCHMAKING_TICKETS.GAME_ID.isNull())));

			// the ticket is refreshed whenever the request is refreshed
			// many things can interrupt this, like a message from the queue that it is closed, the user closing their
			// request, exceptions on the channels, or database exceptions
			while (!Strand.currentStrand().isInterrupted()) {
				// interrupted exception throws a VertxException here!
				var fromClient = commandsFromUser.receive();
				if (fromClient == null || fromClient.getCancel()) {
					// cancel
					return;
				}

				// upserts the ticket
				var ticketsInsertedOrUpdated = await(executor.execute(dsl -> {
					var insert = dsl.insertInto(MATCHMAKING_TICKETS)
							.set(MATCHMAKING_TICKETS.newRecord()
									.setId(ticketId)
									.setUserId(userId)
									.setDeckId(fromClient.getDeckId())
									.setBotDeckId(fromClient.getBotDeckId())
									.setQueueId(fromClient.getQueueId()))
							.onDuplicateKeyUpdate()
							.set(MATCHMAKING_TICKETS.LAST_MODIFIED, OffsetDateTime.now());
					// the user can update decks or the queue they are entered in
					if (!fromClient.getDeckId().isEmpty()) {
						insert.set(MATCHMAKING_TICKETS.DECK_ID, fromClient.getDeckId());
					}
					if (!fromClient.getBotDeckId().isEmpty()) {
						insert.set(MATCHMAKING_TICKETS.BOT_DECK_ID, fromClient.getBotDeckId());
					}
					if (!fromClient.getQueueId().isEmpty()) {
						insert.set(MATCHMAKING_TICKETS.QUEUE_ID, fromClient.getQueueId());
					}
					return insert.where(MATCHMAKING_TICKETS.USER_ID.eq(userId));
				}));

				// no good, should always update or insert
				if (ticketsInsertedOrUpdated == 0) {
					throw new RuntimeException("should have inserted or updated a ticket");
				}
			}
		} finally {
			// goes here with all exceptions eventually
			// run in a separate context in order to not require this to be uninterrupted
			context.runOnContext(v -> closeAll(closeables));
		}
	}

	public void runServerQueue(MatchmakingQueueConfiguration configuration, Vertx vertx) throws SuspendExecution, InterruptedException {
		var maxTickets = configuration.getMaxTicketsToProcess();
		var scanFrequency = configuration.getScanFrequency();
		var timeStarted = System.nanoTime();

		// dequeueing loop
		while (!Strand.currentStrand().isInterrupted()) {
			var didAssignThisIteration = false;
			var transaction = await(Environment.queryExecutor().beginTransaction());
			try {
				// gather tickets
				// this will lock the rows that this matchmaker might match using the transaction, allowing other matchmakers
				// possibly processing the same tickets / queueId to skip these naturally.
				var tickets = await(transaction.findManyRow(dsl ->
						dsl.selectFrom(MATCHMAKING_TICKETS)
								// find only tickets that belong to this queue that haven't been assigned
								.where(MATCHMAKING_TICKETS.QUEUE_ID.eq(configuration.getId()),
										MATCHMAKING_TICKETS.GAME_ID.isNull())
								// retrieve the oldest ones first
								.orderBy(MATCHMAKING_TICKETS.CREATED_AT.asc())
								// only retrieve maxTickets at a time
								.limit(maxTickets)
								// lock, prevent others from grabbing these tickets during iteration
								.forUpdate().of(MATCHMAKING_TICKETS.GAME_ID).skipLocked()))
						.stream().map(RowMappers.getMatchmakingTicketsMapper()).collect(toList());

				// did we collect enough tickets to actually make a match?
				if (tickets.size() < configuration.getLobbySize()) {
					// we failed to get a match and making assignments
					throw new RuntimeException("insufficient lobby size");
				}

				// create assignments
				// TODO: Do real matchmaking based on player ELO
				var assignments = new ArrayList<Future>();

				// iterate through each ticket
				// e.g          i < 10             - (2                            - 1); i += 2
				//              i < 9 ; i += 2
				// i will iterate an expected 5 times on 0,1 2,3 4,5 6,7 8,9
				for (var i = 0; i < tickets.size() - (configuration.getLobbySize() - 1); i += configuration.getLobbySize()) {
					// actual body of matchmaking function, this is responsible for determining which players will play against
					// each other
					var thisGameTickets = tickets.subList(i, i + configuration.getLobbySize()).toArray(MatchmakingTickets[]::new);
					// we will await creating the game
					assignments.add(createGame(configuration, thisGameTickets)
							// then update the tickets in SQL
							.compose(gameId ->
									transaction.execute(dsl -> dsl.update(MATCHMAKING_TICKETS)
											.set(MATCHMAKING_TICKETS.GAME_ID, gameId)
											.set(MATCHMAKING_TICKETS.ASSIGNED_AT, OffsetDateTime.now())
											.where(MATCHMAKING_TICKETS.ID.in(Arrays.stream(thisGameTickets).map(MatchmakingTickets::getId).toArray(String[]::new))))
											.map(gameId))
							// then notify the players who were queued that they have been matchmade and the game is ready for them
							// as a side effect this closes the matchmaking stream the clients have opened
							.compose(gameId -> CompositeFuture.all(Arrays.stream(thisGameTickets).map(ticket -> {
								var userId = ticket.getUserId();
								var address = "matchmaking:enqueue:" + userId;

								// observe this is a request
								// the request will only return once the matchmaking stream has been written to, accommodating drops if
								// players clos the client after a match has been made but before they have been notified
								// the protocol is to send over the event bus the game ID to the queued player, wherever they are, and
								// once the client has actually received the notification, reply to this event bus request
								return vertx.eventBus().request(address, gameId);
							}).collect(toList()))));
				}

				// notify everyone at the same time
				var ids = await(all(assignments));
				await(transaction.commit());

				if (ids.size() > 0) {
					// we succeeded in making assignments
					didAssignThisIteration = true;
				}
			} catch (Throwable t) {
				await(transaction.rollback());
			}

			// if we successfully assigned and we're only supposed to matchmake once, break
			// or, if we didn't assign and the amount of time we're willing to wait for an empty lobby exceeded the amount
			// of time that has passed, break
			if ((didAssignThisIteration && configuration.getOnce())
					|| (!didAssignThisIteration && configuration.getEmptyLobbyTimeout() != 0 && (System.nanoTime() - timeStarted) > configuration.getEmptyLobbyTimeout())) {
				break;
			}

			// Keep looping otherwise
			Strand.sleep(scanFrequency);
		}

		// exited the loop, possibly interrupted
		// we have to delete this queue
		vertx.undeploy(deploymentID());
	}

	public Future<Long> createGame(MatchmakingQueueConfiguration configuration, MatchmakingTickets... tickets) {
		var gamesDao = new GamesDao(Environment.jooqAkaDaoConfiguration(),Environment.sqlPoolAkaDaoDelegate());
		return gamesDao.insertReturningPrimary(new Games());
	}
}
