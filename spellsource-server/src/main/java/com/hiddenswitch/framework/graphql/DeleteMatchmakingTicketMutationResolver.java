package com.hiddenswitch.framework.graphql;


/**
 * Deletes a single `MatchmakingTicket` using its globally unique id.
 */
public interface DeleteMatchmakingTicketMutationResolver {

    /**
     * Deletes a single `MatchmakingTicket` using its globally unique id.
     */
    DeleteMatchmakingTicketPayload deleteMatchmakingTicket(DeleteMatchmakingTicketInput input) throws Exception;

}
