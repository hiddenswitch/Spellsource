package com.hiddenswitch.framework.graphql;


/**
 * Represents an update to a `DeckPlayerAttributeTuple`. Fields that are set will be updated.
 */
public class DeckPlayerAttributeTuplePatch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String deckId;
    private Integer attribute;
    private String stringValue;

    public DeckPlayerAttributeTuplePatch() {
    }

    public DeckPlayerAttributeTuplePatch(String deckId, Integer attribute, String stringValue) {
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

    public Integer getAttribute() {
        return attribute;
    }
    public void setAttribute(Integer attribute) {
        this.attribute = attribute;
    }

    public String getStringValue() {
        return stringValue;
    }
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }



    public static DeckPlayerAttributeTuplePatch.Builder builder() {
        return new DeckPlayerAttributeTuplePatch.Builder();
    }

    public static class Builder {

        private String deckId;
        private Integer attribute;
        private String stringValue;

        public Builder() {
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setAttribute(Integer attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder setStringValue(String stringValue) {
            this.stringValue = stringValue;
            return this;
        }


        public DeckPlayerAttributeTuplePatch build() {
            return new DeckPlayerAttributeTuplePatch(deckId, attribute, stringValue);
        }

    }
}
