package com.hiddenswitch.framework.graphql;


/**
 * Creates a single `BotUser`.
 */
public interface CreateBotUserMutationResolver {

    /**
     * Creates a single `BotUser`.
     */
    CreateBotUserPayload createBotUser(CreateBotUserInput input) throws Exception;

}
