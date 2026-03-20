package com.hiddenswitch.framework.graphql;


public class DecksUpdateInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String deckId;
    private java.util.List<String> pullAllCardIds;
    private java.util.List<String> pushCardIds;
    private String setHeroClass;
    private String setName;
    private AttributeValueInput setPlayerEntityAttribute;
    private String unsetPlayerEntityAttribute;

    public DecksUpdateInput() {
    }

    public DecksUpdateInput(String deckId, java.util.List<String> pullAllCardIds, java.util.List<String> pushCardIds, String setHeroClass, String setName, AttributeValueInput setPlayerEntityAttribute, String unsetPlayerEntityAttribute) {
        this.deckId = deckId;
        this.pullAllCardIds = pullAllCardIds;
        this.pushCardIds = pushCardIds;
        this.setHeroClass = setHeroClass;
        this.setName = setName;
        this.setPlayerEntityAttribute = setPlayerEntityAttribute;
        this.unsetPlayerEntityAttribute = unsetPlayerEntityAttribute;
    }

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public java.util.List<String> getPullAllCardIds() {
        return pullAllCardIds;
    }
    public void setPullAllCardIds(java.util.List<String> pullAllCardIds) {
        this.pullAllCardIds = pullAllCardIds;
    }

    public java.util.List<String> getPushCardIds() {
        return pushCardIds;
    }
    public void setPushCardIds(java.util.List<String> pushCardIds) {
        this.pushCardIds = pushCardIds;
    }

    public String getSetHeroClass() {
        return setHeroClass;
    }
    public void setSetHeroClass(String setHeroClass) {
        this.setHeroClass = setHeroClass;
    }

    public String getSetName() {
        return setName;
    }
    public void setSetName(String setName) {
        this.setName = setName;
    }

    public AttributeValueInput getSetPlayerEntityAttribute() {
        return setPlayerEntityAttribute;
    }
    public void setSetPlayerEntityAttribute(AttributeValueInput setPlayerEntityAttribute) {
        this.setPlayerEntityAttribute = setPlayerEntityAttribute;
    }

    public String getUnsetPlayerEntityAttribute() {
        return unsetPlayerEntityAttribute;
    }
    public void setUnsetPlayerEntityAttribute(String unsetPlayerEntityAttribute) {
        this.unsetPlayerEntityAttribute = unsetPlayerEntityAttribute;
    }



    public static DecksUpdateInput.Builder builder() {
        return new DecksUpdateInput.Builder();
    }

    public static class Builder {

        private String deckId;
        private java.util.List<String> pullAllCardIds;
        private java.util.List<String> pushCardIds;
        private String setHeroClass;
        private String setName;
        private AttributeValueInput setPlayerEntityAttribute;
        private String unsetPlayerEntityAttribute;

        public Builder() {
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setPullAllCardIds(java.util.List<String> pullAllCardIds) {
            this.pullAllCardIds = pullAllCardIds;
            return this;
        }

        public Builder setPushCardIds(java.util.List<String> pushCardIds) {
            this.pushCardIds = pushCardIds;
            return this;
        }

        public Builder setSetHeroClass(String setHeroClass) {
            this.setHeroClass = setHeroClass;
            return this;
        }

        public Builder setSetName(String setName) {
            this.setName = setName;
            return this;
        }

        public Builder setSetPlayerEntityAttribute(AttributeValueInput setPlayerEntityAttribute) {
            this.setPlayerEntityAttribute = setPlayerEntityAttribute;
            return this;
        }

        public Builder setUnsetPlayerEntityAttribute(String unsetPlayerEntityAttribute) {
            this.unsetPlayerEntityAttribute = unsetPlayerEntityAttribute;
            return this;
        }


        public DecksUpdateInput build() {
            return new DecksUpdateInput(deckId, pullAllCardIds, pushCardIds, setHeroClass, setName, setPlayerEntityAttribute, unsetPlayerEntityAttribute);
        }

    }
}
