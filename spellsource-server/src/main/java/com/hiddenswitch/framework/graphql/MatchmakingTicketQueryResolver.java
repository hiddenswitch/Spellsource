package com.hiddenswitch.framework.graphql;


/**
 * Reads a single `MatchmakingTicket` using its globally unique `ID`.
 */
public interface MatchmakingTicketQueryResolver {

    /**
     * Reads a single `MatchmakingTicket` using its globally unique `ID`.
     */
    MatchmakingTicket matchmakingTicket(String nodeId) throws Exception;

}
