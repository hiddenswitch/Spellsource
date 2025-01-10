package com.hiddenswitch.framework.graphql;


/**
 * Creates a single `Friend`.
 */
public interface CreateFriendMutationResolver {

    /**
     * Creates a single `Friend`.
     */
    CreateFriendPayload createFriend(CreateFriendInput input) throws Exception;

}
