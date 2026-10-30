package com.hiddenswitch.framework;

import com.google.common.collect.Lists;
import com.hiddenswitch.diagnostics.Traced;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.framework.impl.*;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.MatchmakingQueuesDao;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets;
import com.hiddenswitch.framework.schema.spellsource.tables.records.MatchmakingQueuesRecord;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutResponse;
import com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutResponseUnityConnection;
import com.hiddenswitch.spellsource.rpc.VertxMatchmakingGrpcServer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.tag.Tags;
import io.vertx.core.*;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.WriteStream;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hiddenswitch.framework.Environment.withDslContext;
import static com.hiddenswitch.framework.Environment.withExecutor;
import static com.hiddenswitch.framework.schema.spellsource.Tables.*;
import static io.vertx.await.Async.await;
import static io.vertx.await.Async.forEachParallel;
import static io.vertx.await.Async.join;
import static io.vertx.await.Async.mapParallel;
import static io.vertx.await.Async.openScope;
import static io.vertx.await.Async.virtualThreadFactory;

public class Matchmaking extends AbstractVirtualThreadVerticle {
	public static final String MATCHMAKING_ENQUEUE = "matchmaking:enqueue:";
	public static final String MATCHMAKING_PREPARE = "matchmaking:prepare:";
	public static final String MATCHMAKING_QUEUE_CLOSED = "matchmaking:queue-closed:";
	public static final String MATCHMAKING_WAKE = "matchmaking:wake";
	private static final long NOTIFY_RETRY_MILLIS = 50L;
	private static final String CLAIM_COMPLETE_LOBBIES_SQL = """
			WITH locked_tickets AS (
			  SELECT t.ticket_id, t.queue_id, q.lobby_size,
			         ROW_NUMBER() OVER (PARTITION BY t.queue_id ORDER BY t.ticket_id) AS rn,
			         COUNT(*) OVER (PARTITION BY t.queue_id) AS total_in_queue
			  FROM (
			    SELECT ticket_id, queue_id
			    FROM spellsource.matchmaking_tickets
			    WHERE created_at <= now()
			    ORDER BY queue_id, ticket_id
			    FOR UPDATE SKIP LOCKED
			  ) t
			  JOIN spellsource.matchmaking_queues q ON q.id = t.queue_id
			),
			to_claim AS (
			  SELECT ticket_id, queue_id
			  FROM locked_tickets
			  WHERE rn <= (total_in_queue / lobby_size) * lobby_size
			),
			claimed AS (
			  UPDATE spellsource.matchmaking_tickets t
			  SET created_at = now() + interval '1500 milliseconds'
			  FROM to_claim c
			  WHERE t.ticket_id = c.ticket_id
			  RETURNING t.ticket_id, t.queue_id, t.user_id, t.deck_id, t.bot_deck_id, t.created_at
			)
			SELECT d.ticket_id, d.queue_id, d.user_id, d.deck_id, d.bot_deck_id, d.created_at,
			       q.id, q.name, q.bot_opponent, q.private_lobby, q.starts_automatically,
			       q.still_connected_timeout, q.empty_lobby_timeout, q.awaiting_lobby_timeout,
			       q.once, q.automatically_close, q.lobby_size, q.queue_created_at
			FROM claimed d
			JOIN spellsource.matchmaking_queues q ON q.id = d.queue_id
			ORDER BY d.queue_id, d.ticket_id
			""";
	private static final Logger LOGGER = LoggerFactory.getLogger(Matchmaking.class);
	private final static Counter TICKETS_TAKEN = Counter
			.builder("matchmaking.tickets.taken")
			.baseUnit(BaseUnits.ROWS)
			.description("The number of tickets taken during this evaluation of the matchmaker.")
			.register(Metrics.globalRegistry);
	private static final Map<String, Span> ACTIVE_TICKET_SPANS = new ConcurrentHashMap<>();
	private volatile boolean stopping;
	private MessageConsumer<Object> wakeConsumer;
	private Long periodicTimerId;

	private static Span createTicketSpan(String userId, String queueId, String deckId, String botDeckId) {
		return Tracing.tracing().buildSpan("Matchmaking/enqueue")
				.asChildOf(Tracing.activeSpanContext())
				.withTag("userId", userId)
				.withTag("queueId", Objects.requireNonNullElse(queueId, ""))
				.withTag("deckId", Objects.requireNonNullElse(deckId, ""))
				.withTag("botDeckId", Objects.requireNonNullElse(botDeckId, ""))
				.start();
	}

	public static Span startTicketSpan(String userId, String queueId, String deckId, String botDeckId) {
		var span = createTicketSpan(userId, queueId, deckId, botDeckId);
		Optional.ofNullable(ACTIVE_TICKET_SPANS.put(userId, span))
				.ifPresent(prev -> prev.setTag("status", "superseded").setTag("cancelled", true).finish());
		return span;
	}

	public static Span ensureTicketSpan(MatchmakingTickets ticket) {
		return ACTIVE_TICKET_SPANS.computeIfAbsent(ticket.getUserId(),
				userId -> createTicketSpan(userId, ticket.getQueueId(), ticket.getDeckId(), ticket.getBotDeckId()));
	}

	public static void finishTicketSpanMatched(String userId, String gameId, String status) {
		Optional.ofNullable(ACTIVE_TICKET_SPANS.remove(userId))
				.ifPresent(span -> span.setTag("gameId", gameId).setTag("status", status).finish());
	}

	public static void finishTicketSpanCancelled(String userId, String status) {
		Optional.ofNullable(ACTIVE_TICKET_SPANS.remove(userId))
				.ifPresent(span -> span.setTag("status", status).setTag("cancelled", true).finish());
	}

	public static void finishTicketSpanAborted(String userId, String abortedGameId) {
		Optional.ofNullable(ACTIVE_TICKET_SPANS.remove(userId))
				.ifPresent(span -> span.setTag(Tags.ERROR.getKey(), true).setTag("status", "aborted").setTag("abortedGameId", abortedGameId).finish());
	}

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
		return response.write(message);
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
				.map(ignored -> completion -> Matchmaking.deleteQueue(configuration.getId()).onComplete(completion));
	}

	public static Future<Void> notifyGameReady(String userId, String gameId, boolean retryIfNoHandlers) {
		var vertx = Vertx.currentContext().owner();
		var serverConfiguration = Environment.getConfiguration();
		var smallTimeout = serverConfiguration.getMatchmaking().getScanFrequencyMillis();
		var deadline = retryIfNoHandlers ? System.currentTimeMillis() + Games.getDefaultConnectionTime() : 0L;
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
		return verifyTicketLive(vertx, ticket.getUserId(), botOpponent, 6);
	}

	private static Future<Boolean> verifyTicketLive(Vertx vertx, String userId, boolean botOpponent, int retriesLeft) {
		return vertx.eventBus().<String>request(
						MATCHMAKING_PREPARE + userId,
						"ping",
						new DeliveryOptions().setSendTimeout(6000L))
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

	private static Future<Void> releaseTicketByUserId(String userId) {
		return withDslContext(dsl -> dsl.update(MATCHMAKING_TICKETS)
				.set(MATCHMAKING_TICKETS.CREATED_AT, org.jooq.impl.DSL.field("now()", org.jooq.impl.SQLDataType.TIMESTAMPWITHTIMEZONE))
				.where(MATCHMAKING_TICKETS.USER_ID.eq(userId)))
				.<Void>mapEmpty()
				.otherwiseEmpty();
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
	public void stopVirtual() {
		stopping = true;
		vertx.cancelTimer(periodicTimerId);
		await(wakeConsumer.unregister().otherwiseEmpty());
	}

	@Override
	public void startVirtual() {
		runServerQueue();
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
			if (stopping || !scanInProgress.compareAndSet(false, true)) {
				rescanRequested.set(true);
				return;
			}

			virtualThreadFactory(vertx).newThread(() -> {
				boolean claimedMore = false;
				try {
					LOGGER.trace("started matchmaking scan");
					var rows = await(Environment.sqlClient().query(CLAIM_COMPLETE_LOBBIES_SQL).execute());
					int claimedCount = rows.size();
					if (claimedCount > 0) {
						claimedMore = true;
						LOGGER.trace("claimed {} tickets", claimedCount);
						Map<String, MatchmakingQueues> queues = new LinkedHashMap<>();
						Map<String, List<MatchmakingTickets>> groupedTickets = new LinkedHashMap<>();
						for (Row row : rows) {
							var ticket = ticketMapper.apply(row);
							var queueId = ticket.getQueueId();
							queues.computeIfAbsent(queueId, k -> queueMapper.apply(row));
							groupedTickets.computeIfAbsent(queueId, k -> new ArrayList<>()).add(ticket);
						}
						forEachParallel(groupedTickets.entrySet(), entry ->
								processQueueTickets(vertx, queues.get(entry.getKey()), entry.getValue()));
					}
				} finally {
					scanInProgress.set(false);
					if (!stopping && (rescanRequested.getAndSet(false) || claimedMore)) {
						vertx.runOnContext(v -> triggerScanRef[0].run());
					}
				}
			}).start();
		};

		wakeConsumer = vertx.eventBus().consumer(MATCHMAKING_WAKE, msg -> triggerScanRef[0].run());
		periodicTimerId = vertx.setPeriodic(scanFrequency, timerId -> triggerScanRef[0].run());
		vertx.runOnContext(v -> triggerScanRef[0].run());
	}

	@Traced
	private void processQueueTickets(Vertx vertx,
	                                 MatchmakingQueues configuration,
	                                 List<MatchmakingTickets> tickets) {
		for (var ticket : tickets) {
			ensureTicketSpan(ticket);
		}
		var lobbySize = (int) configuration.getLobbySize();
		boolean botOpponent = Boolean.TRUE.equals(configuration.getBotOpponent());
		var liveStates = mapParallel(tickets, ticket -> await(verifyTicketLive(vertx, ticket, botOpponent)));
		List<MatchmakingTickets> liveTickets = new ArrayList<>(tickets.size());
		int playableCount;
		try (var scope = openScope(vertx)) {
			for (int i = 0; i < tickets.size(); i++) {
				var ticket = tickets.get(i);
				if (Boolean.TRUE.equals(liveStates.get(i))) {
					liveTickets.add(ticket);
				} else {
					scope.fork(() -> {
						await(deleteTicketByUserId(ticket.getUserId()));
						finishTicketSpanCancelled(ticket.getUserId(), "disconnected");
						return null;
					});
				}
			}

			playableCount = (liveTickets.size() / lobbySize) * lobbySize;
			for (var i = 0; i <= playableCount - lobbySize; i += lobbySize) {
				var thisGameTickets = liveTickets.subList(i, i + lobbySize).toArray(MatchmakingTickets[]::new);
				scope.fork(() -> {
					startLobbyMatch(vertx, configuration, thisGameTickets);
					return null;
				});
			}
			for (var i = playableCount; i < liveTickets.size(); i++) {
				var unplayableTicket = liveTickets.get(i);
				scope.fork(() -> {
					ensureTicketSpan(unplayableTicket).log(Map.of("event", "released", "reason", "awaiting_lobby"));
					await(releaseTicketByUserId(unplayableTicket.getUserId()));
					return null;
				});
			}
			if (playableCount > 0) {
				TICKETS_TAKEN.increment(playableCount);
			}
			join(scope);
		}
		if (playableCount < liveTickets.size()) {
			vertx.eventBus().publish(MATCHMAKING_WAKE, "wake");
		}
	}

	private void startLobbyMatch(Vertx vertx,
	                             MatchmakingQueues configuration,
	                             MatchmakingTickets[] thisGameTickets) {
		var ticketContexts = new ArrayList<SpanContext>(thisGameTickets.length);
		for (var ticket : thisGameTickets) {
			ticketContexts.add(ensureTicketSpan(ticket).context());
		}
		try (var ignored = Tracing.enterSpanWithLinks("Matchmaking/startLobbyMatch", ticketContexts)) {
			boolean botOpponent = Boolean.TRUE.equals(configuration.getBotOpponent());
			var gameId = await(createGame(configuration, thisGameTickets));
			try {
				forEachParallel(List.of(thisGameTickets), ticket ->
						await(Matchmaking.notifyGameReady(ticket.getUserId(), gameId, botOpponent)));
				forEachParallel(List.of(thisGameTickets), ticket -> {
					await(deleteTicketByUserId(ticket.getUserId()));
					finishTicketSpanMatched(ticket.getUserId(), gameId, "matched");
				});
			} catch (Throwable err) {
				compensateAbortedGame(vertx, gameId, thisGameTickets);
			}
		}
	}

	@Traced
	private void compensateAbortedGame(Vertx vertx,
	                                   String gameId,
	                                   MatchmakingTickets[] thisGameTickets) {
		long numericGameId = Long.parseLong(gameId);
		try (var scope = openScope(vertx)) {
			scope.fork(() -> {
				forEachParallel(List.of(thisGameTickets), ticket ->
						await(Games.concedeGame(ticket.getUserId()).otherwiseEmpty()));
				await(withDslContext(dsl -> dsl.deleteFrom(GAMES).where(GAMES.ID.eq(numericGameId))).otherwiseEmpty());
				return null;
			});
			for (var ticket : thisGameTickets) {
				scope.fork(() -> {
					if (await(verifyTicketLive(vertx, ticket.getUserId(), false, 0))) {
						ensureTicketSpan(ticket)
								.setTag("compensated", true)
								.log(Map.of("event", "compensated_requeued", "abortedGameId", gameId));
						await(releaseTicketByUserId(ticket.getUserId()));
					} else {
						await(deleteTicketByUserId(ticket.getUserId()));
						finishTicketSpanAborted(ticket.getUserId(), gameId);
					}
					return null;
				});
			}
			join(scope);
		}
		vertx.eventBus().publish(MATCHMAKING_WAKE, "wake");
	}

	@Traced
	public Future<String> createGame(MatchmakingQueues configuration, MatchmakingTickets... tickets) {
		Long gameId;
		ConfigurationRequest request;
		try (var scope = openScope(vertx)) {
			var gameIdTask = scope.fork(() -> await(withExecutor(executor ->
					executor.executeAny(dsl -> dsl.insertInto(GAMES).defaultValues().returning(GAMES.ID))
							.map(rowSet -> rowSet.iterator().next().getLong(GAMES.ID.getName())))));
			var configTask = scope.fork(() -> {
				if (Boolean.TRUE.equals(configuration.getBotOpponent())) {
					var bot = await(Bots.bot());
					var botDeckId = Objects.requireNonNullElseGet(tickets[0].getBotDeckId(), () -> await(Legacy.getAllValidDeckIds(bot.getId()).map(ids -> ids.get(new Random().nextInt(ids.size())))));
					return ConfigurationRequest.botMatch(null, tickets[0].getUserId(), bot.getId(), tickets[0].getDeckId(), botDeckId);
				}
				return ConfigurationRequest.versusMatch(null, tickets[0].getUserId(), tickets[0].getDeckId(), tickets[1].getUserId(), tickets[1].getDeckId());
			});
			join(scope);
			gameId = gameIdTask.get();
			request = configTask.get();
		}
		request.setGameId(gameId.toString());
		try (var scope = openScope(vertx)) {
			for (var i = 0; i < tickets.length; i++) {
				var record = GAME_USERS.newRecord()
						.setGameId(gameId)
						.setUserId(tickets[i].getUserId())
						.setDeckId(tickets[i].getDeckId())
						.setPlayerIndex((short) i);
				scope.fork(() -> await(withDslContext(dsl -> dsl.insertInto(GAME_USERS).set(record))));
			}
			scope.fork(() -> await(Games.createGame(request)));
			join(scope);
		}
		return Future.succeededFuture(gameId.toString());
	}

	public record Services(BindAll<VertxMatchmakingGrpcServer.MatchmakingApi> binder,
	                       ClientMatchmakingService clientMatchmakingService) {
	}
}
