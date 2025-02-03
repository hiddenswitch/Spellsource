package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `Friend`.
 */
public interface AllFriendsQueryResolver {

    /**
     * Reads and enables pagination through a set of `Friend`.
     */
    FriendsConnection allFriends(Integer first, Integer last, Integer offset, String before, String after, java.util.List<FriendsOrderBy> orderBy, FriendCondition condition, FriendFilter filter) throws Exception;

}
