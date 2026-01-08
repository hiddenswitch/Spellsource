package com.hiddenswitch.framework.graphql;


public interface MutationResolver {

    io.vertx.core.Future<RogueRun> startRogueRun(String heroClass, java.lang.Long seed) throws Exception;

    io.vertx.core.Future<RogueRun> makeRogueChoice(java.lang.Long choiceId, java.util.List<Integer> choices) throws Exception;

    io.vertx.core.Future<RogueRun> reroll(java.lang.Long choiceId) throws Exception;

    io.vertx.core.Future<RogueRun> trashCard(java.lang.Long rogueId, String cardId) throws Exception;

    io.vertx.core.Future<RogueRun> skipBoss(java.lang.Long rogueId) throws Exception;

    io.vertx.core.Future<RogueRun> upgradeCard(java.lang.Long rogueId, String cardId) throws Exception;

}
