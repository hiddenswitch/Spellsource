package com.hiddenswitch.framework.graphql;


/**
 * Creates a single `GameUser`.
 */
public interface CreateGameUserMutationResolver {

    /**
     * Creates a single `GameUser`.
     */
    CreateGameUserPayload createGameUser(CreateGameUserInput input) throws Exception;

}
