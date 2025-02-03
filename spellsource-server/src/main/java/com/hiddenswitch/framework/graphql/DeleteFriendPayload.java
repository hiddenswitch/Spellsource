package com.hiddenswitch.framework.graphql;


/**
 * The output of our delete `Friend` mutation.
 */
public class DeleteFriendPayload implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private Friend friend;
    private String deletedFriendId;
    private Query query;
    private FriendsEdge friendEdge;

    public DeleteFriendPayload() {
    }

    public DeleteFriendPayload(String clientMutationId, Friend friend, String deletedFriendId, Query query, FriendsEdge friendEdge) {
        this.clientMutationId = clientMutationId;
        this.friend = friend;
        this.deletedFriendId = deletedFriendId;
        this.query = query;
        this.friendEdge = friendEdge;
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
     * The `Friend` that was deleted by this mutation.
     */
    public Friend getFriend() {
        return friend;
    }
    /**
     * The `Friend` that was deleted by this mutation.
     */
    public void setFriend(Friend friend) {
        this.friend = friend;
    }

    public String getDeletedFriendId() {
        return deletedFriendId;
    }
    public void setDeletedFriendId(String deletedFriendId) {
        this.deletedFriendId = deletedFriendId;
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
     * An edge for our `Friend`. May be used by Relay 1.
     */
    public FriendsEdge getFriendEdge() {
        return friendEdge;
    }
    /**
     * An edge for our `Friend`. May be used by Relay 1.
     */
    public void setFriendEdge(FriendsEdge friendEdge) {
        this.friendEdge = friendEdge;
    }



    public static DeleteFriendPayload.Builder builder() {
        return new DeleteFriendPayload.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private Friend friend;
        private String deletedFriendId;
        private Query query;
        private FriendsEdge friendEdge;

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
         * The `Friend` that was deleted by this mutation.
         */
        public Builder setFriend(Friend friend) {
            this.friend = friend;
            return this;
        }

        public Builder setDeletedFriendId(String deletedFriendId) {
            this.deletedFriendId = deletedFriendId;
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
         * An edge for our `Friend`. May be used by Relay 1.
         */
        public Builder setFriendEdge(FriendsEdge friendEdge) {
            this.friendEdge = friendEdge;
            return this;
        }


        public DeleteFriendPayload build() {
            return new DeleteFriendPayload(clientMutationId, friend, deletedFriendId, query, friendEdge);
        }

    }
}
