package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `BotUser` using its globally unique id and a patch.
 */
public interface UpdateBotUserMutationResolver {

    /**
     * Updates a single `BotUser` using its globally unique id and a patch.
     */
    UpdateBotUserPayload updateBotUser(UpdateBotUserInput input) throws Exception;

}
