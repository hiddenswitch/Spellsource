package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Matchmaking;
import com.hiddenswitch.framework.graphql.*;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import org.apache.commons.lang3.NotImplementedException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import javax.naming.AuthenticationException;
import java.util.concurrent.atomic.AtomicBoolean;

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
	public Publisher<ServerGameMessage> gameMessages() throws Exception {
		var userId = Accounts.userId();
		if (userId == null) {
			throw new AuthenticationException("must be authenticated");
		}
		// Return the publisher directly - graphql-kickstart handles Publisher returns
		// from subscription resolvers natively.
		return GraphQLGameBridge.gameMessagesPublisher(userId);
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
			var address = Matchmaking.MATCHMAKING_ENQUEUE + userId;
			var vertx = Vertx.currentContext().owner();
			var eventBus = vertx.eventBus();

			MessageConsumer<String> consumer = eventBus.consumer(address);

			subscriber.onSubscribe(new Subscription() {
				@Override
				public void request(long n) {
					// backpressure not needed; match events are rare
				}

				@Override
				public void cancel() {
					cancelled.set(true);
					consumer.unregister();
				}
			});

			consumer.handler(message -> {
				if (cancelled.get()) return;

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
			});

			consumer.exceptionHandler(err -> {
				if (!cancelled.get()) {
					subscriber.onError(err);
				}
			});
		}
	}
}
