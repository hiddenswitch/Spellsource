package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `Guest` using its globally unique id and a patch.
 */
public interface UpdateGuestMutationResolver {

    /**
     * Updates a single `Guest` using its globally unique id and a patch.
     */
    UpdateGuestPayload updateGuest(UpdateGuestInput input) throws Exception;

}
