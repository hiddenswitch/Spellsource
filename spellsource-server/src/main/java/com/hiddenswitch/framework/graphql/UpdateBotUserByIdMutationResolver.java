package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `BotUser` using a unique key and a patch.
 */
public interface UpdateBotUserByIdMutationResolver {

    /**
     * Updates a single `BotUser` using a unique key and a patch.
     */
    UpdateBotUserPayload updateBotUserById(UpdateBotUserByIdInput input) throws Exception;

}
