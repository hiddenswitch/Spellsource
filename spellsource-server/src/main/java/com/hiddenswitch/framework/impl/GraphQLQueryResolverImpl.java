package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.graphql.QueryResolver;
import graphql.kickstart.tools.GraphQLQueryResolver;
import io.vertx.core.Future;

public class GraphQLQueryResolverImpl implements QueryResolver, GraphQLQueryResolver {

	@Override
	public Future<String> currentUserId() throws Exception {
		return Future.succeededFuture(Accounts.userId());
	}

	@Override
	public Future<Integer> rerollCost(Long rogueId) throws Exception {
		return RogueManager.rerollCost(rogueId);
	}
}