package com.hiddenswitch.framework.graphql;


/**
 * All input for the `deleteDeckShareByDeckIdAndShareRecipientId` mutation.
 */
public class DeleteDeckShareByDeckIdAndShareRecipientIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String deckId;
    private String shareRecipientId;

    public DeleteDeckShareByDeckIdAndShareRecipientIdInput() {
    }

    public DeleteDeckShareByDeckIdAndShareRecipientIdInput(String clientMutationId, String deckId, String shareRecipientId) {
        this.clientMutationId = clientMutationId;
        this.deckId = deckId;
        this.shareRecipientId = shareRecipientId;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public String getShareRecipientId() {
        return shareRecipientId;
    }
    public void setShareRecipientId(String shareRecipientId) {
        this.shareRecipientId = shareRecipientId;
    }



    public static DeleteDeckShareByDeckIdAndShareRecipientIdInput.Builder builder() {
        return new DeleteDeckShareByDeckIdAndShareRecipientIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String deckId;
        private String shareRecipientId;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setShareRecipientId(String shareRecipientId) {
            this.shareRecipientId = shareRecipientId;
            return this;
        }


        public DeleteDeckShareByDeckIdAndShareRecipientIdInput build() {
            return new DeleteDeckShareByDeckIdAndShareRecipientIdInput(clientMutationId, deckId, shareRecipientId);
        }

    }
}
