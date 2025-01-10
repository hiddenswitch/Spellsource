package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `Deck` using its globally unique id and a patch.
 */
public interface UpdateDeckMutationResolver {

    /**
     * Updates a single `Deck` using its globally unique id and a patch.
     */
    UpdateDeckPayload updateDeck(UpdateDeckInput input) throws Exception;

}
