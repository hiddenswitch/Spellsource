package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `DeckShare` using its globally unique `ID`.
 */
public interface DeckShareQueryResolver {

    /**
     * Reads a single `DeckShare` using its globally unique `ID`.
     */
    DeckShare deckShare(String nodeId) throws Exception;

}
