package com.hiddenswitch.framework.graphql;


/**
 * Get a single `Deck`.
 */
public interface DeckByIdQueryResolver {

    /**
     * Get a single `Deck`.
     */
    Deck deckById(String id) throws Exception;

}
