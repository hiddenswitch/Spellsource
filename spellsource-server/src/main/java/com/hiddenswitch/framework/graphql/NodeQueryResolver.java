package com.hiddenswitch.framework.graphql;


/**
 * Fetches an object given its globally unique `ID`.
 */
public interface NodeQueryResolver {

    /**
     * Fetches an object given its globally unique `ID`.
     */
    Node node(String nodeId) throws Exception;

}
