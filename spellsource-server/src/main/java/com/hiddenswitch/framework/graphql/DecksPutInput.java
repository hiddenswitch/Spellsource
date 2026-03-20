package com.hiddenswitch.framework.graphql;


public class DecksPutInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String heroClass;
    private String format;
    private String deckList;
    private java.util.List<String> cardIds;

    public DecksPutInput() {
    }

    public DecksPutInput(String name, String heroClass, String format, String deckList, java.util.List<String> cardIds) {
        this.name = name;
        this.heroClass = heroClass;
        this.format = format;
        this.deckList = deckList;
        this.cardIds = cardIds;
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

    public String getDeckList() {
        return deckList;
    }
    public void setDeckList(String deckList) {
        this.deckList = deckList;
    }

    public java.util.List<String> getCardIds() {
        return cardIds;
    }
    public void setCardIds(java.util.List<String> cardIds) {
        this.cardIds = cardIds;
    }



    public static DecksPutInput.Builder builder() {
        return new DecksPutInput.Builder();
    }

    public static class Builder {

        private String name;
        private String heroClass;
        private String format;
        private String deckList;
        private java.util.List<String> cardIds;

        public Builder() {
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

        public Builder setDeckList(String deckList) {
            this.deckList = deckList;
            return this;
        }

        public Builder setCardIds(java.util.List<String> cardIds) {
            this.cardIds = cardIds;
            return this;
        }


        public DecksPutInput build() {
            return new DecksPutInput(name, heroClass, format, deckList, cardIds);
        }

    }
}
