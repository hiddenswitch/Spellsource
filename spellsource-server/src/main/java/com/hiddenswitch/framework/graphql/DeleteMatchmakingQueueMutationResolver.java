package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `MatchmakingQueue` using its globally unique id.
 */
public interface DeleteMatchmakingQueueMutationResolver {

    /**
     * Deletes a single `MatchmakingQueue` using its globally unique id.
     */
    DeleteMatchmakingQueuePayload deleteMatchmakingQueue(DeleteMatchmakingQueueInput input) throws Exception;

}
