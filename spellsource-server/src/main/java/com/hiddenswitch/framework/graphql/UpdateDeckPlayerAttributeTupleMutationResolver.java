package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `DeckPlayerAttributeTuple` using its globally unique id and a patch.
 */
public interface UpdateDeckPlayerAttributeTupleMutationResolver {

    /**
     * Updates a single `DeckPlayerAttributeTuple` using its globally unique id and a patch.
     */
    UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTuple(UpdateDeckPlayerAttributeTupleInput input) throws Exception;

}
