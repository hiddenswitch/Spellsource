package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `DeckPlayerAttributeTuple` using a unique key and a patch.
 */
public interface UpdateDeckPlayerAttributeTupleByIdMutationResolver {

    /**
     * Updates a single `DeckPlayerAttributeTuple` using a unique key and a patch.
     */
    UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTupleById(UpdateDeckPlayerAttributeTupleByIdInput input) throws Exception;

}
