package com.hiddenswitch.framework.graphql;


/**
 * Get a single `CardsInDeck`.
 */
public interface CardsInDeckByIdQueryResolver {

    /**
     * Get a single `CardsInDeck`.
     */
    CardsInDeck cardsInDeckById(String id) throws Exception;

}
