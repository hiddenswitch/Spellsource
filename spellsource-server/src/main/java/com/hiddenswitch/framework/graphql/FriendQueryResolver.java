package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `Friend` using its globally unique `ID`.
 */
public interface FriendQueryResolver {

    /**
     * Reads a single `Friend` using its globally unique `ID`.
     */
    Friend friend(String nodeId) throws Exception;

}
