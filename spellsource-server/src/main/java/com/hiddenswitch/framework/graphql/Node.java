package com.hiddenswitch.framework.graphql;


/**
 * An object with a globally unique `ID`.
 */
public interface Node {

    /**
     * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
     */
    String getNodeId();

}
