package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `Friend` using its globally unique id and a patch.
 */
public interface UpdateFriendMutationResolver {

    /**
     * Updates a single `Friend` using its globally unique id and a patch.
     */
    UpdateFriendPayload updateFriend(UpdateFriendInput input) throws Exception;

}
