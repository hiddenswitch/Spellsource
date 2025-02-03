package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `Game` using its globally unique id.
 */
public interface DeleteGameMutationResolver {

    /**
     * Deletes a single `Game` using its globally unique id.
     */
    DeleteGamePayload deleteGame(DeleteGameInput input) throws Exception;

}
