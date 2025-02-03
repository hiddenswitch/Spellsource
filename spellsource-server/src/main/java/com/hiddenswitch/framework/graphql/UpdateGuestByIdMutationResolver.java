package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `Guest` using a unique key and a patch.
 */
public interface UpdateGuestByIdMutationResolver {

    /**
     * Updates a single `Guest` using a unique key and a patch.
     */
    UpdateGuestPayload updateGuestById(UpdateGuestByIdInput input) throws Exception;

}
