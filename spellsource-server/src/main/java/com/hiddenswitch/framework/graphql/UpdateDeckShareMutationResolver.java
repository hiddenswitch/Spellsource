package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `DeckShare` using its globally unique id and a patch.
 */
public interface UpdateDeckShareMutationResolver {

    /**
     * Updates a single `DeckShare` using its globally unique id and a patch.
     */
    UpdateDeckSharePayload updateDeckShare(UpdateDeckShareInput input) throws Exception;

}
