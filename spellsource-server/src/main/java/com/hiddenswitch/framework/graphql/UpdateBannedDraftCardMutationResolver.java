package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `BannedDraftCard` using its globally unique id and a patch.
 */
public interface UpdateBannedDraftCardMutationResolver {

    /**
     * Updates a single `BannedDraftCard` using its globally unique id and a patch.
     */
    UpdateBannedDraftCardPayload updateBannedDraftCard(UpdateBannedDraftCardInput input) throws Exception;

}
