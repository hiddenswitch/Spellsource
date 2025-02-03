package com.hiddenswitch.framework.graphql;


/**
 * The output of our create `MatchmakingTicket` mutation.
 */
public class CreateMatchmakingTicketPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private MatchmakingTicket matchmakingTicket;
    private Query query;
    private MatchmakingTicketsEdge matchmakingTicketEdge;
    private Deck deckByBotDeckId;
    private Deck deckByDeckId;
    private MatchmakingQueue matchmakingQueueByQueueId;

    public CreateMatchmakingTicketPayload() {
    }

    public CreateMatchmakingTicketPayload(String clientMutationId, MatchmakingTicket matchmakingTicket, Query query, MatchmakingTicketsEdge matchmakingTicketEdge, Deck deckByBotDeckId, Deck deckByDeckId, MatchmakingQueue matchmakingQueueByQueueId) {
        this.clientMutationId = clientMutationId;
        this.matchmakingTicket = matchmakingTicket;
        this.query = query;
        this.matchmakingTicketEdge = matchmakingTicketEdge;
        this.deckByBotDeckId = deckByBotDeckId;
        this.deckByDeckId = deckByDeckId;
        this.matchmakingQueueByQueueId = matchmakingQueueByQueueId;
    }

    /**
     * The exact same `clientMutationId` that was provided in the mutation input,
unchanged and unused. May be used by a client to track mutations.
     */
    public String getClientMutationId() {
        return clientMutationId;
    }
    /**
     * The exact same `clientMutationId` that was provided in the mutation input,
unchanged and unused. May be used by a client to track mutations.
     */
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    /**
     * The `MatchmakingTicket` that was created by this mutation.
     */
    public MatchmakingTicket getMatchmakingTicket() {
        return matchmakingTicket;
    }
    /**
     * The `MatchmakingTicket` that was created by this mutation.
     */
    public void setMatchmakingTicket(MatchmakingTicket matchmakingTicket) {
        this.matchmakingTicket = matchmakingTicket;
    }

    /**
     * Our root query field type. Allows us to run any query from our mutation payload.
     */
    public Query getQuery() {
        return query;
    }
    /**
     * Our root query field type. Allows us to run any query from our mutation payload.
     */
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * An edge for our `MatchmakingTicket`. May be used by Relay 1.
     */
    public MatchmakingTicketsEdge getMatchmakingTicketEdge() {
        return matchmakingTicketEdge;
    }
    /**
     * An edge for our `MatchmakingTicket`. May be used by Relay 1.
     */
    public void setMatchmakingTicketEdge(MatchmakingTicketsEdge matchmakingTicketEdge) {
        this.matchmakingTicketEdge = matchmakingTicketEdge;
    }

    /**
     * Reads a single `Deck` that is related to this `MatchmakingTicket`.
     */
    public Deck getDeckByBotDeckId() {
        return deckByBotDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `MatchmakingTicket`.
     */
    public void setDeckByBotDeckId(Deck deckByBotDeckId) {
        this.deckByBotDeckId = deckByBotDeckId;
    }

    /**
     * Reads a single `Deck` that is related to this `MatchmakingTicket`.
     */
    public Deck getDeckByDeckId() {
        return deckByDeckId;
    }
    /**
     * Reads a single `Deck` that is related to this `MatchmakingTicket`.
     */
    public void setDeckByDeckId(Deck deckByDeckId) {
        this.deckByDeckId = deckByDeckId;
    }

    /**
     * Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`.
     */
    public MatchmakingQueue getMatchmakingQueueByQueueId() {
        return matchmakingQueueByQueueId;
    }
    /**
     * Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`.
     */
    public void setMatchmakingQueueByQueueId(MatchmakingQueue matchmakingQueueByQueueId) {
        this.matchmakingQueueByQueueId = matchmakingQueueByQueueId;
    }



    public static CreateMatchmakingTicketPayload.Builder builder() {
        return new CreateMatchmakingTicketPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private MatchmakingTicket matchmakingTicket;
        private Query query;
        private MatchmakingTicketsEdge matchmakingTicketEdge;
        private Deck deckByBotDeckId;
        private Deck deckByDeckId;
        private MatchmakingQueue matchmakingQueueByQueueId;

        public Builder() {
        }

        /**
         * The exact same `clientMutationId` that was provided in the mutation input,
unchanged and unused. May be used by a client to track mutations.
         */
        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        /**
         * The `MatchmakingTicket` that was created by this mutation.
         */
        public Builder setMatchmakingTicket(MatchmakingTicket matchmakingTicket) {
            this.matchmakingTicket = matchmakingTicket;
            return this;
        }

        /**
         * Our root query field type. Allows us to run any query from our mutation payload.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }

        /**
         * An edge for our `MatchmakingTicket`. May be used by Relay 1.
         */
        public Builder setMatchmakingTicketEdge(MatchmakingTicketsEdge matchmakingTicketEdge) {
            this.matchmakingTicketEdge = matchmakingTicketEdge;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `MatchmakingTicket`.
         */
        public Builder setDeckByBotDeckId(Deck deckByBotDeckId) {
            this.deckByBotDeckId = deckByBotDeckId;
            return this;
        }

        /**
         * Reads a single `Deck` that is related to this `MatchmakingTicket`.
         */
        public Builder setDeckByDeckId(Deck deckByDeckId) {
            this.deckByDeckId = deckByDeckId;
            return this;
        }

        /**
         * Reads a single `MatchmakingQueue` that is related to this `MatchmakingTicket`.
         */
        public Builder setMatchmakingQueueByQueueId(MatchmakingQueue matchmakingQueueByQueueId) {
            this.matchmakingQueueByQueueId = matchmakingQueueByQueueId;
            return this;
        }


        public CreateMatchmakingTicketPayload build() {
            return new CreateMatchmakingTicketPayload(clientMutationId, matchmakingTicket, query, matchmakingTicketEdge, deckByBotDeckId, deckByDeckId, matchmakingQueueByQueueId);
        }

    }
}
