package com.hiddenswitch.framework.graphql;


public class InventoryCollection implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String heroClass;
    private String format;
    private DeckType deckType;
    private CollectionType collectionType;
    private String userId;
    private boolean isStandardDeck;
    private java.util.List<CardRecord> inventory;
    private java.util.List<AttributeValueTuple> playerEntityAttributes;
    private ValidationReport validationReport;

    public InventoryCollection() {
    }

    public InventoryCollection(String id, String name, String heroClass, String format, DeckType deckType, CollectionType collectionType, String userId, boolean isStandardDeck, java.util.List<CardRecord> inventory, java.util.List<AttributeValueTuple> playerEntityAttributes, ValidationReport validationReport) {
        this.id = id;
        this.name = name;
        this.heroClass = heroClass;
        this.format = format;
        this.deckType = deckType;
        this.collectionType = collectionType;
        this.userId = userId;
        this.isStandardDeck = isStandardDeck;
        this.inventory = inventory;
        this.playerEntityAttributes = playerEntityAttributes;
        this.validationReport = validationReport;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getHeroClass() {
        return heroClass;
    }
    public void setHeroClass(String heroClass) {
        this.heroClass = heroClass;
    }

    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }

    public DeckType getDeckType() {
        return deckType;
    }
    public void setDeckType(DeckType deckType) {
        this.deckType = deckType;
    }

    public CollectionType getCollectionType() {
        return collectionType;
    }
    public void setCollectionType(CollectionType collectionType) {
        this.collectionType = collectionType;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean getIsStandardDeck() {
        return isStandardDeck;
    }
    public void setIsStandardDeck(boolean isStandardDeck) {
        this.isStandardDeck = isStandardDeck;
    }

    public java.util.List<CardRecord> getInventory() {
        return inventory;
    }
    public void setInventory(java.util.List<CardRecord> inventory) {
        this.inventory = inventory;
    }

    public java.util.List<AttributeValueTuple> getPlayerEntityAttributes() {
        return playerEntityAttributes;
    }
    public void setPlayerEntityAttributes(java.util.List<AttributeValueTuple> playerEntityAttributes) {
        this.playerEntityAttributes = playerEntityAttributes;
    }

    public ValidationReport getValidationReport() {
        return validationReport;
    }
    public void setValidationReport(ValidationReport validationReport) {
        this.validationReport = validationReport;
    }



    public static InventoryCollection.Builder builder() {
        return new InventoryCollection.Builder();
    }

    public static class Builder {

        private String id;
        private String name;
        private String heroClass;
        private String format;
        private DeckType deckType;
        private CollectionType collectionType;
        private String userId;
        private boolean isStandardDeck;
        private java.util.List<CardRecord> inventory;
        private java.util.List<AttributeValueTuple> playerEntityAttributes;
        private ValidationReport validationReport;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setHeroClass(String heroClass) {
            this.heroClass = heroClass;
            return this;
        }

        public Builder setFormat(String format) {
            this.format = format;
            return this;
        }

        public Builder setDeckType(DeckType deckType) {
            this.deckType = deckType;
            return this;
        }

        public Builder setCollectionType(CollectionType collectionType) {
            this.collectionType = collectionType;
            return this;
        }

        public Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder setIsStandardDeck(boolean isStandardDeck) {
            this.isStandardDeck = isStandardDeck;
            return this;
        }

        public Builder setInventory(java.util.List<CardRecord> inventory) {
            this.inventory = inventory;
            return this;
        }

        public Builder setPlayerEntityAttributes(java.util.List<AttributeValueTuple> playerEntityAttributes) {
            this.playerEntityAttributes = playerEntityAttributes;
            return this;
        }

        public Builder setValidationReport(ValidationReport validationReport) {
            this.validationReport = validationReport;
            return this;
        }


        public InventoryCollection build() {
            return new InventoryCollection(id, name, heroClass, format, deckType, collectionType, userId, isStandardDeck, inventory, playerEntityAttributes, validationReport);
        }

    }
}
