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
		return RogueManager.startRogueRun(heroClass, seed, Accounts.userId()).map(RogueRun::new);
	}

	@Override
	public Future<RogueRun> makeRogueChoice(Long choiceId, List<Integer> choices) throws Exception {
		return RogueManager.makeRogueChoice(choiceId, choices).map(RogueRun::new);
	}

	@Override
	public Future<RogueRun> reroll(Long choiceId) throws Exception {
		return RogueManager.reroll(choiceId).map(RogueRun::new);
	}

	@Override
	public Future<RogueRun> trashCard(Long rogueId, String cardId) throws Exception {
		return RogueManager.trashCard(rogueId, cardId).map(RogueRun::new);
	}

	@Override
	public Future<RogueRun> skipBoss(Long rogueId) throws Exception {
		return RogueManager.skipBoss(rogueId).map(r -> new com.hiddenswitch.framework.graphql.RogueRun(r.getId()));
	}

	@Override
	public Future<RogueRun> upgradeCard(Long rogueId, String cardId) throws Exception {
		return RogueManager.upgradeCard(rogueId, cardId).map(RogueRun::new);
	}
}