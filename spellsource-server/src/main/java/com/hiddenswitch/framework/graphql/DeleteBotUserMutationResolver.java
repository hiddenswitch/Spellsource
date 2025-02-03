package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `BotUser` using its globally unique id.
 */
public interface DeleteBotUserMutationResolver {

    /**
     * Deletes a single `BotUser` using its globally unique id.
     */
    DeleteBotUserPayload deleteBotUser(DeleteBotUserInput input) throws Exception;

}
