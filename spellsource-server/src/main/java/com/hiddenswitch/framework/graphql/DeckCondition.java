package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `Deck` object types. All fields are tested for equality and combined with a logical ‘and.’
 */
public class DeckCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String createdBy;
    private String lastEditedBy;
    private String name;
    private String heroClass;
    private Boolean trashed;
    private String format;
    private Integer deckType;
    private Boolean isPremade;
    private Boolean permittedToDuplicate;

    public DeckCondition() {
    }

    public DeckCondition(String id, String createdBy, String lastEditedBy, String name, String heroClass, Boolean trashed, String format, Integer deckType, Boolean isPremade, Boolean permittedToDuplicate) {
        this.id = id;
        this.createdBy = createdBy;
        this.lastEditedBy = lastEditedBy;
        this.name = name;
        this.heroClass = heroClass;
        this.trashed = trashed;
        this.format = format;
        this.deckType = deckType;
        this.isPremade = isPremade;
        this.permittedToDuplicate = permittedToDuplicate;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getLastEditedBy() {
        return lastEditedBy;
    }
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
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

    public Boolean getTrashed() {
        return trashed;
    }
    public void setTrashed(Boolean trashed) {
        this.trashed = trashed;
    }

    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }

    public Integer getDeckType() {
        return deckType;
    }
    public void setDeckType(Integer deckType) {
        this.deckType = deckType;
    }

    public Boolean getIsPremade() {
        return isPremade;
    }
    public void setIsPremade(Boolean isPremade) {
        this.isPremade = isPremade;
    }

    public Boolean getPermittedToDuplicate() {
        return permittedToDuplicate;
    }
    public void setPermittedToDuplicate(Boolean permittedToDuplicate) {
        this.permittedToDuplicate = permittedToDuplicate;
    }



    public static DeckCondition.Builder builder() {
        return new DeckCondition.Builder();
    }

    public static class Builder {

        private String id;
        private String createdBy;
        private String lastEditedBy;
        private String name;
        private String heroClass;
        private Boolean trashed;
        private String format;
        private Integer deckType;
        private Boolean isPremade;
        private Boolean permittedToDuplicate;

        public Builder() {
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setLastEditedBy(String lastEditedBy) {
            this.lastEditedBy = lastEditedBy;
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

        public Builder setTrashed(Boolean trashed) {
            this.trashed = trashed;
            return this;
        }

        public Builder setFormat(String format) {
            this.format = format;
            return this;
        }

        public Builder setDeckType(Integer deckType) {
            this.deckType = deckType;
            return this;
        }

        public Builder setIsPremade(Boolean isPremade) {
            this.isPremade = isPremade;
            return this;
        }

        public Builder setPermittedToDuplicate(Boolean permittedToDuplicate) {
            this.permittedToDuplicate = permittedToDuplicate;
            return this;
        }


        public DeckCondition build() {
            return new DeckCondition(id, createdBy, lastEditedBy, name, heroClass, trashed, format, deckType, isPremade, permittedToDuplicate);
        }

    }
}
