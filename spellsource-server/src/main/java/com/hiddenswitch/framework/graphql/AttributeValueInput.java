package com.hiddenswitch.framework.graphql;


public class AttributeValueInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private PlayerEntityAttribute attribute;
    private String stringValue;

    public AttributeValueInput() {
    }

    public AttributeValueInput(PlayerEntityAttribute attribute, String stringValue) {
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



    public static AttributeValueInput.Builder builder() {
        return new AttributeValueInput.Builder();
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


        public AttributeValueInput build() {
            return new AttributeValueInput(attribute, stringValue);
        }

    }
}
