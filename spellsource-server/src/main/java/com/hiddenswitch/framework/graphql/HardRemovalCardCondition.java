package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `HardRemovalCard` object types. All fields are
tested for equality and combined with a logical ‘and.’
 */
public class HardRemovalCardCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cardId;

    public HardRemovalCardCondition() {
    }

    public HardRemovalCardCondition(String cardId) {
        this.cardId = cardId;
    }

    public String getCardId() {
        return cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }



    public static HardRemovalCardCondition.Builder builder() {
        return new HardRemovalCardCondition.Builder();
    }

    public static class Builder {

        private String cardId;

        public Builder() {
        }

        public Builder setCardId(String cardId) {
            this.cardId = cardId;
            return this;
        }


        public HardRemovalCardCondition build() {
            return new HardRemovalCardCondition(cardId);
        }

    }
}
