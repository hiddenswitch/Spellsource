package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `BannedDraftCard` object types. All fields are
tested for equality and combined with a logical ‘and.’
 */
public class BannedDraftCardCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cardId;

    public BannedDraftCardCondition() {
    }

    public BannedDraftCardCondition(String cardId) {
        this.cardId = cardId;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }



    public static BannedDraftCardCondition.Builder builder() {
        return new BannedDraftCardCondition.Builder();
    }

    public static class Builder {

        private String cardId;

        public Builder() {
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }


        public BannedDraftCardCondition build() {
            return new BannedDraftCardCondition(cardId);
        }

    }
}
