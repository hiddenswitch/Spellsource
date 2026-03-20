package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.graphql.*;
import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import io.vertx.core.Future;
import org.apache.commons.lang3.NotImplementedException;
import org.reactivestreams.Publisher;

import javax.naming.AuthenticationException;

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

	@Override
	public Publisher<MatchFound> matchFound() throws Exception {
		throw new NotImplementedException();
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
}
