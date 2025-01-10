package com.hiddenswitch.framework.graphql;


/**
 * Get a single `BotUser`.
 */
public interface BotUserByIdQueryResolver {

    /**
     * Get a single `BotUser`.
     */
    BotUser botUserById(String id) throws Exception;

}
