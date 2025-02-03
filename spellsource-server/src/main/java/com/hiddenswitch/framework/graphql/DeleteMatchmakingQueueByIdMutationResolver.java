package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `MatchmakingQueue` using a unique key.
 */
public interface DeleteMatchmakingQueueByIdMutationResolver {

    /**
     * Deletes a single `MatchmakingQueue` using a unique key.
     */
    DeleteMatchmakingQueuePayload deleteMatchmakingQueueById(DeleteMatchmakingQueueByIdInput input) throws Exception;

}
