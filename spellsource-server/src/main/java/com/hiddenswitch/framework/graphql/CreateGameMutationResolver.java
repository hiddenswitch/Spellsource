package com.hiddenswitch.framework.graphql;


/**
 * Creates a single `Game`.
 */
public interface CreateGameMutationResolver {

    /**
     * Creates a single `Game`.
     */
    CreateGamePayload createGame(CreateGameInput input) throws Exception;

}
