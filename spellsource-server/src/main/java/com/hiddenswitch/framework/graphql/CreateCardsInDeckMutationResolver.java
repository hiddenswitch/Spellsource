package com.hiddenswitch.framework.graphql;


/**
 * Creates a single `CardsInDeck`.
 */
public interface CreateCardsInDeckMutationResolver {

    /**
     * Creates a single `CardsInDeck`.
     */
    CreateCardsInDeckPayload createCardsInDeck(CreateCardsInDeckInput input) throws Exception;

}
