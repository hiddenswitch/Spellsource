package com.hiddenswitch.framework.graphql;


/**
 * Reads and enables pagination through a set of `MatchmakingTicket`.
 */
public interface AllMatchmakingTicketsQueryResolver {

    /**
     * Reads and enables pagination through a set of `MatchmakingTicket`.
     */
    MatchmakingTicketsConnection allMatchmakingTickets(Integer first, Integer last, Integer offset, String before, String after, java.util.List<MatchmakingTicketsOrderBy> orderBy, MatchmakingTicketCondition condition, MatchmakingTicketFilter filter) throws Exception;

}
