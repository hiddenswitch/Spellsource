package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `PublishedCard` using its globally unique id and a patch.
 */
public interface UpdatePublishedCardMutationResolver {

    /**
     * Updates a single `PublishedCard` using its globally unique id and a patch.
     */
    UpdatePublishedCardPayload updatePublishedCard(UpdatePublishedCardInput input) throws Exception;

}
