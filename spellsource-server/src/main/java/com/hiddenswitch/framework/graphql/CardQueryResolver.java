package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `Card` using its globally unique `ID`.
 */
public interface CardQueryResolver {

    /**
     * Reads a single `Card` using its globally unique `ID`.
     */
    Card card(String nodeId) throws Exception;

}
