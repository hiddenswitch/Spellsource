package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `CardsInDeck` using a unique key and a patch.
 */
public interface UpdateCardsInDeckByIdMutationResolver {

    /**
     * Updates a single `CardsInDeck` using a unique key and a patch.
     */
    UpdateCardsInDeckPayload updateCardsInDeckById(UpdateCardsInDeckByIdInput input) throws Exception;

}
