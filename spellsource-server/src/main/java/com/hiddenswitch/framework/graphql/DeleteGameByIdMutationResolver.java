package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `Game` using a unique key.
 */
public interface DeleteGameByIdMutationResolver {

    /**
     * Deletes a single `Game` using a unique key.
     */
    DeleteGamePayload deleteGameById(DeleteGameByIdInput input) throws Exception;

}
