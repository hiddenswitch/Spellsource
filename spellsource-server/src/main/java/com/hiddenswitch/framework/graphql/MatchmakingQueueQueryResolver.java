package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `MatchmakingQueue` using its globally unique `ID`.
 */
public interface MatchmakingQueueQueryResolver {

    /**
     * Reads a single `MatchmakingQueue` using its globally unique `ID`.
     */
    MatchmakingQueue matchmakingQueue(String nodeId) throws Exception;

}
