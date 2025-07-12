package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.graphql.MutationResolver;
import com.hiddenswitch.framework.graphql.RogueRun;
import graphql.kickstart.tools.GraphQLMutationResolver;
import io.vertx.core.Future;

import java.util.List;

public class GraphQLMutationResolverImpl implements MutationResolver, GraphQLMutationResolver {

	@Override
	public Future<RogueRun> startRogueRun(String heroClass, Long seed) throws Exception {
		return RogueManager.startRogueRun(heroClass, seed, Accounts.userId()).compose(id -> Future.succeededFuture(new RogueRun(id)));
	}

	@Override
	public Future<RogueRun> makeRogueChoice(Long choiceId, List<Integer> choices) throws Exception {
		return RogueManager.makeRogueChoice(choiceId, choices).compose(id -> Future.succeededFuture(new RogueRun(id)));
	}
}