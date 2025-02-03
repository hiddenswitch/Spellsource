package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `Deck` using a unique key and a patch.
 */
public interface UpdateDeckByIdMutationResolver {

    /**
     * Updates a single `Deck` using a unique key and a patch.
     */
    UpdateDeckPayload updateDeckById(UpdateDeckByIdInput input) throws Exception;

}
