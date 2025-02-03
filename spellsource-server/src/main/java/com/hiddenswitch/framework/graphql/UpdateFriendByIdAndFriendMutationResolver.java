package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `Friend` using a unique key and a patch.
 */
public interface UpdateFriendByIdAndFriendMutationResolver {

    /**
     * Updates a single `Friend` using a unique key and a patch.
     */
    UpdateFriendPayload updateFriendByIdAndFriend(UpdateFriendByIdAndFriendInput input) throws Exception;

}
