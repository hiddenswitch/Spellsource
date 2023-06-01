package com.hiddenswitch.framework;

import com.google.common.collect.Lists;
import com.hiddenswitch.framework.impl.ClientMatchmakingService;
import com.hiddenswitch.framework.impl.CodecRegistration;
import com.hiddenswitch.framework.impl.ConfigurationRequest;
import com.hiddenswitch.framework.impl.CreateGameSessionResponse;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.MatchmakingQueuesDao;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets;
import com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutResponse;
import com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutResponseUnityConnection;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.grpc.ServerServiceDefinition;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.streams.WriteStream;
import io.vertx.pgclient.PgException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;

import static com.hiddenswitch.framework.Environment.withDslContext;
import static com.hiddenswitch.framework.Environment.withExecutor;
import static com.hiddenswitch.framework.schema.spellsource.Tables.*;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.jooq.impl.DSL.asterisk;

public class Matchmaking extends AbstractVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(Matchmaking.class);
	private final static Counter TICKETS_TAKEN = Counter
			.builder("matchmaking.tickets.taken")
			.baseUnit(BaseUnits.ROWS)
			.description("The number of tickets taken during this evaluation of the matchmaker.")
			.register(Metrics.globalRegistry);

	public static final String MATCHMAKING_ENQUEUE = "matchmaking:enqueue:";
	public static final String MATCHMAKING_QUEUE_CLOSED = "matchmaking:queue-closed:";
	private volatile boolean stopping;

	public Matchmaking() {
	}


	@Override
	public void stop(Promise<Void> stopPromise) throws Exception {
		stopping = true;
		stopPromise.complete();
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		runServerQueue();
		// todo: await(started) ?
		startPromise.complete();
	}

	public static Future<ServerServiceDefinition> services() {
		var matchmakingService = new ClientMatchmakingService();
		return Future.succeededFuture(matchmakingService)
				.compose(Accounts::requiresAuthorization);
	}

	public static Future<Void> writeGameId(WriteStream<MatchmakingQueuePutResponse> response, String gameId) {
		var message = MatchmakingQueuePutResponse.newBuilder()
				.setUnityConnection(MatchmakingQueuePutResponseUnityConnection.newBuilder()
						.setGameId(gameId).build()).build();
		return response.write(message);
	}

	public void runServerQueue() {
		LOGGER.trace("running");
		CodecRegistration.register(ConfigurationRequest.class).andRegister(CreateGameSessionResponse.class);
		var serverConfiguration = Environment.getConfiguration();
		var scanFrequency = serverConfiguration.getMatchmaking().getScanFrequencyMillis();
		var maxTickets = serverConfiguration.getMatchmaking().getMaxTicketsToProcess();

		// dequeueing loop
		vertx.setPeriodic(scanFrequency, timerId -> {
			if (stopping) {
				vertx.cancelTimer(timerId);
				return;
			}

			var queryExecutor = new ReactiveClassicGenericQueryExecutor(Environment.jooqAkaDaoConfiguration(), Environment.transactionPool());
			queryExecutor
					.beginTransaction()
					.compose(transaction -> {
						var ticketsTaken = new ArrayList<String>();
						LOGGER.trace("started matchmaking scan");
						// gather tickets
						// this will lock the rows that this matchmaker might match using the transaction, allowing other matchmakers
						// to skip these easily naturally.
						return transaction.findManyRow(dsl -> dsl.select(asterisk())
										.from(MATCHMAKING_TICKETS)
										// order by the queue ID to reduce the odds of a deadlock where two matchmakers are each pulling one of
										// two players
										.orderBy(MATCHMAKING_TICKETS.QUEUE_ID)
										// only retrieve maxTickets at a time
										.limit(maxTickets)
										// lock, prevent others from grabbing these tickets during iteration
										.forNoKeyUpdate()
										.skipLocked())
								.compose(rows -> {
									var records = rows
											.stream()
											.collect(groupingBy(r -> r.getString(MATCHMAKING_TICKETS.QUEUE_ID.getName())));

									LOGGER.trace("retrieved {} tickets", rows.size());
									// retrieve queues with active tickets
									// we could do a join but usually this will be more stuff than just doing it here, because there are many more
									// tickets than queues
									return transaction
											.findManyRow(dsl -> dsl.selectFrom(MATCHMAKING_QUEUES).where(MATCHMAKING_QUEUES.ID.in(records.keySet())))
											.map(queues -> queues.stream().map(RowMappers.getMatchmakingQueuesMapper())
													.collect(toMap(MatchmakingQueues::getId, Function.identity())))
											.compose(queues -> {
												for (var group : records.entrySet()) {
													var tickets = group.getValue().stream().map(RowMappers.getMatchmakingTicketsMapper()).toList();
													var queueId = group.getKey();
													var configuration = queues.get(queueId);
													// did we collect enough tickets to actually make a match?
													var numberOfTickets = tickets.size();
													var lobbySize = (int) configuration.getLobbySize();
													LOGGER.trace("matchmaking inner loop handling numberOfTickets={}, lobbySize={}", numberOfTickets, lobbySize);
													var isTooSmall = numberOfTickets < lobbySize;
													if (isTooSmall) {
														// can't match with not ci
														LOGGER.trace("matchmaking pulled too few tickets for queueId={}", queueId);
														continue;
													}

													// iterate through each ticket
													// e.g          i < 10             - (2                            - 1); i += 2
													//              i < 9 ; i += 2
													// i will iterate an expected 5 times on 0,1 2,3 4,5 6,7 8,9
													for (var i = 0; i < numberOfTickets - (lobbySize - 1); i += lobbySize) {
														// actual body of matchmaking function, this is responsible for determining which players will play against
														// each other
														var thisGameTickets = tickets.subList(i, i + lobbySize).toArray(MatchmakingTickets[]::new);
														// we will not await creating the game separately to maximize throughput
														createGame(configuration, thisGameTickets)
																.onFailure(Environment.onFailure("could not create game"))
																.compose(gameId -> {
																	var notifications = new ArrayList<Future>();
																	for (var ticket : thisGameTickets) {
																		notifications.add(Matchmaking.notifyGameReady(ticket.getUserId(), gameId));
																	}
																	return CompositeFuture.all(notifications);
																})
																.onFailure(Environment.onFailure("could not notify users"));

														for (var ticket : thisGameTickets) {
															ticketsTaken.add(ticket.getUserId());
														}
													}
												}

												TICKETS_TAKEN.increment(ticketsTaken.size());
												// delete the tickets that were actually processed
												return transaction.execute(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS).where(MATCHMAKING_TICKETS.USER_ID.in(ticketsTaken)));
												// it's possible that some player's games never get started, we will not await them before committing though,
												// we want this to happen as fast as possible

											})
											.compose(v -> transaction.commit())
											.onSuccess(v -> LOGGER.trace("did commit transaction with {} tickets", ticketsTaken.size()));

								})
								.recover(t -> {
									if (t instanceof PgException) {
										return Future.succeededFuture();
									}
									return transaction.rollback();
								});
					});
		});
	}

	public Future<String> createGame(MatchmakingQueues configuration, MatchmakingTickets... tickets) {
		// TODO: set player index
		return withExecutor(executor -> executor.executeAny(dsl -> dsl.insertInto(GAMES).defaultValues().returning(GAMES.ID))
				.compose(rowSet -> {
					var gameId = Lists.newArrayList(rowSet.iterator()).get(0).getLong(GAMES.ID.getName());
					var added = new ArrayList<Future>();
					for (var i = 0; i < tickets.length; i++) {
						var record = GAME_USERS.newRecord().setGameId(gameId)
								.setUserId(tickets[i].getUserId())
								.setDeckId(tickets[i].getDeckId())
								.setPlayerIndex((short) i);
						added.add(executor.execute(dsl -> dsl.insertInto(GAME_USERS).set(record)));
					}
					return CompositeFuture.all(added).map(gameId);
				})
				.compose(gameId -> {
					if (configuration.getBotOpponent()) {
						// retrieve a bot and choose a random deck for it if one is not specified by the opponent
						return Bots.bot()
								.compose(bot -> {
									var botDeckIdPromise = Promise.<String>promise();

									if (tickets[0].getBotDeckId() == null) {
										var random = new Random();
										Legacy.getAllValidDeckIds(bot.getId())
												.map(allValidDeckIds -> allValidDeckIds.get(random.nextInt(allValidDeckIds.size())))
												.onComplete(botDeckIdPromise);

									} else {
										botDeckIdPromise.complete(tickets[0].getBotDeckId());
									}

									return botDeckIdPromise.future()
											.map(botDeckId -> ConfigurationRequest.botMatch(gameId.toString(), tickets[0].getUserId(), bot.getId(), tickets[0].getDeckId(), botDeckId));
								});
					} else {
						return Future.succeededFuture(ConfigurationRequest.versusMatch(gameId.toString(), tickets[0].getUserId(), tickets[0].getDeckId(), tickets[1].getUserId(), tickets[1].getDeckId()));
					}
				}))
				.compose(Games::createGame)
				.map(CreateGameSessionResponse::getGameId);
	}

	public static Future<Void> deleteQueue(String queueId) {
		LOGGER.trace("deleting queueId={}", queueId);
		return withDslContext(dsl -> dsl.deleteFrom(MATCHMAKING_QUEUES)
				.where(MATCHMAKING_QUEUES.ID.eq(queueId)))
				.compose(deleted -> {
					if (deleted == 0) {
						return Future.failedFuture("no queue found to delete with ID " + queueId);
					}

					// send a message to all currently connected users awaiting this queue that the queue is closed
					Vertx.currentContext().owner().eventBus().publish(MATCHMAKING_QUEUE_CLOSED + queueId, "closed");

					return Future.succeededFuture();
				});
	}

	public static Future<Closeable> createQueue(MatchmakingQueues configuration) {
		var matchmakingQueuesDao = new MatchmakingQueuesDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
		return matchmakingQueuesDao.insert(configuration, true)
				.onSuccess(i -> LOGGER.debug("created queue {} ({})", configuration.getId(), i == 1))
				.compose(ignored -> Future.succeededFuture(new Closeable() {
					@Override
					public void close(Promise<Void> completion) {
						Matchmaking.deleteQueue(configuration.getId()).onComplete(completion);
					}
				}));
	}

	public static Future<Void> notifyGameReady(String userId, String gameId) {
		var address = MATCHMAKING_ENQUEUE + userId;
		var vertx = Vertx.currentContext().owner();
		var serverConfiguration = Environment.getConfiguration();
		var smallTimeout = serverConfiguration.getMatchmaking().getScanFrequencyMillis();

		// observe this is a request
		// the request will only return once the matchmaking stream has been written to, accommodating drops if
		// players close the client after a match has been made but before they have been notified
		// the protocol is to send over the event bus the game ID to the queued player, wherever they are, and
		// once the client has actually received the notification, reply to this event bus request
		return vertx.eventBus().<String>request(address, gameId, new DeliveryOptions().setSendTimeout(smallTimeout))
				.onFailure(Environment.onFailure("failed to notify game ready"))
				.mapEmpty();
	}
}
