package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `HardRemovalCard` using its globally unique `ID`.
 */
public interface HardRemovalCardQueryResolver {

    /**
     * Reads a single `HardRemovalCard` using its globally unique `ID`.
     */
    HardRemovalCard hardRemovalCard(String nodeId) throws Exception;

}
