package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `HardRemovalCard`
 */
public class HardRemovalCardInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cardId;

    public HardRemovalCardInput() {
    }

    public HardRemovalCardInput(String cardId) {
        this.cardId = cardId;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }



    public static HardRemovalCardInput.Builder builder() {
        return new HardRemovalCardInput.Builder();
    }

    public static class Builder {

        private String cardId;

        public Builder() {
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }


        public HardRemovalCardInput build() {
            return new HardRemovalCardInput(cardId);
        }

    }
}
