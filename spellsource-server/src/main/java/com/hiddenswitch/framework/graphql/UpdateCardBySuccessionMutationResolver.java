package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `Card` using a unique key and a patch.
 */
public interface UpdateCardBySuccessionMutationResolver {

    /**
     * Updates a single `Card` using a unique key and a patch.
     */
    UpdateCardPayload updateCardBySuccession(UpdateCardBySuccessionInput input) throws Exception;

}
