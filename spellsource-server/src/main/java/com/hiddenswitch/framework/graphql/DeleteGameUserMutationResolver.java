package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `GameUser` using its globally unique id.
 */
public interface DeleteGameUserMutationResolver {

    /**
     * Deletes a single `GameUser` using its globally unique id.
     */
    DeleteGameUserPayload deleteGameUser(DeleteGameUserInput input) throws Exception;

}
