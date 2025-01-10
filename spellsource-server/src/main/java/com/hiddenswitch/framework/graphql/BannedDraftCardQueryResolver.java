package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `BannedDraftCard` using its globally unique `ID`.
 */
public interface BannedDraftCardQueryResolver {

    /**
     * Reads a single `BannedDraftCard` using its globally unique `ID`.
     */
    BannedDraftCard bannedDraftCard(String nodeId) throws Exception;

}
