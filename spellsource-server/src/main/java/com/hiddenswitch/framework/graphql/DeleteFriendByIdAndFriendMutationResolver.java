package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `Friend` using a unique key.
 */
public interface DeleteFriendByIdAndFriendMutationResolver {

    /**
     * Deletes a single `Friend` using a unique key.
     */
    DeleteFriendPayload deleteFriendByIdAndFriend(DeleteFriendByIdAndFriendInput input) throws Exception;

}
