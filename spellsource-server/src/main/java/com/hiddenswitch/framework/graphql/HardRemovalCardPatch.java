package com.hiddenswitch.framework.graphql;


/**
 * Represents an update to a `HardRemovalCard`. Fields that are set will be updated.
 */
public class HardRemovalCardPatch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cardId;

    public HardRemovalCardPatch() {
    }

    public HardRemovalCardPatch(String cardId) {
        this.cardId = cardId;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }



    public static HardRemovalCardPatch.Builder builder() {
        return new HardRemovalCardPatch.Builder();
    }

    public static class Builder {

        private String cardId;

        public Builder() {
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }


        public HardRemovalCardPatch build() {
            return new HardRemovalCardPatch(cardId);
        }

    }
}
