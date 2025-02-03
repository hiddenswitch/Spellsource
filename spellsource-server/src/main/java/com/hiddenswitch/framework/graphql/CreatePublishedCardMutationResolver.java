package com.hiddenswitch.framework.graphql;


/**
 * Creates a single `PublishedCard`.
 */
public interface CreatePublishedCardMutationResolver {

    /**
     * Creates a single `PublishedCard`.
     */
    CreatePublishedCardPayload createPublishedCard(CreatePublishedCardInput input) throws Exception;

}
