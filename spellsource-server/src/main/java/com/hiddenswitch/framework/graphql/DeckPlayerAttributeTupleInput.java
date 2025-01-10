package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `DeckPlayerAttributeTuple`
 */
public class DeckPlayerAttributeTupleInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String deckId;
    private int attribute;
    private String stringValue;

    public DeckPlayerAttributeTupleInput() {
    }

    public DeckPlayerAttributeTupleInput(String deckId, int attribute, String stringValue) {
        this.deckId = deckId;
        this.attribute = attribute;
        this.stringValue = stringValue;
    }

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public int getAttribute() {
        return attribute;
    }
    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }

    public String getStringValue() {
        return stringValue;
    }
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }



    public static DeckPlayerAttributeTupleInput.Builder builder() {
        return new DeckPlayerAttributeTupleInput.Builder();
    }

    public static class Builder {

        private String deckId;
        private int attribute;
        private String stringValue;

        public Builder() {
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setAttribute(int attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder setStringValue(String stringValue) {
            this.stringValue = stringValue;
            return this;
        }


        public DeckPlayerAttributeTupleInput build() {
            return new DeckPlayerAttributeTupleInput(deckId, attribute, stringValue);
        }

    }
}
