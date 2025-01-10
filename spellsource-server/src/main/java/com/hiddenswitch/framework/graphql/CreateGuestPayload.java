package com.hiddenswitch.framework.graphql;


/**
 * The output of our create `Guest` mutation.
 */
public class CreateGuestPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private Guest guest;
    private Query query;
    private GuestsEdge guestEdge;

    public CreateGuestPayload() {
    }

    public CreateGuestPayload(String clientMutationId, Guest guest, Query query, GuestsEdge guestEdge) {
        this.clientMutationId = clientMutationId;
        this.guest = guest;
        this.query = query;
        this.guestEdge = guestEdge;
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
     * The `Guest` that was created by this mutation.
     */
    public Guest getGuest() {
        return guest;
    }
    /**
     * The `Guest` that was created by this mutation.
     */
    public void setGuest(Guest guest) {
        this.guest = guest;
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
     * An edge for our `Guest`. May be used by Relay 1.
     */
    public GuestsEdge getGuestEdge() {
        return guestEdge;
    }
    /**
     * An edge for our `Guest`. May be used by Relay 1.
     */
    public void setGuestEdge(GuestsEdge guestEdge) {
        this.guestEdge = guestEdge;
    }



    public static CreateGuestPayload.Builder builder() {
        return new CreateGuestPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private Guest guest;
        private Query query;
        private GuestsEdge guestEdge;

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
         * The `Guest` that was created by this mutation.
         */
        public Builder setGuest(Guest guest) {
            this.guest = guest;
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
         * An edge for our `Guest`. May be used by Relay 1.
         */
        public Builder setGuestEdge(GuestsEdge guestEdge) {
            this.guestEdge = guestEdge;
            return this;
        }


        public CreateGuestPayload build() {
            return new CreateGuestPayload(clientMutationId, guest, query, guestEdge);
        }

    }
}
