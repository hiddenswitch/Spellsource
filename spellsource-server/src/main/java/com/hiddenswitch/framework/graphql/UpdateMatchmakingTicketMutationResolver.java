package com.hiddenswitch.framework.graphql;


/**
 * Updates a single `MatchmakingTicket` using its globally unique id and a patch.
 */
public interface UpdateMatchmakingTicketMutationResolver {

    /**
     * Updates a single `MatchmakingTicket` using its globally unique id and a patch.
     */
    UpdateMatchmakingTicketPayload updateMatchmakingTicket(UpdateMatchmakingTicketInput input) throws Exception;

}
