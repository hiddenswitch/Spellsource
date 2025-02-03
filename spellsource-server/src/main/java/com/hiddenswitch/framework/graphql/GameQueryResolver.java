package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `Game` using its globally unique `ID`.
 */
public interface GameQueryResolver {

    /**
     * Reads a single `Game` using its globally unique `ID`.
     */
    Game game(String nodeId) throws Exception;

}
