package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `Game` using a unique key and a patch.
 */
public interface UpdateGameByIdMutationResolver {

    /**
     * Updates a single `Game` using a unique key and a patch.
     */
    UpdateGamePayload updateGameById(UpdateGameByIdInput input) throws Exception;

}
