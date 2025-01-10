package com.hiddenswitch.framework.graphql;


/**
 * All input for the `updateDeckShareByDeckIdAndShareRecipientId` mutation.
 */
public class UpdateDeckShareByDeckIdAndShareRecipientIdInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String deckId;
    private String shareRecipientId;
    private DeckSharePatch deckSharePatch;

    public UpdateDeckShareByDeckIdAndShareRecipientIdInput() {
    }

    public UpdateDeckShareByDeckIdAndShareRecipientIdInput(String clientMutationId, String deckId, String shareRecipientId, DeckSharePatch deckSharePatch) {
        this.clientMutationId = clientMutationId;
        this.deckId = deckId;
        this.shareRecipientId = shareRecipientId;
        this.deckSharePatch = deckSharePatch;
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

    public DeckSharePatch getDeckSharePatch() {
        return deckSharePatch;
    }
    public void setDeckSharePatch(DeckSharePatch deckSharePatch) {
        this.deckSharePatch = deckSharePatch;
    }



    public static UpdateDeckShareByDeckIdAndShareRecipientIdInput.Builder builder() {
        return new UpdateDeckShareByDeckIdAndShareRecipientIdInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String deckId;
        private String shareRecipientId;
        private DeckSharePatch deckSharePatch;

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

        public Builder setDeckSharePatch(DeckSharePatch deckSharePatch) {
            this.deckSharePatch = deckSharePatch;
            return this;
        }


        public UpdateDeckShareByDeckIdAndShareRecipientIdInput build() {
            return new UpdateDeckShareByDeckIdAndShareRecipientIdInput(clientMutationId, deckId, shareRecipientId, deckSharePatch);
        }

    }
}
