package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `BannedDraftCard`
 */
public class BannedDraftCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cardId;

    public BannedDraftCardInput() {
    }

    public BannedDraftCardInput(String cardId) {
        this.cardId = cardId;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }



    public static BannedDraftCardInput.Builder builder() {
        return new BannedDraftCardInput.Builder();
    }

    public static class Builder {

        private String cardId;

        public Builder() {
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }


        public BannedDraftCardInput build() {
            return new BannedDraftCardInput(cardId);
        }

    }
}
