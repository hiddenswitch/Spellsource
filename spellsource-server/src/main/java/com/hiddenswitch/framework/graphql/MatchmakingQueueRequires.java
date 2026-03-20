package com.hiddenswitch.framework.graphql;


/**
 * ─── Matchmaking types ────────────────────────────────────────
 */
public class MatchmakingQueueRequires implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private boolean deck;
    private boolean heroClass;
    private java.util.List<String> deckIdChoices;

    public MatchmakingQueueRequires() {
    }

    public MatchmakingQueueRequires(boolean deck, boolean heroClass, java.util.List<String> deckIdChoices) {
        this.deck = deck;
        this.heroClass = heroClass;
        this.deckIdChoices = deckIdChoices;
    }

    public boolean getDeck() {
        return deck;
    }
    public void setDeck(boolean deck) {
        this.deck = deck;
    }

    public boolean getHeroClass() {
        return heroClass;
    }
    public void setHeroClass(boolean heroClass) {
        this.heroClass = heroClass;
    }

    public java.util.List<String> getDeckIdChoices() {
        return deckIdChoices;
    }
    public void setDeckIdChoices(java.util.List<String> deckIdChoices) {
        this.deckIdChoices = deckIdChoices;
    }



    public static MatchmakingQueueRequires.Builder builder() {
        return new MatchmakingQueueRequires.Builder();
    }

    public static class Builder {

        private boolean deck;
        private boolean heroClass;
        private java.util.List<String> deckIdChoices;

        public Builder() {
        }

        public Builder setDeck(boolean deck) {
            this.deck = deck;
            return this;
        }

        public Builder setHeroClass(boolean heroClass) {
            this.heroClass = heroClass;
            return this;
        }

        public Builder setDeckIdChoices(java.util.List<String> deckIdChoices) {
            this.deckIdChoices = deckIdChoices;
            return this;
        }


        public MatchmakingQueueRequires build() {
            return new MatchmakingQueueRequires(deck, heroClass, deckIdChoices);
        }

    }
}
