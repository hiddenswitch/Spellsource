package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `Guest` using a unique key.
 */
public interface DeleteGuestByIdMutationResolver {

    /**
     * Deletes a single `Guest` using a unique key.
     */
    DeleteGuestPayload deleteGuestById(DeleteGuestByIdInput input) throws Exception;

}
