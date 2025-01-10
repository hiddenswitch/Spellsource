package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `DeckPlayerAttributeTuple` using its globally unique id.
 */
public interface DeleteDeckPlayerAttributeTupleMutationResolver {

    /**
     * Deletes a single `DeckPlayerAttributeTuple` using its globally unique id.
     */
    DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTuple(DeleteDeckPlayerAttributeTupleInput input) throws Exception;

}
