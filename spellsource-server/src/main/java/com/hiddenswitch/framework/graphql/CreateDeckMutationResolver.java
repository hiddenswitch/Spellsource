package com.hiddenswitch.framework.graphql;


/**
 * Creates a single `Deck`.
 */
public interface CreateDeckMutationResolver {

    /**
     * Creates a single `Deck`.
     */
    CreateDeckPayload createDeck(CreateDeckInput input) throws Exception;

}
