package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `CardsInDeck` using its globally unique id and a patch.
 */
public interface UpdateCardsInDeckMutationResolver {

    /**
     * Updates a single `CardsInDeck` using its globally unique id and a patch.
     */
    UpdateCardsInDeckPayload updateCardsInDeck(UpdateCardsInDeckInput input) throws Exception;

}
