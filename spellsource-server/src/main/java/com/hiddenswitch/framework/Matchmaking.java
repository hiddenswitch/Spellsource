package com.hiddenswitch.framework;

import com.google.common.collect.Lists;
import com.hiddenswitch.framework.impl.*;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.MatchmakingQueuesDao;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets;
import com.hiddenswitch.framework.schema.spellsource.tables.records.MatchmakingQueuesRecord;
import com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutResponse;
import com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutResponseUnityConnection;
import com.hiddenswitch.spellsource.rpc.VertxMatchmakingGrpcServer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.sqlclient.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hiddenswitch.framework.Environment.withDslContext;
import static com.hiddenswitch.framework.Environment.withExecutor;
import static com.hiddenswitch.framework.schema.spellsource.Tables.*;

public class Matchmaking extends AbstractVerticle {
	public static final String MATCHMAKING_ENQUEUE = "matchmaking:enqueue:";
	public static final String MATCHMAKING_PREPARE = "matchmaking:prepare:";
	public static final String MATCHMAKING_REQUEUE = "matchmaking:requeue:";
	public static final String MATCHMAKING_QUEUE_CLOSED = "matchmaking:queue-closed:";
	public static final String MATCHMAKING_WAKE = "matchmaking:wake";
	public static final String REINSERT_TICKET_SQL = """
			INSERT INTO spellsource.matchmaking_tickets (ticket_id, queue_id, user_id, deck_id, bot_deck_id)
			OVERRIDING SYSTEM VALUE
			VALUES ($1, $2, $3, $4, $5)
			ON CONFLICT (user_id) DO UPDATE SET
			  queue_id = EXCLUDED.queue_id,
			  deck_id = EXCLUDED.deck_id,
			  bot_deck_id = EXCLUDED.bot_deck_id
			""";
	private static final long NOTIFY_RETRY_MILLIS = 50L;
	private static final String CLAIM_COMPLETE_LOBBIES_SQL = """
			WITH ready_queues AS (
			  SELECT t.queue_id
			  FROM spellsource.matchmaking_tickets t
			  JOIN spellsource.matchmaking_queues q ON q.id = t.queue_id
			  GROUP BY t.queue_id, q.lobby_size
			  HAVING COUNT(*) >= q.lobby_size
			),
			locked_tickets AS (
			  SELECT t.ticket_id, t.queue_id, t.user_id, t.deck_id, t.bot_deck_id, t.created_at
			  FROM spellsource.matchmaking_tickets t
			  WHERE t.queue_id IN (SELECT queue_id FROM ready_queues)
			    AND pg_try_advisory_xact_lock(hashtext(t.queue_id))
			  ORDER BY t.queue_id, t.ticket_id
			  FOR UPDATE SKIP LOCKED
			),
			numbered AS (
			  SELECT t.ticket_id, t.queue_id, t.user_id, t.deck_id, t.bot_deck_id, t.created_at,
			         q.id, q.name, q.bot_opponent, q.private_lobby, q.starts_automatically,
			         q.still_connected_timeout, q.empty_lobby_timeout, q.awaiting_lobby_timeout,
			         q.once, q.automatically_close, q.lobby_size, q.queue_created_at,
			         ROW_NUMBER() OVER (PARTITION BY t.queue_id ORDER BY t.ticket_id) AS rn,
			         COUNT(*) OVER (PARTITION BY t.queue_id) AS cnt
			  FROM locked_tickets t
			  JOIN spellsource.matchmaking_queues q ON q.id = t.queue_id
			),
			claimed AS (
			  DELETE FROM spellsource.matchmaking_tickets mt
			  USING numbered n
			  WHERE mt.user_id = n.user_id
			    AND n.rn <= (n.cnt / n.lobby_size) * n.lobby_size
			  RETURNING n.*
			)
			SELECT * FROM claimed ORDER BY queue_id, rn
			""";
	private static final Logger LOGGER = LoggerFactory.getLogger(Matchmaking.class);
	private final static Counter TICKETS_TAKEN = Counter
			.builder("matchmaking.tickets.taken")
			.baseUnit(BaseUnits.ROWS)
			.description("The number of tickets taken during this evaluation of the matchmaker.")
			.register(Metrics.globalRegistry);
	private volatile boolean stopping;
	private MessageConsumer<Object> wakeConsumer;

	public Matchmaking() {
	}

	public static Services services() {
		var matchmakingService = new ClientMatchmakingService();
		return new Services(matchmakingService::bindAll, matchmakingService);
	}

	public static Future<Void> writeGameId(WriteStream<MatchmakingQueuePutResponse> response, String gameId) {
		var message = MatchmakingQueuePutResponse.newBuilder()
				.setUnityConnection(MatchmakingQueuePutResponseUnityConnection.newBuilder()
						.setGameId(gameId).build()).build();
		try {
			return response.write(message);
		} catch (Throwable t) {
			return Future.failedFuture(t);
		}
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
					public void close(Completable<Void> completion) {
						Matchmaking.deleteQueue(configuration.getId()).onComplete(ar -> { if (ar.succeeded()) { completion.succeed(); } else { completion.fail(ar.cause()); } });
					}
				}));
	}

	public static Future<Void> notifyGameReady(String userId, String gameId) {
		var vertx = Vertx.currentContext().owner();
		var serverConfiguration = Environment.getConfiguration();
		var smallTimeout = serverConfiguration.getMatchmaking().getScanFrequencyMillis();
		var deadline = System.currentTimeMillis() + Games.getDefaultConnectionTime();
		return notifyGameReady(vertx, MATCHMAKING_ENQUEUE + userId, gameId, smallTimeout, deadline)
				.onFailure(Environment.onFailure("failed to notify game ready"));
	}

	private static Future<Void> notifyGameReady(Vertx vertx, String address, String gameId, long sendTimeout, long deadline) {
		// observe this is a request
		// the request will only return once the matchmaking stream has been written to, accommodating drops if
		// players close the client after a match has been made but before they have been notified
		// the protocol is to send over the event bus the game ID to the queued player, wherever they are, and
		// once the client has actually received the notification, reply to this event bus request
		return vertx.eventBus().<String>request(address, gameId, new DeliveryOptions().setSendTimeout(sendTimeout))
				.<Void>mapEmpty()
				.recover(cause -> {
					// clients open their notification stream and enqueue at the same time, and a bot queue matches
					// within one scan, so the stream may not be registered yet when the game is created. keep
					// offering the game until the player has had the connection time to show up
					var noHandlers = cause instanceof ReplyException reply && reply.failureType() == ReplyFailure.NO_HANDLERS;
					if (!noHandlers || System.currentTimeMillis() >= deadline) {
						return Future.failedFuture(cause);
					}
					return Future.future(promise -> vertx.setTimer(NOTIFY_RETRY_MILLIS, ignored ->
							notifyGameReady(vertx, address, gameId, sendTimeout, deadline).onComplete(promise)));
				});
	}

	private static Future<Boolean> verifyTicketLive(Vertx vertx, MatchmakingTickets ticket, boolean botOpponent) {
		return verifyTicketLive(vertx, ticket.getUserId(), botOpponent, 3);
	}

	private static Future<Boolean> verifyTicketLive(Vertx vertx, String userId, boolean botOpponent, int retriesLeft) {
		return vertx.eventBus().<String>request(
						MATCHMAKING_PREPARE + userId,
						"ping",
						new DeliveryOptions().setSendTimeout(1500L))
				.map(msg -> true)
				.recover(cause -> {
					if (cause instanceof ReplyException reply && reply.failureType() == ReplyFailure.NO_HANDLERS) {
						if (botOpponent) {
							return Future.succeededFuture(true);
						}
						if (retriesLeft > 0) {
							return Future.future(promise -> vertx.setTimer(25L, ignored ->
									verifyTicketLive(vertx, userId, false, retriesLeft - 1).onComplete(promise)));
						}
					}
					return Future.succeededFuture(false);
				});
	}

	private static Future<Void> deleteTicketByUserId(String userId) {
		return withDslContext(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS)
				.where(MATCHMAKING_TICKETS.USER_ID.eq(userId)))
				.<Void>mapEmpty()
				.otherwiseEmpty();
	}

	private static Future<Void> requeueLiveTicket(Vertx vertx, MatchmakingTickets ticket) {
		var payload = new JsonObject()
				.put("ticketId", ticket.getTicketId())
				.put("queueId", ticket.getQueueId())
				.put("deckId", ticket.getDeckId())
				.put("botDeckId", ticket.getBotDeckId());
		return vertx.eventBus().<String>request(
						MATCHMAKING_REQUEUE + ticket.getUserId(),
						payload,
						new DeliveryOptions().setSendTimeout(1500L))
				.<Void>mapEmpty()
				.otherwiseEmpty();
	}

	private static Future<Void> abortCreatedGame(String gameId, MatchmakingTickets[] tickets) {
		var concedes = new ArrayList<Future<?>>();
		for (var ticket : tickets) {
			concedes.add(Games.concedeGame(ticket.getUserId()).otherwiseEmpty());
		}
		long numericGameId;
		try {
			numericGameId = Long.parseLong(gameId);
		} catch (NumberFormatException e) {
			return Future.all(concedes).mapEmpty();
		}
		return Future.all(concedes)
				.eventually(() -> withDslContext(dsl -> dsl.deleteFrom(GAMES).where(GAMES.ID.eq(numericGameId))).otherwiseEmpty())
				.mapEmpty();
	}

	public static MatchmakingQueuesRecord[] defaultQueues() {
		var quickPlay = MATCHMAKING_QUEUES.newRecord()
				.setName("Quick Play")
				.setId("quickPlay")
				.setAutomaticallyClose(false)
				.setLobbySize(1)
				.setOnce(false)
				.setBotOpponent(true)
				.setPrivateLobby(false)
				.setAwaitingLobbyTimeout(0L)
				.setEmptyLobbyTimeout(0L)
				.setStillConnectedTimeout(4000L)
				.setStartsAutomatically(true);
		var constructed = MATCHMAKING_QUEUES.newRecord()
				.setName("Constructed")
				.setId("constructed")
				.setAutomaticallyClose(false)
				.setLobbySize(2)
				.setOnce(false)
				.setBotOpponent(false)
				.setPrivateLobby(false)
				.setAwaitingLobbyTimeout(0L)
				.setEmptyLobbyTimeout(0L)
				.setStillConnectedTimeout(1000L)
				.setStartsAutomatically(true);
		var rogueRun = MATCHMAKING_QUEUES.newRecord()
				.setName("Rogue Run")
				.setId("rogueRun")
				.setAutomaticallyClose(false)
				.setLobbySize(1)
				.setOnce(false)
				.setBotOpponent(true)
				.setPrivateLobby(false)
				.setAwaitingLobbyTimeout(0L)
				.setEmptyLobbyTimeout(0L)
				.setStillConnectedTimeout(4000L)
				.setStartsAutomatically(true);

		return new MatchmakingQueuesRecord[]{quickPlay, constructed, rogueRun};
	}

	@Override
	public void stop(Promise<Void> stopPromise) throws Exception {
		stopping = true;
		if (wakeConsumer != null) {
			wakeConsumer.unregister().onComplete(ar -> stopPromise.complete());
		} else {
			stopPromise.complete();
		}
	}

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		runServerQueue();
		startPromise.complete();
	}

	public void runServerQueue() {
		LOGGER.trace("running");
		CodecRegistration.register(ConfigurationRequest.class).andRegister(CreateGameSessionResponse.class);
		var serverConfiguration = Environment.getConfiguration();
		var scanFrequency = serverConfiguration.getMatchmaking().getScanFrequencyMillis();

		var scanInProgress = new AtomicBoolean(false);
		var rescanRequested = new AtomicBoolean(false);
		var queueMapper = RowMappers.getMatchmakingQueuesMapper();
		var ticketMapper = RowMappers.getMatchmakingTicketsMapper();

		Runnable[] triggerScanRef = new Runnable[1];
		triggerScanRef[0] = () -> {
			if (stopping) {
				return;
			}
			if (!scanInProgress.compareAndSet(false, true)) {
				rescanRequested.set(true);
				return;
			}

			LOGGER.trace("started matchmaking scan");
			Environment.sqlClient()
					.query(CLAIM_COMPLETE_LOBBIES_SQL)
					.execute()
					.compose(rows -> {
						int claimedCount = rows.size();
						if (claimedCount == 0) {
							return Future.succeededFuture(false);
						}
						LOGGER.trace("claimed {} tickets", claimedCount);
						Map<String, MatchmakingQueues> queues = new LinkedHashMap<>();
						Map<String, List<MatchmakingTickets>> groupedTickets = new LinkedHashMap<>();
						for (Row row : rows) {
							var ticket = ticketMapper.apply(row);
							var queueId = ticket.getQueueId();
							queues.computeIfAbsent(queueId, k -> queueMapper.apply(row));
							groupedTickets.computeIfAbsent(queueId, k -> new ArrayList<>()).add(ticket);
						}

						var queueFutures = new ArrayList<Future<?>>(groupedTickets.size());
						for (var entry : groupedTickets.entrySet()) {
							var queueId = entry.getKey();
							var tickets = entry.getValue();
							var configuration = queues.get(queueId);
							queueFutures.add(processQueueTickets(vertx, configuration, tickets, rescanRequested));
						}
						return Future.all(queueFutures).map(true);
					})
					.onFailure(t -> LOGGER.debug("matchmaking scan error", t))
					.onComplete(ar -> {
						scanInProgress.set(false);
						boolean claimedMore = ar.succeeded() && Boolean.TRUE.equals(ar.result());
						if (!stopping && (rescanRequested.getAndSet(false) || claimedMore)) {
							vertx.runOnContext(v -> triggerScanRef[0].run());
						}
					});
		};

		wakeConsumer = vertx.eventBus().consumer(MATCHMAKING_WAKE, msg -> triggerScanRef[0].run());
		vertx.setPeriodic(scanFrequency, timerId -> {
			if (stopping) {
				vertx.cancelTimer(timerId);
				return;
			}
			triggerScanRef[0].run();
		});
		vertx.runOnContext(v -> triggerScanRef[0].run());
	}

	private Future<Void> processQueueTickets(Vertx vertx,
	                                         MatchmakingQueues configuration,
	                                         List<MatchmakingTickets> tickets,
	                                         AtomicBoolean rescanRequested) {
		var lobbySize = (int) configuration.getLobbySize();
		if (tickets.size() < lobbySize) {
			return Future.succeededFuture();
		}
		boolean botOpponent = Boolean.TRUE.equals(configuration.getBotOpponent());
		var checks = new ArrayList<Future<Boolean>>(tickets.size());
		for (var ticket : tickets) {
			checks.add(verifyTicketLive(vertx, ticket, botOpponent));
		}
		return Future.all(checks).compose(cf -> {
			List<MatchmakingTickets> liveTickets = new ArrayList<>(tickets.size());
			List<MatchmakingTickets> deadTickets = new ArrayList<>();
			for (int i = 0; i < tickets.size(); i++) {
				if (Boolean.TRUE.equals(cf.resultAt(i))) {
					liveTickets.add(tickets.get(i));
				} else {
					deadTickets.add(tickets.get(i));
				}
			}

			var settleFutures = new ArrayList<Future<?>>();
			for (var dead : deadTickets) {
				LOGGER.debug("discarding disconnected matchmaking ticket for userId={} in queueId={}", dead.getUserId(), dead.getQueueId());
				settleFutures.add(deleteTicketByUserId(dead.getUserId()));
			}

			int playableCount = (liveTickets.size() / lobbySize) * lobbySize;
			var matchedTickets = liveTickets.subList(0, playableCount);
			var leftoverTickets = liveTickets.subList(playableCount, liveTickets.size());

			for (var leftover : leftoverTickets) {
				rescanRequested.set(true);
				settleFutures.add(requeueLiveTicket(vertx, leftover));
			}

			int totalTaken = 0;
			for (var i = 0; i <= matchedTickets.size() - lobbySize; i += lobbySize) {
				var thisGameTickets = matchedTickets.subList(i, i + lobbySize).toArray(MatchmakingTickets[]::new);
				startLobbyMatch(vertx, configuration, thisGameTickets, rescanRequested);
				totalTaken += lobbySize;
			}
			if (totalTaken > 0) {
				TICKETS_TAKEN.increment(totalTaken);
			}
			return Future.all(settleFutures).mapEmpty();
		});
	}

	private void startLobbyMatch(Vertx vertx,
	                             MatchmakingQueues configuration,
	                             MatchmakingTickets[] thisGameTickets,
	                             AtomicBoolean rescanRequested) {
		boolean botOpponent = Boolean.TRUE.equals(configuration.getBotOpponent());
		createGame(configuration, thisGameTickets)
				.onFailure(Environment.onFailure("could not create game"))
				.compose(gameId -> {
					if (botOpponent) {
						var notifications = new ArrayList<Future<?>>();
						for (var ticket : thisGameTickets) {
							notifications.add(Matchmaking.notifyGameReady(ticket.getUserId(), gameId));
						}
						return Future.all(notifications).mapEmpty();
					}
					var secondChecks = new ArrayList<Future<Boolean>>(thisGameTickets.length);
					for (var ticket : thisGameTickets) {
						secondChecks.add(verifyTicketLive(vertx, ticket, false));
					}
					return Future.all(secondChecks).compose(cf -> {
						List<MatchmakingTickets> stillLive = new ArrayList<>(thisGameTickets.length);
						List<MatchmakingTickets> nowDead = new ArrayList<>();
						for (int i = 0; i < thisGameTickets.length; i++) {
							if (Boolean.TRUE.equals(cf.resultAt(i))) {
								stillLive.add(thisGameTickets[i]);
							} else {
								nowDead.add(thisGameTickets[i]);
							}
						}
						if (!nowDead.isEmpty()) {
							LOGGER.debug("aborting game {} because {} participant(s) disconnected during game creation", gameId, nowDead.size());
							return abortCreatedGame(gameId, thisGameTickets)
									.eventually(() -> {
										var cleanups = new ArrayList<Future<?>>();
										for (var dead : nowDead) {
											cleanups.add(deleteTicketByUserId(dead.getUserId()));
										}
										for (var live : stillLive) {
											rescanRequested.set(true);
											cleanups.add(requeueLiveTicket(vertx, live));
										}
										return Future.all(cleanups).mapEmpty();
									});
						}
						var notifications = new ArrayList<Future<?>>();
						for (var ticket : thisGameTickets) {
							notifications.add(Matchmaking.notifyGameReady(ticket.getUserId(), gameId));
						}
						return Future.all(notifications).mapEmpty();
					});
				})
				.onFailure(Environment.onFailure("could not notify users"));
	}

	public Future<String> createGame(MatchmakingQueues configuration, MatchmakingTickets... tickets) {
		// TODO: set player index
		return withExecutor(executor -> executor.executeAny(dsl -> dsl.insertInto(GAMES).defaultValues().returning(GAMES.ID))
				.compose(rowSet -> {
					var gameId = Lists.newArrayList(rowSet.iterator()).get(0).getLong(GAMES.ID.getName());
					var added = new ArrayList<Future<?>>();
					for (var i = 0; i < tickets.length; i++) {
						var record = GAME_USERS.newRecord().setGameId(gameId)
								.setUserId(tickets[i].getUserId())
								.setDeckId(tickets[i].getDeckId())
								.setPlayerIndex((short) i);
						added.add(executor.execute(dsl -> dsl.insertInto(GAME_USERS).set(record)));
					}
					return Future.all(added).map(gameId);
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

	public record Services(BindAll<VertxMatchmakingGrpcServer.MatchmakingApi> binder,
	                       ClientMatchmakingService clientMatchmakingService) {
	}
}
