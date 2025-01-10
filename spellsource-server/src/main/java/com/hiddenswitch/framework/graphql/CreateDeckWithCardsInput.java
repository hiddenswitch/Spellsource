package com.hiddenswitch.framework.graphql;


/**
 * All input for the `createDeckWithCards` mutation.
 */
public class CreateDeckWithCardsInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String clientMutationId;
    private String deckName;
    private String classHero;
    private String formatName;
    private java.util.List<String> cardIds;

    public CreateDeckWithCardsInput() {
    }

    public CreateDeckWithCardsInput(String clientMutationId, String deckName, String classHero, String formatName, java.util.List<String> cardIds) {
        this.clientMutationId = clientMutationId;
        this.deckName = deckName;
        this.classHero = classHero;
        this.formatName = formatName;
        this.cardIds = cardIds;
    }

    public String getClientMutationId() {
        return clientMutationId;
    }
    public void setClientMutationId(String clientMutationId) {
        this.clientMutationId = clientMutationId;
    }

    public String getDeckName() {
        return deckName;
    }
    public void setDeckName(String deckName) {
        this.deckName = deckName;
    }

    public String getClassHero() {
        return classHero;
    }
    public void setClassHero(String classHero) {
        this.classHero = classHero;
    }

    public String getFormatName() {
        return formatName;
    }
    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public java.util.List<String> getCardIds() {
        return cardIds;
    }
    public void setCardIds(java.util.List<String> cardIds) {
        this.cardIds = cardIds;
    }



    public static CreateDeckWithCardsInput.Builder builder() {
        return new CreateDeckWithCardsInput.Builder();
    }

    public static class Builder {

        private String clientMutationId;
        private String deckName;
        private String classHero;
        private String formatName;
        private java.util.List<String> cardIds;

        public Builder() {
        }

        public Builder setClientMutationId(String clientMutationId) {
            this.clientMutationId = clientMutationId;
            return this;
        }

        public Builder setDeckName(String deckName) {
            this.deckName = deckName;
            return this;
        }

        public Builder setClassHero(String classHero) {
            this.classHero = classHero;
            return this;
        }

        public Builder setFormatName(String formatName) {
            this.formatName = formatName;
            return this;
        }

        public Builder setCardIds(java.util.List<String> cardIds) {
            this.cardIds = cardIds;
            return this;
        }


        public CreateDeckWithCardsInput build() {
            return new CreateDeckWithCardsInput(clientMutationId, deckName, classHero, formatName, cardIds);
        }

    }
}
