package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `Deck` using its globally unique id.
 */
public interface DeleteDeckMutationResolver {

    /**
     * Deletes a single `Deck` using its globally unique id.
     */
    DeleteDeckPayload deleteDeck(DeleteDeckInput input) throws Exception;

}
