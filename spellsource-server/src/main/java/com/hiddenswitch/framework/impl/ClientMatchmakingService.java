package com.hiddenswitch.framework.impl;

import com.google.common.base.Strings;
import com.hiddenswitch.diagnostics.Tracing;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Matchmaking;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import com.hiddenswitch.spellsource.rpc.VertxMatchmakingGrpcServer;
import io.grpc.internal.KeepAliveManager;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Metrics;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.grpc.common.impl.GrpcWriteStreamBase;
import io.vertx.grpc.server.GrpcServerRequest;
import io.vertx.sqlclient.Tuple;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.hiddenswitch.framework.Environment.withDslContext;
import static com.hiddenswitch.framework.Matchmaking.*;
import static com.hiddenswitch.framework.schema.spellsource.Tables.MATCHMAKING_TICKETS;
import static io.vertx.core.Future.any;
import static io.vertx.core.Future.join;

public class ClientMatchmakingService implements Closeable, VertxMatchmakingGrpcServer.MatchmakingApi {
	private final static LongTaskTimer MATCHMAKING_TICKETS_DURATION = LongTaskTimer
			.builder("spellsource_matchmaking_tickets_duration")
			.minimumExpectedValue(Duration.ofMillis(5))
			.maximumExpectedValue(Duration.ofSeconds(120))
			.publishPercentileHistogram()
			.tag("status", "unknown")
			.description("The duration the client waited until it was assigned to a match.")
			.register(Metrics.globalRegistry);

	private final static Logger LOGGER = LoggerFactory.getLogger(ClientMatchmakingService.class);
	private final Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());
	private final Map<String, Session> activeSessionsByUser = new ConcurrentHashMap<>();

	public ClientMatchmakingService() {
	}

	public int getActiveSessionCount() {
		return sessions.size();
	}

	@Override
	public void enqueue(GrpcServerRequest<Spellsource.MatchmakingQueuePutRequest, Spellsource.MatchmakingQueuePutResponse> grpcServerRequest, ReadStream<Spellsource.MatchmakingQueuePutRequest> request, WriteStream<Spellsource.MatchmakingQueuePutResponse> response) {
		var userId = grpcServerRequest.routingContext().user().subject();
		Objects.requireNonNull(userId);

		var vertx = Vertx.currentContext().owner();
		var eventBus = vertx.eventBus();
		var timer = MATCHMAKING_TICKETS_DURATION.start();
		var channels = Collections.synchronizedList(new ArrayList<MessageConsumer>());
		var grpcParentSpan = Tracing.extractFromHeaders(grpcServerRequest.headers());
		var session = new Session(s -> {
			activeSessionsByUser.remove(s.userId(), s);
			sessions.remove(s);
		}, response, userId, channels, timer, new AtomicBoolean(false), grpcServerRequest.connection());
		sessions.add(session);
		var previousSession = activeSessionsByUser.put(userId, session);
		if (previousSession != null) {
			session.chainInitialWrite(previousSession.close().otherwiseEmpty());
		}
		session.keepAliveManager().onTransportStarted();

		LOGGER.trace("enqueue called userId={}", userId);

		var prepareConsumer = eventBus.<String>consumer(MATCHMAKING_PREPARE + userId);
		prepareConsumer.handler(message -> session.verifyAlive(vertx)
				.onSuccess(v -> message.reply("ok"))
				.onFailure(err -> message.fail(1, Objects.requireNonNullElse(err.getMessage(), "session disconnected"))));
		channels.add(prepareConsumer);

		// notify the user when a game has been assigned
		var enqueueConsumer = eventBus.<String>consumer(MATCHMAKING_ENQUEUE + userId);
		enqueueConsumer.handler(message -> {
			session.matched.set(true);
			writeGameId(response, message.body())
					.compose(v -> session.close())
					.onSuccess(v -> message.reply("ok"));
		});
		channels.add(enqueueConsumer);

		var consumersReady = Future.all(
				prepareConsumer.completion(),
				enqueueConsumer.completion()
		).mapEmpty().otherwiseEmpty();

		request.handler(message -> {
			session.keepAliveManager().onDataReceived();
			LOGGER.trace("client matchmaking service did get message (deckId={} cancel={}) for userId={}", message.getDeckId(), message.getCancel(), userId);
			if (session.cancelled.get()) {
				return;
			}

			if (message.getCancel()) {
				session.close();
				return;
			}

			try (var ignored = Tracing.enterSpan("Matchmaking/grpcEnqueue", grpcParentSpan)) {
				Matchmaking.startTicketSpan(userId, message.getQueueId(), message.getDeckId(), message.getBotDeckId());
			}

			Games.getGameId(userId)
					.compose(existingGameId -> {
						if (existingGameId != null) {
							session.matched.set(true);
							Matchmaking.finishTicketSpanMatched(userId, existingGameId, "existing_game");
							return Matchmaking.writeGameId(response, existingGameId)
									.eventually(session::close);
						}

						var queueId = message.getQueueId();
						var record = MATCHMAKING_TICKETS.newRecord()
								.setUserId(userId)
								.setQueueId(queueId)
								.setDeckId(message.getDeckId())
								.setBotDeckId(message.getBotDeckId().isEmpty() ? null : message.getBotDeckId());
						return consumersReady.compose(ignored -> session.enqueueTicketWrite(() -> {
							session.registerQueueClosedChannel(eventBus, queueId);
							return withDslContext(dsl -> dsl
									.insertInto(MATCHMAKING_TICKETS)
									.set(record)
									.onDuplicateKeyUpdate()
									.set(record));
						}).onSuccess(v -> eventBus.publish(MATCHMAKING_WAKE, queueId)));
					})
					.onFailure(t -> session.close());
		});
		response.exceptionHandler(v -> session.close());
		// handle the request messages, which are queue tickets
		request.exceptionHandler(v -> session.close());
		request.endHandler(v -> session.close());
	}


	@Override
	public void close(Completable<Void> completion) {
		// interrupt all the threads, runClientEnqueue will handle it appropriately
		var closeAll = new ArrayList<Future<?>>();
		for (var session : new ArrayList<>(sessions)) {
			closeAll.add(session.close());
		}

		join(closeAll).onComplete(v -> completion.succeed());
	}

	public static final class Session {
		private final Consumer<Session> remover;
		private final WriteStream<Spellsource.MatchmakingQueuePutResponse> response;
		private final @NotNull String userId;
		private final List<MessageConsumer> channels;
		private final LongTaskTimer.Sample timer;
		private final AtomicBoolean cancelled;
		private final AtomicBoolean matched = new AtomicBoolean(false);
		private final KeepAliveManager keepAliveManager;
		private final HttpConnection connection;
		private final Promise<Void> closePromise = Promise.promise();
		private Future<?> ticketWrite = Future.succeededFuture();
		private Future<Void> closeFuture;
		private MessageConsumer<String> queueClosedChannel;
		private String queueClosedQueueId;

		Session(Consumer<Session> remover,
		        WriteStream<Spellsource.MatchmakingQueuePutResponse> response,
		        @NotNull String userId,
		        List<MessageConsumer> channels,
		        LongTaskTimer.Sample timer,
		        AtomicBoolean cancelled,
		        HttpConnection connection) {
			this.remover = remover;
			this.response = response;
			this.userId = userId;
			this.channels = channels;
			this.timer = timer;
			this.cancelled = cancelled;
			this.connection = connection;
			this.keepAliveManager = Environment.keepAliveManager(connection, v -> close(), true);
		}

		public KeepAliveManager keepAliveManager() {
			return keepAliveManager;
		}

		synchronized void registerQueueClosedChannel(io.vertx.core.eventbus.EventBus eventBus, String queueId) {
			if (this.queueClosedChannel != null) {
				if (Objects.equals(this.queueClosedQueueId, queueId)) {
					return;
				}
				this.channels.remove(this.queueClosedChannel);
				this.queueClosedChannel.unregister();
			}
			var channel = eventBus.<String>consumer(MATCHMAKING_QUEUE_CLOSED + queueId);
			channel.handler(v2 -> this.close());
			this.queueClosedChannel = channel;
			this.queueClosedQueueId = queueId;
			this.channels.add(channel);
		}

		synchronized void chainInitialWrite(Future<?> prev) {
			this.ticketWrite = this.ticketWrite.mapEmpty().otherwiseEmpty().compose(ignored -> prev);
		}

		public synchronized Future<Void> enqueueTicketWrite(Supplier<Future<?>> writeSupplier) {
			this.ticketWrite = this.ticketWrite.mapEmpty().otherwiseEmpty()
					.compose(ignored -> {
						synchronized (Session.this) {
							if (this.cancelled.get()) {
								return Future.failedFuture("session closed");
							}
							return writeSupplier.get();
						}
					});
			return this.ticketWrite.mapEmpty();
		}

		private boolean isDead() {
			var grpcWriteStream = (GrpcWriteStreamBase<?, ?>) this.response;
			return this.cancelled.get() || grpcWriteStream.isCancelled() || grpcWriteStream.isTrailersSent() || grpcWriteStream.hasError();
		}

		public Future<Void> verifyAlive(Vertx vertx) {
			Future<Void> pingCheck = any(this.connection.ping(Buffer.buffer(new byte[]{1, 2, 3, 4, 5, 6, 7, 8})).mapEmpty(), this.closePromise.future())
					.compose(cf -> {
						if (isDead()) {
							return Future.failedFuture("session closed");
						}
						this.keepAliveManager.onDataReceived();
						return Future.succeededFuture();
					});
			return Environment.timeout(pingCheck, 5000L, vertx)
					.onFailure(err -> this.close());
		}

		public Future<Void> close() {
			Future<?> pendingTicketWrite;
			synchronized (this) {
				this.cancelled.set(true);
				this.closePromise.tryComplete();
				if (this.closeFuture != null) {
					return this.closeFuture;
				}
				this.remover.accept(this);
				if (!this.matched.get()) {
					Matchmaking.finishTicketSpanCancelled(this.userId, "cancelled");
				}
				pendingTicketWrite = this.ticketWrite;

				this.timer.stop();
				keepAliveManager.onTransportTermination();

				var closedChannels = new ArrayList<Future<?>>();
				synchronized (this.channels) {
					for (var channel : this.channels) {
						closedChannels.add(channel.unregister());
					}
					this.channels.clear();
				}

				var closedResponse = pendingTicketWrite.mapEmpty().otherwiseEmpty()
						.compose(ignored -> withDslContext(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS)
								.where(MATCHMAKING_TICKETS.USER_ID.eq(this.userId))))
						.eventually(() -> this.response.end().otherwiseEmpty());
				closedChannels.add(closedResponse);
				this.closeFuture = Future.all(closedChannels).mapEmpty();
				return this.closeFuture;
			}
		}

		public @NotNull String userId() {
			return userId;
		}
	}
}
