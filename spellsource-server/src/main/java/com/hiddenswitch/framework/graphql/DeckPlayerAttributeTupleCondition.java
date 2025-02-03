package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `DeckPlayerAttributeTuple` object types. All
fields are tested for equality and combined with a logical ‘and.’
 */
public class DeckPlayerAttributeTupleCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String deckId;
    private Integer attribute;
    private String stringValue;

    public DeckPlayerAttributeTupleCondition() {
    }

    public DeckPlayerAttributeTupleCondition(String id, String deckId, Integer attribute, String stringValue) {
        this.id = id;
        this.deckId = deckId;
        this.attribute = attribute;
        this.stringValue = stringValue;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
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



    public static DeckPlayerAttributeTupleCondition.Builder builder() {
        return new DeckPlayerAttributeTupleCondition.Builder();
    }

    public static class Builder {

        private String id;
        private String deckId;
        private Integer attribute;
        private String stringValue;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
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


        public DeckPlayerAttributeTupleCondition build() {
            return new DeckPlayerAttributeTupleCondition(id, deckId, attribute, stringValue);
        }

    }
}
