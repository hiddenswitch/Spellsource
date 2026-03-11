package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.graphql.QueryResolver;
import graphql.kickstart.tools.GraphQLQueryResolver;
import io.vertx.core.Future;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.util.List;

public class GraphQLQueryResolverImpl implements QueryResolver, GraphQLQueryResolver {

	@Override
	public Future<String> currentUserId() throws Exception {
		return Future.succeededFuture(Accounts.userId());
	}

	@Override
	public Future<Integer> rerollCost(Long rogueId) throws Exception {
		return RogueManager.rerollCost(rogueId);
	}

	@Override
	public Future<Integer> trashCardCost(Long rogueId, String cardId) throws Exception {
		return Future.succeededFuture(1);
	}

	@Override
	public Future<Integer> upgradeCardCost(Long rogueId, String cardId) throws Exception {
		return RogueManager.upgradeCardCost(rogueId, cardId);
	}

	@Override
	public Future<List<String>> currentRogueClasses() throws Exception {
		return Future.succeededFuture(List.of(HeroClass.COPPER, HeroClass.TOAST));
	}
}