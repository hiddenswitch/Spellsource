package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `GameUser` using a unique key and a patch.
 */
public interface UpdateGameUserByGameIdAndUserIdMutationResolver {

    /**
     * Updates a single `GameUser` using a unique key and a patch.
     */
    UpdateGameUserPayload updateGameUserByGameIdAndUserId(UpdateGameUserByGameIdAndUserIdInput input) throws Exception;

}
