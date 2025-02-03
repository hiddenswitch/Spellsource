package com.hiddenswitch.framework.graphql;


/**
 * Represents an update to a `BannedDraftCard`. Fields that are set will be updated.
 */
public class BannedDraftCardPatch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cardId;

    public BannedDraftCardPatch() {
    }

    public BannedDraftCardPatch(String cardId) {
        this.cardId = cardId;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }



    public static BannedDraftCardPatch.Builder builder() {
        return new BannedDraftCardPatch.Builder();
    }

    public static class Builder {

        private String cardId;

        public Builder() {
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }


        public BannedDraftCardPatch build() {
            return new BannedDraftCardPatch(cardId);
        }

    }
}
