package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `Deck` using its globally unique `ID`.
 */
public interface DeckQueryResolver {

    /**
     * Reads a single `Deck` using its globally unique `ID`.
     */
    Deck deck(String nodeId) throws Exception;

}
