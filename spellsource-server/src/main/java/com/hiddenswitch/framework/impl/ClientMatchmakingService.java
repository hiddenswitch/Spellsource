package com.hiddenswitch.framework.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Matchmaking;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import com.hiddenswitch.spellsource.rpc.VertxMatchmakingGrpcServer;
import io.grpc.internal.KeepAliveManager;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Metrics;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.grpc.server.GrpcServerRequest;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static com.hiddenswitch.framework.Environment.withDslContext;
import static com.hiddenswitch.framework.Matchmaking.*;
import static com.hiddenswitch.framework.schema.spellsource.Tables.MATCHMAKING_TICKETS;
import static io.vertx.core.CompositeFuture.join;

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
	private final Set<Session> sessions = new HashSet<>();

	public ClientMatchmakingService() {
	}

	@Override
	public void enqueue(GrpcServerRequest<Spellsource.MatchmakingQueuePutRequest, Spellsource.MatchmakingQueuePutResponse> grpcServerRequest, ReadStream<Spellsource.MatchmakingQueuePutRequest> request, WriteStream<Spellsource.MatchmakingQueuePutResponse> response) {
		var userId = grpcServerRequest.routingContext().user().subject();
		Objects.requireNonNull(userId);

		var vertx = Vertx.currentContext().owner();
		var eventBus = vertx.eventBus();
		var timer = MATCHMAKING_TICKETS_DURATION.start();
		var channels = new ArrayList<MessageConsumer>();
		var session = new Session(sessions::remove, response, userId, channels, timer, new AtomicBoolean(false), grpcServerRequest.connection());
		sessions.add(session);
		session.keepAliveManager().onTransportStarted();

		LOGGER.trace("enqueue called userId={}", userId);

		// notify the user when a game has been assigned
		channels.add(eventBus.<String>consumer(MATCHMAKING_ENQUEUE + userId)
				.handler(message -> {
					var gameId = message.body();
					writeGameId(response, gameId);
					message.reply("ok");
					session.close();
				}));

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
						if (existingGameId != null) {
							return Matchmaking.writeGameId(response, existingGameId)
									.onComplete(v -> session.close());
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

						return withDslContext(dsl -> dsl
								.insertInto(MATCHMAKING_TICKETS)
								.set(record)
								.onDuplicateKeyUpdate()
								.set(record))
								.mapEmpty();
					})
					.onFailure(t -> session.close());
		});
		response.exceptionHandler(v -> session.close());
		// handle the request messages, which are queue tickets
		request.exceptionHandler(v -> session.close());
		request.endHandler(v -> session.close());
	}


	@Override
	public void close(Promise<Void> completion) {
		// interrupt all the threads, runClientEnqueue will handle it appropriately
		var closeAll = new ArrayList<Future>();
		for (var session : sessions) {
			closeAll.add(session.close());
		}

		join(closeAll).onComplete(v -> completion.complete());
	}

	public static final class Session {
		private final Function<Session, Boolean> remover;
		private final WriteStream<Spellsource.MatchmakingQueuePutResponse> response;
		private final @NotNull String userId;
		private final List<MessageConsumer> channels;
		private final LongTaskTimer.Sample timer;
		private final AtomicBoolean cancelled;
		private final KeepAliveManager keepAliveManager;

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
			this.keepAliveManager = Environment.keepAliveManager(connection, v -> close(), true);
		}

		public KeepAliveManager keepAliveManager() {
			return keepAliveManager;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Session session = (Session) o;
			return userId.equals(session.userId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(userId);
		}

		public Future<Void> close() {
			this.cancelled.set(true);
			if (!this.remover.apply(this)) {
				// already removed this session
				return Future.succeededFuture();
			}

			var closedChannels = new ArrayList<Future>();
			for (var channel : this.channels) {
				closedChannels.add(channel.unregister());
			}

			this.channels.clear();
			var deletedTickets = withDslContext(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS).where(MATCHMAKING_TICKETS.USER_ID.eq(this.userId)));
			var closedResponse = this.response.end();
			this.timer.stop();
			keepAliveManager.onTransportTermination();
			return CompositeFuture.all(Lists.newArrayList(Iterables.concat(closedChannels, List.of(deletedTickets, closedResponse)))).mapEmpty();
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
