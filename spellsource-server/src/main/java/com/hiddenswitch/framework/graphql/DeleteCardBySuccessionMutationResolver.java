package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `Card` using a unique key.
 */
public interface DeleteCardBySuccessionMutationResolver {

    /**
     * Deletes a single `Card` using a unique key.
     */
    DeleteCardPayload deleteCardBySuccession(DeleteCardBySuccessionInput input) throws Exception;

}
