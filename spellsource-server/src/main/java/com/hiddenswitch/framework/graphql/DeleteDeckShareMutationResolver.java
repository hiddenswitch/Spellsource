package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `DeckShare` using its globally unique id.
 */
public interface DeleteDeckShareMutationResolver {

    /**
     * Deletes a single `DeckShare` using its globally unique id.
     */
    DeleteDeckSharePayload deleteDeckShare(DeleteDeckShareInput input) throws Exception;

}
