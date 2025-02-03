package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `MatchmakingQueue`.
 */
public interface AllMatchmakingQueuesQueryResolver {

    /**
     * Reads and enables pagination through a set of `MatchmakingQueue`.
     */
    MatchmakingQueuesConnection allMatchmakingQueues(Integer first, Integer last, Integer offset, String before, String after, java.util.List<MatchmakingQueuesOrderBy> orderBy, MatchmakingQueueCondition condition, MatchmakingQueueFilter filter) throws Exception;

}
