package com.hiddenswitch.framework.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Matchmaking;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import com.hiddenswitch.spellsource.rpc.VertxMatchmakingGrpc;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Metrics;
import io.vertx.core.*;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hiddenswitch.framework.Environment.withDslContext;
import static com.hiddenswitch.framework.Matchmaking.*;
import static com.hiddenswitch.framework.schema.spellsource.Tables.MATCHMAKING_TICKETS;
import static io.vertx.core.CompositeFuture.join;

public class ClientMatchmakingService extends VertxMatchmakingGrpc.MatchmakingVertxImplBase implements Closeable {
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
	public void enqueue(ReadStream<Spellsource.MatchmakingQueuePutRequest> request, WriteStream<Spellsource.MatchmakingQueuePutResponse> response) {
		var userId = Accounts.userId();
		Objects.requireNonNull(userId);

		var vertx = Vertx.currentContext().owner();
		var eventBus = vertx.eventBus();
		var timer = MATCHMAKING_TICKETS_DURATION.start();
		var channels = new ArrayList<MessageConsumer>();
		var session = new Session(response, userId, channels, timer, new AtomicBoolean(false));
		sessions.add(session);

		LOGGER.trace("enqueue called userId={}", userId);

		// notify the user when a game has been assigned
		channels.add(eventBus.<String>consumer(MATCHMAKING_ENQUEUE + userId)
				.handler(message -> {
					var gameId = message.body();
					writeGameId(response, gameId);
					message.reply("ok");
					close(session);
				}));

		request.handler(message -> {
			LOGGER.trace("client matchmaking service did get message (has deck id? {} cancel={}) for userId={}", !Strings.isNullOrEmpty(message.getDeckId()), message.getCancel(), userId);
			if (session.cancelled.get()) {
				return;
			}

			if (message.getCancel()) {
				close(session);
				return;
			}

			Games.getGameId(userId)
					.compose(existingGameId -> {
						if (existingGameId != null) {
							return Matchmaking.writeGameId(response, existingGameId)
									.onComplete(v -> close(session));
						}

						var queueId = message.getQueueId();
						var queueClosedChannel = eventBus.<String>consumer(MATCHMAKING_QUEUE_CLOSED + queueId);
						queueClosedChannel.handler(v2 -> close(session));
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
					.onFailure(t -> close(session));
		});
		response.exceptionHandler(v -> close(session));
		// handle the request messages, which are queue tickets
		request.exceptionHandler(v -> close(session));
		request.endHandler(v -> close(session));
	}


	private Future<Void> close(Session session) {
		session.cancelled.set(true);
		if (!this.sessions.remove(session)) {
			// already removed this session
			return Future.succeededFuture();
		}

		var closedChannels = new ArrayList<Future>();
		for (var channel : session.channels) {
			closedChannels.add(channel.unregister());
		}

		session.channels.clear();
		var deletedTickets = withDslContext(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS).where(MATCHMAKING_TICKETS.USER_ID.eq(session.userId)));
		var closedResponse = session.response.end();
		session.timer.stop();

		return CompositeFuture.all(Lists.newArrayList(Iterables.concat(closedChannels, List.of(deletedTickets, closedResponse)))).mapEmpty();
	}

	@Override
	public void close(Promise<Void> completion) {
		// interrupt all the threads, runClientEnqueue will handle it appropriately
		var closeAll = new ArrayList<Future>();
		for (var session : sessions) {
			closeAll.add(close(session));
		}

		join(closeAll).onComplete(v -> completion.complete());
	}

	record Session(WriteStream<Spellsource.MatchmakingQueuePutResponse> response,
	               @NotNull String userId,
	               List<MessageConsumer> channels,
	               LongTaskTimer.Sample timer,
	               AtomicBoolean cancelled) {
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
	}
}
