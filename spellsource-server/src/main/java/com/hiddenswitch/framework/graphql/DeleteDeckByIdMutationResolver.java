package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `Deck` using a unique key.
 */
public interface DeleteDeckByIdMutationResolver {

    /**
     * Deletes a single `Deck` using a unique key.
     */
    DeleteDeckPayload deleteDeckById(DeleteDeckByIdInput input) throws Exception;

}
