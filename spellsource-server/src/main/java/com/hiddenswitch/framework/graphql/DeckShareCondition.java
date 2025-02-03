package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `DeckShare` object types. All fields are tested
for equality and combined with a logical ‘and.’
 */
public class DeckShareCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String deckId;
    private String shareRecipientId;
    private Boolean trashedByRecipient;

    public DeckShareCondition() {
    }

    public DeckShareCondition(String deckId, String shareRecipientId, Boolean trashedByRecipient) {
        this.deckId = deckId;
        this.shareRecipientId = shareRecipientId;
        this.trashedByRecipient = trashedByRecipient;
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

    public Boolean getTrashedByRecipient() {
        return trashedByRecipient;
    }
    public void setTrashedByRecipient(Boolean trashedByRecipient) {
        this.trashedByRecipient = trashedByRecipient;
    }



    public static DeckShareCondition.Builder builder() {
        return new DeckShareCondition.Builder();
    }

    public static class Builder {

        private String deckId;
        private String shareRecipientId;
        private Boolean trashedByRecipient;

        public Builder() {
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setShareRecipientId(String shareRecipientId) {
            this.shareRecipientId = shareRecipientId;
            return this;
        }

        public Builder setTrashedByRecipient(Boolean trashedByRecipient) {
            this.trashedByRecipient = trashedByRecipient;
            return this;
        }


        public DeckShareCondition build() {
            return new DeckShareCondition(deckId, shareRecipientId, trashedByRecipient);
        }

    }
}
