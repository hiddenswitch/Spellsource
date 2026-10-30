package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Matchmaking;
import com.hiddenswitch.framework.graphql.*;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import org.apache.commons.lang3.NotImplementedException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import javax.naming.AuthenticationException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hiddenswitch.framework.Environment.withDslContext;
import static com.hiddenswitch.framework.Matchmaking.MATCHMAKING_WAKE;
import static com.hiddenswitch.framework.schema.spellsource.Tables.MATCHMAKING_TICKETS;

/**
 * GraphQL subscription resolver.
 * <p>
 * Note: The codegen generates {@code Future<T>} return types, but graphql-java subscriptions
 * require {@code Publisher<T>}. The {@code gameMessages()} method returns a {@code Publisher}
 * that the schema parser will accept because of the genericWrappers configuration.
 * For the remaining subscriptions, they return Future stubs until properly wired.
 */
public class GraphQLSubscriptionResolverImpl implements SubscriptionResolver, GraphQLSubscriptionResolver {

	/**
	 * Streams game messages for the authenticated user's active game.
	 * <p>
	 * This bridges the Vert.x event bus (where the game engine publishes
	 * ServerToClientMessage) to a reactive Publisher that graphql-java
	 * can use for subscription streaming.
	 */
	@Override
	public Publisher<ServerGameMessage> gameMessages(String connectionId) throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			throw new AuthenticationException("must be authenticated");
		}
		// Return the publisher directly - graphql-kickstart handles Publisher returns
		// from subscription resolvers natively.
		return GraphQLGameBridge.gameMessagesPublisher(userId, connectionId);
	}

	/**
	 * Emits a {@link MatchFound} when the matchmaking system creates a game for the
	 * authenticated user. Listens on the event bus address
	 * {@code matchmaking:enqueue:{userId}} which is published to by
	 * {@link Matchmaking#notifyGameReady(String, String)}.
	 */
	@Override
	public Publisher<MatchFound> matchFound() throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			throw new AuthenticationException("must be authenticated");
		}
		return new MatchFoundPublisher(userId);
	}

	@Override
	public Publisher<Friend> friendUpdated() throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public Publisher<Invite> inviteUpdated() throws Exception {
		throw new NotImplementedException();
	}

	@Override
	public Publisher<EditableCard> editableCardUpdated() throws Exception {
		throw new NotImplementedException();
	}

	/**
	 * A Publisher that listens on the event bus for matchmaking notifications and emits
	 * {@link MatchFound} when the user is matched into a game.
	 * <p>
	 * The event bus message body is the gameId string. The publisher replies to the
	 * event bus message to acknowledge receipt (the matchmaker waits for this reply
	 * before proceeding).
	 */
	private static class MatchFoundPublisher implements Publisher<MatchFound> {
		private final String userId;

		MatchFoundPublisher(String userId) {
			this.userId = userId;
		}

		@Override
		public void subscribe(Subscriber<? super MatchFound> subscriber) {
			var cancelled = new AtomicBoolean(false);
			var matched = new AtomicBoolean(false);
			var address = Matchmaking.MATCHMAKING_ENQUEUE + userId;
			var prepareAddress = Matchmaking.MATCHMAKING_PREPARE + userId;
			var vertx = Vertx.currentContext().owner();
			var eventBus = vertx.eventBus();

			MessageConsumer<String> consumer = eventBus.consumer(address);
			MessageConsumer<String> prepareConsumer = eventBus.consumer(prepareAddress);
			Runnable cleanup = () -> {
				if (cancelled.compareAndSet(false, true)) {
					if (!matched.get()) {
						Matchmaking.finishTicketSpanCancelled(userId, "cancelled");
					}
					Future.all(
							consumer.unregister(),
							prepareConsumer.unregister(),
							withDslContext(dsl -> dsl.deleteFrom(MATCHMAKING_TICKETS)
									.where(MATCHMAKING_TICKETS.USER_ID.eq(userId)))
									.otherwiseEmpty()
					);
				}
			};

			subscriber.onSubscribe(new Subscription() {
				@Override
				public void request(long n) {
					// backpressure not needed; match events are rare
				}

				@Override
				public void cancel() {
					cleanup.run();
				}
			});

			prepareConsumer.handler(message -> {
				if (cancelled.get()) {
					message.fail(1, "subscription cancelled");
					return;
				}
				message.reply("ok");
			});

			consumer.handler(message -> {
				if (cancelled.get()) {
					message.fail(1, "subscription cancelled");
					return;
				}
				matched.set(true);
				cleanup.run();

				var gameId = message.body();
				// Reply to acknowledge receipt so the matchmaker knows the player was notified
				message.reply("ok");

				var matchFound = MatchFound.builder()
						.setGameId(gameId)
						.setUrl("")
						.setPlayerKey("")
						.setPlayerSecret("")
						.build();

				subscriber.onNext(matchFound);
				subscriber.onComplete();
			});

			consumer.exceptionHandler(err -> {
				cleanup.run();
				subscriber.onError(err);
			});
		}
	}
}
