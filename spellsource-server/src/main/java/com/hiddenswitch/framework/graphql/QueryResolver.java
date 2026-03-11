package com.hiddenswitch.framework.graphql;


public interface QueryResolver {

    io.vertx.core.Future<String> currentUserId() throws Exception;

    io.vertx.core.Future<java.util.List<String>> currentRogueClasses() throws Exception;

    io.vertx.core.Future<Integer> rerollCost(java.lang.Long rogueId) throws Exception;

    io.vertx.core.Future<Integer> trashCardCost(java.lang.Long rogueId, String cardId) throws Exception;

    io.vertx.core.Future<Integer> upgradeCardCost(java.lang.Long rogueId, String cardId) throws Exception;

}
