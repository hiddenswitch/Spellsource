package com.hiddenswitch.framework.graphql;


/**
 * Get a single `MatchmakingQueue`.
 */
public interface MatchmakingQueueByIdQueryResolver {

    /**
     * Get a single `MatchmakingQueue`.
     */
    MatchmakingQueue matchmakingQueueById(String id) throws Exception;

}
