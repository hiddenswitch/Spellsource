package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `PublishedCard` using a unique key and a patch.
 */
public interface UpdatePublishedCardByIdMutationResolver {

    /**
     * Updates a single `PublishedCard` using a unique key and a patch.
     */
    UpdatePublishedCardPayload updatePublishedCardById(UpdatePublishedCardByIdInput input) throws Exception;

}
