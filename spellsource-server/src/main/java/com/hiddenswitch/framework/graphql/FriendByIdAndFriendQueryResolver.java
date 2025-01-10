package com.hiddenswitch.framework.graphql;


/**
 * Get a single `Friend`.
 */
public interface FriendByIdAndFriendQueryResolver {

    /**
     * Get a single `Friend`.
     */
    Friend friendByIdAndFriend(String id, String friend) throws Exception;

}
