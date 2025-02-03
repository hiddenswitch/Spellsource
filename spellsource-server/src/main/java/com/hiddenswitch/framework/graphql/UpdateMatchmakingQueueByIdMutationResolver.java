package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `MatchmakingQueue` using a unique key and a patch.
 */
public interface UpdateMatchmakingQueueByIdMutationResolver {

    /**
     * Updates a single `MatchmakingQueue` using a unique key and a patch.
     */
    UpdateMatchmakingQueuePayload updateMatchmakingQueueById(UpdateMatchmakingQueueByIdInput input) throws Exception;

}
