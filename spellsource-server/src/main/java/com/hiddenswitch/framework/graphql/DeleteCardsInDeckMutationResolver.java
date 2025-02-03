package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `CardsInDeck` using its globally unique id.
 */
public interface DeleteCardsInDeckMutationResolver {

    /**
     * Deletes a single `CardsInDeck` using its globally unique id.
     */
    DeleteCardsInDeckPayload deleteCardsInDeck(DeleteCardsInDeckInput input) throws Exception;

}
