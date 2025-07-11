package com.hiddenswitch.framework.graphql;


public interface MutationResolver {

    io.vertx.core.Future<RogueRun> startRogueRun(String heroClass, java.lang.Long seed) throws Exception;

    io.vertx.core.Future<RogueRun> makeRogueChoice(java.lang.Long choiceId, java.util.List<Integer> choices) throws Exception;

}
