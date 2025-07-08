package com.hiddenswitch.framework.graphql;


public interface QueryResolver {

    io.vertx.core.Future<String> currentUserId() throws Exception;

}
