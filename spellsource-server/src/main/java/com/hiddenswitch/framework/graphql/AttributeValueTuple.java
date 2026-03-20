package com.hiddenswitch.framework.graphql;


/**
 * ─── Deck types ───────────────────────────────────────────────
 */
public class AttributeValueTuple implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private PlayerEntityAttribute attribute;
    private String stringValue;

    public AttributeValueTuple() {
    }

    public AttributeValueTuple(PlayerEntityAttribute attribute, String stringValue) {
        this.attribute = attribute;
        this.stringValue = stringValue;
    }

    public PlayerEntityAttribute getAttribute() {
        return attribute;
    }
    public void setAttribute(PlayerEntityAttribute attribute) {
        this.attribute = attribute;
    }

    public String getStringValue() {
        return stringValue;
    }
    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }



    public static AttributeValueTuple.Builder builder() {
        return new AttributeValueTuple.Builder();
    }

    public static class Builder {

        private PlayerEntityAttribute attribute;
        private String stringValue;

        public Builder() {
        }

        public Builder setAttribute(PlayerEntityAttribute attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder setStringValue(String stringValue) {
            this.stringValue = stringValue;
            return this;
        }


        public AttributeValueTuple build() {
            return new AttributeValueTuple(attribute, stringValue);
        }

    }
}
