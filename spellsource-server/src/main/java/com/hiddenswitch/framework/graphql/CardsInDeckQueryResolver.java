package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `CardsInDeck` using its globally unique `ID`.
 */
public interface CardsInDeckQueryResolver {

    /**
     * Reads a single `CardsInDeck` using its globally unique `ID`.
     */
    CardsInDeck cardsInDeck(String nodeId) throws Exception;

}
