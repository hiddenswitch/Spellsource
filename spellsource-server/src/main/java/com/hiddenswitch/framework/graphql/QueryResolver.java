package com.hiddenswitch.framework.graphql;


public interface QueryResolver {

    io.vertx.core.Future<String> currentUserId() throws Exception;

    io.vertx.core.Future<Integer> rerollCost(java.lang.Long rogueId) throws Exception;

}
