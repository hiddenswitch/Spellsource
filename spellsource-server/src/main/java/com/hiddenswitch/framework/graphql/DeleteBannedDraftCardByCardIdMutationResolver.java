package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `BannedDraftCard` using a unique key.
 */
public interface DeleteBannedDraftCardByCardIdMutationResolver {

    /**
     * Deletes a single `BannedDraftCard` using a unique key.
     */
    DeleteBannedDraftCardPayload deleteBannedDraftCardByCardId(DeleteBannedDraftCardByCardIdInput input) throws Exception;

}
