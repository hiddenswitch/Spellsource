package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `DeckShare` using a unique key.
 */
public interface DeleteDeckShareByDeckIdAndShareRecipientIdMutationResolver {

    /**
     * Deletes a single `DeckShare` using a unique key.
     */
    DeleteDeckSharePayload deleteDeckShareByDeckIdAndShareRecipientId(DeleteDeckShareByDeckIdAndShareRecipientIdInput input) throws Exception;

}
