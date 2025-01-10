package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `MatchmakingQueue` using its globally unique id and a patch.
 */
public interface UpdateMatchmakingQueueMutationResolver {

    /**
     * Updates a single `MatchmakingQueue` using its globally unique id and a patch.
     */
    UpdateMatchmakingQueuePayload updateMatchmakingQueue(UpdateMatchmakingQueueInput input) throws Exception;

}
