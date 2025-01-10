package com.hiddenswitch.framework.graphql;


/**
 * Get a single `Guest`.
 */
public interface GuestByIdQueryResolver {

    /**
     * Get a single `Guest`.
     */
    Guest guestById(String id) throws Exception;

}
