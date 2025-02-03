package com.hiddenswitch.framework.graphql;


/**
 * Creates a single `Guest`.
 */
public interface CreateGuestMutationResolver {

    /**
     * Creates a single `Guest`.
     */
    CreateGuestPayload createGuest(CreateGuestInput input) throws Exception;

}
