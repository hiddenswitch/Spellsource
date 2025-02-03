package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `BannedDraftCard` using its globally unique id.
 */
public interface DeleteBannedDraftCardMutationResolver {

    /**
     * Deletes a single `BannedDraftCard` using its globally unique id.
     */
    DeleteBannedDraftCardPayload deleteBannedDraftCard(DeleteBannedDraftCardInput input) throws Exception;

}
