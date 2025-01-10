package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `CardsInDeck` using a unique key.
 */
public interface DeleteCardsInDeckByIdMutationResolver {

    /**
     * Deletes a single `CardsInDeck` using a unique key.
     */
    DeleteCardsInDeckPayload deleteCardsInDeckById(DeleteCardsInDeckByIdInput input) throws Exception;

}
