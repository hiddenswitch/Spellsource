package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `BotUser` using its globally unique `ID`.
 */
public interface BotUserQueryResolver {

    /**
     * Reads a single `BotUser` using its globally unique `ID`.
     */
    BotUser botUser(String nodeId) throws Exception;

}
