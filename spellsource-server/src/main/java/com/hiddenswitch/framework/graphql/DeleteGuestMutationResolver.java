package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `Guest` using its globally unique id.
 */
public interface DeleteGuestMutationResolver {

    /**
     * Deletes a single `Guest` using its globally unique id.
     */
    DeleteGuestPayload deleteGuest(DeleteGuestInput input) throws Exception;

}
