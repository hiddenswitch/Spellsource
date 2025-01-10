package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `BotUser` using a unique key.
 */
public interface DeleteBotUserByIdMutationResolver {

    /**
     * Deletes a single `BotUser` using a unique key.
     */
    DeleteBotUserPayload deleteBotUserById(DeleteBotUserByIdInput input) throws Exception;

}
