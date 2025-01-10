package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `Guest` using its globally unique `ID`.
 */
public interface GuestQueryResolver {

    /**
     * Reads a single `Guest` using its globally unique `ID`.
     */
    Guest guest(String nodeId) throws Exception;

}
