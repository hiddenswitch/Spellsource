package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `GameUser` using its globally unique id and a patch.
 */
public interface UpdateGameUserMutationResolver {

    /**
     * Updates a single `GameUser` using its globally unique id and a patch.
     */
    UpdateGameUserPayload updateGameUser(UpdateGameUserInput input) throws Exception;

}
