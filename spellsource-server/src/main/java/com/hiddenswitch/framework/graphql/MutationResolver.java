package com.hiddenswitch.framework.graphql;


public interface MutationResolver {

    io.vertx.core.Future<String> startRogueRun(String heroClass, String seed) throws Exception;

}
