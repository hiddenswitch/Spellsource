package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `GameUser` using a unique key.
 */
public interface DeleteGameUserByGameIdAndUserIdMutationResolver {

    /**
     * Deletes a single `GameUser` using a unique key.
     */
    DeleteGameUserPayload deleteGameUserByGameIdAndUserId(DeleteGameUserByGameIdAndUserIdInput input) throws Exception;

}
