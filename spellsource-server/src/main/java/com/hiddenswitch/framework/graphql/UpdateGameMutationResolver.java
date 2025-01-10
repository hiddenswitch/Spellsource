package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `Game` using its globally unique id and a patch.
 */
public interface UpdateGameMutationResolver {

    /**
     * Updates a single `Game` using its globally unique id and a patch.
     */
    UpdateGamePayload updateGame(UpdateGameInput input) throws Exception;

}
