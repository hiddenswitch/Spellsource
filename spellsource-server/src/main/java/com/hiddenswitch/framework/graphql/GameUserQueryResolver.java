package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `GameUser` using its globally unique `ID`.
 */
public interface GameUserQueryResolver {

    /**
     * Reads a single `GameUser` using its globally unique `ID`.
     */
    GameUser gameUser(String nodeId) throws Exception;

}
