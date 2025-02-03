package com.hiddenswitch.framework.graphql;


/**
 * Creates a single `DeckShare`.
 */
public interface CreateDeckShareMutationResolver {

    /**
     * Creates a single `DeckShare`.
     */
    CreateDeckSharePayload createDeckShare(CreateDeckShareInput input) throws Exception;

}
