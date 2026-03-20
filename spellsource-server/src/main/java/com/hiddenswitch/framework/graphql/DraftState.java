package com.hiddenswitch.framework.graphql;


/**
 * ─── Draft types ──────────────────────────────────────────────
 */
public class DraftState implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int cardsRemaining;
    private java.util.List<Entity> currentCardChoices;
    private String deckId;
    private int draftIndex;
    private Entity heroClass;
    private java.util.List<Entity> heroClassChoices;
    private int losses;
    private java.util.List<String> selectedCardIds;
    private DraftStatus status;
    private int wins;

    public DraftState() {
    }

    public DraftState(int cardsRemaining, java.util.List<Entity> currentCardChoices, String deckId, int draftIndex, Entity heroClass, java.util.List<Entity> heroClassChoices, int losses, java.util.List<String> selectedCardIds, DraftStatus status, int wins) {
        this.cardsRemaining = cardsRemaining;
        this.currentCardChoices = currentCardChoices;
        this.deckId = deckId;
        this.draftIndex = draftIndex;
        this.heroClass = heroClass;
        this.heroClassChoices = heroClassChoices;
        this.losses = losses;
        this.selectedCardIds = selectedCardIds;
        this.status = status;
        this.wins = wins;
    }

    public int getCardsRemaining() {
        return cardsRemaining;
    }
    public void setCardsRemaining(int cardsRemaining) {
        this.cardsRemaining = cardsRemaining;
    }

    public java.util.List<Entity> getCurrentCardChoices() {
        return currentCardChoices;
    }
    public void setCurrentCardChoices(java.util.List<Entity> currentCardChoices) {
        this.currentCardChoices = currentCardChoices;
    }

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public int getDraftIndex() {
        return draftIndex;
    }
    public void setDraftIndex(int draftIndex) {
        this.draftIndex = draftIndex;
    }

    public Entity getHeroClass() {
        return heroClass;
    }
    public void setHeroClass(Entity heroClass) {
        this.heroClass = heroClass;
    }

    public java.util.List<Entity> getHeroClassChoices() {
        return heroClassChoices;
    }
    public void setHeroClassChoices(java.util.List<Entity> heroClassChoices) {
        this.heroClassChoices = heroClassChoices;
    }

    public int getLosses() {
        return losses;
    }
    public void setLosses(int losses) {
        this.losses = losses;
    }

    public java.util.List<String> getSelectedCardIds() {
        return selectedCardIds;
    }
    public void setSelectedCardIds(java.util.List<String> selectedCardIds) {
        this.selectedCardIds = selectedCardIds;
    }

    public DraftStatus getStatus() {
        return status;
    }
    public void setStatus(DraftStatus status) {
        this.status = status;
    }

    public int getWins() {
        return wins;
    }
    public void setWins(int wins) {
        this.wins = wins;
    }



    public static DraftState.Builder builder() {
        return new DraftState.Builder();
    }

    public static class Builder {

        private int cardsRemaining;
        private java.util.List<Entity> currentCardChoices;
        private String deckId;
        private int draftIndex;
        private Entity heroClass;
        private java.util.List<Entity> heroClassChoices;
        private int losses;
        private java.util.List<String> selectedCardIds;
        private DraftStatus status;
        private int wins;

        public Builder() {
        }

        public Builder setCardsRemaining(int cardsRemaining) {
            this.cardsRemaining = cardsRemaining;
            return this;
        }

        public Builder setCurrentCardChoices(java.util.List<Entity> currentCardChoices) {
            this.currentCardChoices = currentCardChoices;
            return this;
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }

        public Builder setDraftIndex(int draftIndex) {
            this.draftIndex = draftIndex;
            return this;
        }

        public Builder setHeroClass(Entity heroClass) {
            this.heroClass = heroClass;
            return this;
        }

        public Builder setHeroClassChoices(java.util.List<Entity> heroClassChoices) {
            this.heroClassChoices = heroClassChoices;
            return this;
        }

        public Builder setLosses(int losses) {
            this.losses = losses;
            return this;
        }

        public Builder setSelectedCardIds(java.util.List<String> selectedCardIds) {
            this.selectedCardIds = selectedCardIds;
            return this;
        }

        public Builder setStatus(DraftStatus status) {
            this.status = status;
            return this;
        }

        public Builder setWins(int wins) {
            this.wins = wins;
            return this;
        }


        public DraftState build() {
            return new DraftState(cardsRemaining, currentCardChoices, deckId, draftIndex, heroClass, heroClassChoices, losses, selectedCardIds, status, wins);
        }

    }
}
