package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `Friend` using its globally unique id.
 */
public interface DeleteFriendMutationResolver {

    /**
     * Deletes a single `Friend` using its globally unique id.
     */
    DeleteFriendPayload deleteFriend(DeleteFriendInput input) throws Exception;

}
