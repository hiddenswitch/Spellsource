package com.hiddenswitch.framework.graphql;


/**
 * Get a single `MatchmakingTicket`.
 */
public interface MatchmakingTicketByUserIdQueryResolver {

    /**
     * Get a single `MatchmakingTicket`.
     */
    MatchmakingTicket matchmakingTicketByUserId(String userId) throws Exception;

}
