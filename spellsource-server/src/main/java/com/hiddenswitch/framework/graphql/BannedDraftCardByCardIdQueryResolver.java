package com.hiddenswitch.framework.graphql;


/**
 * Get a single `BannedDraftCard`.
 */
public interface BannedDraftCardByCardIdQueryResolver {

    /**
     * Get a single `BannedDraftCard`.
     */
    BannedDraftCard bannedDraftCardByCardId(String cardId) throws Exception;

}
