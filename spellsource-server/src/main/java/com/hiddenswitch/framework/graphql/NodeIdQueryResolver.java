package com.hiddenswitch.framework.graphql;


/**
 * The root query type must be a `Node` to work well with Relay 1 mutations. This just resolves to `query`.
 */
public interface NodeIdQueryResolver {

    /**
     * The root query type must be a `Node` to work well with Relay 1 mutations. This just resolves to `query`.
     */
    String nodeId() throws Exception;

}
