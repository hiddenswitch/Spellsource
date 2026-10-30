package com.hiddenswitch.framework.impl;

import com.google.common.base.Strings;
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
import java.util.function.Function;
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

	@Override
	public void enqueue(GrpcServerRequest<Spellsource.MatchmakingQueuePutRequest, Spellsource.MatchmakingQueuePutResponse> grpcServerRequest, ReadStream<Spellsource.MatchmakingQueuePutRequest> request, WriteStream<Spellsource.MatchmakingQueuePutResponse> response) {
		var userId = grpcServerRequest.routingContext().user().subject();
		Objects.requireNonNull(userId);

		var vertx = Vertx.currentContext().owner();
		var eventBus = vertx.eventBus();
		var timer = MATCHMAKING_TICKETS_DURATION.start();
		var channels = Collections.synchronizedList(new ArrayList<MessageConsumer>());
		var session = new Session(s -> {
			activeSessionsByUser.remove(s.userId(), s);
			return sessions.remove(s);
		}, response, userId, channels, timer, new AtomicBoolean(false), grpcServerRequest.connection());
		sessions.add(session);
		var previousSession = activeSessionsByUser.put(userId, session);
		if (previousSession != null && previousSession != session) {
			session.chainInitialWrite(previousSession.close().otherwiseEmpty());
		}
		session.keepAliveManager().onTransportStarted();

		LOGGER.trace("enqueue called userId={}", userId);

		var prepareConsumer = eventBus.<String>consumer(MATCHMAKING_PREPARE + userId);
		prepareConsumer.handler(message -> {
			session.verifyAlive(vertx)
					.onSuccess(v -> message.reply("ok"))
					.onFailure(err -> message.fail(1, err.getMessage() != null ? err.getMessage() : "session disconnected"));
		});
		channels.add(prepareConsumer);

		var requeueConsumer = eventBus.<JsonObject>consumer(MATCHMAKING_REQUEUE + userId);
		requeueConsumer.handler(message -> {
			var body = message.body();
			session.requeueTicket(
							body.getLong("ticketId"),
							body.getString("queueId"),
							body.getString("deckId"),
							body.getString("botDeckId"))
					.onSuccess(v -> {
						eventBus.publish(MATCHMAKING_WAKE, body.getString("queueId"));
						message.reply("ok");
					})
					.onFailure(err -> message.fail(1, err.getMessage() != null ? err.getMessage() : "requeue failed"));
		});
		channels.add(requeueConsumer);

		// notify the user when a game has been assigned
		var enqueueConsumer = eventBus.<String>consumer(MATCHMAKING_ENQUEUE + userId);
		enqueueConsumer.handler(message -> {
			if (session.cancelled.get()) {
				message.fail(1, "session already closed");
				return;
			}
			var gameId = message.body();
			session.matched.set(true);
			writeGameId(response, gameId)
					.onSuccess(v -> {
						session.close();
						message.reply("ok");
					})
					.onFailure(err -> {
						session.matched.set(false);
						session.close();
						message.fail(1, err.getMessage() != null ? err.getMessage() : "failed to write gameId");
					});
		});
		channels.add(enqueueConsumer);

		var consumersReady = Future.all(
				prepareConsumer.completion(),
				requeueConsumer.completion(),
				enqueueConsumer.completion()
		).mapEmpty().otherwiseEmpty();

		request.handler(message -> {
			session.keepAliveManager().onDataReceived();
			LOGGER.trace("client matchmaking service did get message (has deck id? {} cancel={}) for userId={}", !Strings.isNullOrEmpty(message.getDeckId()), message.getCancel(), userId);
			if (session.cancelled.get()) {
				return;
			}

			if (message.getCancel()) {
				session.close();
				return;
			}

			Games.getGameId(userId)
					.compose(existingGameId -> {
						if (session.cancelled.get()) return Future.succeededFuture();
						if (existingGameId != null) {
							session.matched.set(true);
							return Matchmaking.writeGameId(response, existingGameId)
									.eventually(() -> session.close());
						}

						var queueId = message.getQueueId();
						var queueClosedChannel = eventBus.<String>consumer(MATCHMAKING_QUEUE_CLOSED + queueId);
						queueClosedChannel.handler(v2 -> session.close());
						session.channels.add(queueClosedChannel);

						var record = MATCHMAKING_TICKETS.newRecord()
								.setUserId(userId)
								.setDeckId(message.getDeckId())
								// protobufs always give non-null empty strings but sql expects null, "" is a valid identifier
								.setBotDeckId(message.getBotDeckId().isEmpty() ? null : message.getBotDeckId())
								.setQueueId(queueId);

						return consumersReady.compose(ignored -> session.enqueueTicketWrite(() -> withDslContext(dsl -> dsl
								.insertInto(MATCHMAKING_TICKETS)
								.set(record)
								.onDuplicateKeyUpdate()
								.set(record))
								.onSuccess(v -> eventBus.publish(MATCHMAKING_WAKE, queueId))));
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
		private final Function<Session, Boolean> remover;
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

		Session(Function<Session, Boolean> remover,
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

		synchronized void chainInitialWrite(Future<?> prev) {
			this.ticketWrite = this.ticketWrite.mapEmpty().otherwiseEmpty().compose(ignored -> prev);
		}

		public synchronized Future<Void> enqueueTicketWrite(Supplier<Future<?>> writeSupplier) {
			if (this.cancelled.get() || this.matched.get()) {
				return Future.failedFuture("session closed or matched");
			}
			this.ticketWrite = this.ticketWrite.mapEmpty().otherwiseEmpty()
					.compose(ignored -> {
						synchronized (Session.this) {
							if (this.cancelled.get() || this.matched.get()) {
								return Future.failedFuture("session closed or matched");
							}
						}
						return writeSupplier.get();
					});
			return this.ticketWrite.mapEmpty();
		}

		public Future<Void> requeueTicket(Long ticketId, String queueId, String deckId, String botDeckId) {
			return enqueueTicketWrite(() -> Matchmaking.executeRequeueTicket(this.userId, ticketId, queueId, deckId, botDeckId));
		}

		private boolean isResponseDead() {
			if (this.response instanceof GrpcWriteStreamBase<?, ?> grpcWriteStream) {
				return grpcWriteStream.isCancelled() || grpcWriteStream.isTrailersSent() || grpcWriteStream.hasError();
			}
			return false;
		}

		public Future<Void> verifyAlive(Vertx vertx) {
			synchronized (this) {
				if (this.cancelled.get() || this.matched.get()) {
					return Future.failedFuture("session closed or matched");
				}
			}
			if (isResponseDead()) {
				this.close();
				return Future.failedFuture("grpc response stream closed");
			}
			if (this.connection == null) {
				return Future.succeededFuture();
			}
			Future<Buffer> pingFuture;
			try {
				pingFuture = this.connection.ping(Buffer.buffer(new byte[]{1, 2, 3, 4, 5, 6, 7, 8}));
			} catch (UnsupportedOperationException e) {
				return Future.succeededFuture();
			} catch (Throwable t) {
				this.close();
				return Future.failedFuture(t);
			}
			pingFuture = pingFuture.recover(err -> {
				if (err instanceof UnsupportedOperationException) {
					return Future.succeededFuture(Buffer.buffer());
				}
				return Future.failedFuture(err);
			});
			Future<Void> pingCheck = any(pingFuture.mapEmpty(), this.closePromise.future())
					.compose(cf -> {
						if (this.cancelled.get() || this.matched.get() || isResponseDead()) {
							return Future.failedFuture("session closed during ping");
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
				this.closePromise.tryFail("session closed");
				if (this.closeFuture != null) {
					return this.closeFuture;
				}
				if (!this.remover.apply(this)) {
					this.closeFuture = Future.succeededFuture();
					return this.closeFuture;
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

				// A disconnect can arrive while the database insert is in flight.
				// Delete after that write settles so it cannot recreate a cancelled ticket,
				// and end the response stream after the ticket is deleted so callers awaiting
				// stream end observe the ticket already removed.
				Future<?> cleanupTicket;
				if (this.matched.get()) {
					cleanupTicket = pendingTicketWrite.mapEmpty().otherwiseEmpty();
				} else {
					cleanupTicket = pendingTicketWrite.mapEmpty().otherwiseEmpty()
							.compose(ignored -> withDslContext(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS)
									.where(MATCHMAKING_TICKETS.USER_ID.eq(this.userId))));
				}
				var closedResponse = cleanupTicket.eventually(() -> {
					try {
						return this.response.end().otherwiseEmpty();
					} catch (IllegalStateException ignored) {
						return Future.succeededFuture();
					}
				});
				closedChannels.add(closedResponse);
				this.closeFuture = Future.all(closedChannels).mapEmpty();
				return this.closeFuture;
			}
		}

		public Function<Session, Boolean> remover() {
			return remover;
		}

		public WriteStream<Spellsource.MatchmakingQueuePutResponse> response() {
			return response;
		}

		public @NotNull String userId() {
			return userId;
		}

		public List<MessageConsumer> channels() {
			return channels;
		}

		public LongTaskTimer.Sample timer() {
			return timer;
		}

		public AtomicBoolean cancelled() {
			return cancelled;
		}

		@Override
		public String toString() {
			return "Session[" +
					"remover=" + remover + ", " +
					"response=" + response + ", " +
					"userId=" + userId + ", " +
					"channels=" + channels + ", " +
					"timer=" + timer + ", " +
					"cancelled=" + cancelled + ']';
		}

	}
}
