package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `PublishedCard` using its globally unique id.
 */
public interface DeletePublishedCardMutationResolver {

    /**
     * Deletes a single `PublishedCard` using its globally unique id.
     */
    DeletePublishedCardPayload deletePublishedCard(DeletePublishedCardInput input) throws Exception;

}
