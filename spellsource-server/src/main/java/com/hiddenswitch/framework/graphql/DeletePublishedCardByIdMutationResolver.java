package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `PublishedCard` using a unique key.
 */
public interface DeletePublishedCardByIdMutationResolver {

    /**
     * Deletes a single `PublishedCard` using a unique key.
     */
    DeletePublishedCardPayload deletePublishedCardById(DeletePublishedCardByIdInput input) throws Exception;

}
