package com.hiddenswitch.framework.graphql;


public class CardEvent implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private Entity card;
    private boolean showLocal;

    public CardEvent() {
    }

    public CardEvent(Entity card, boolean showLocal) {
        this.card = card;
        this.showLocal = showLocal;
    }

    public Entity getCard() {
        return card;
    }
    public void setCard(Entity card) {
        this.card = card;
    }

    public boolean getShowLocal() {
        return showLocal;
    }
    public void setShowLocal(boolean showLocal) {
        this.showLocal = showLocal;
    }



    public static CardEvent.Builder builder() {
        return new CardEvent.Builder();
    }

    public static class Builder {

        private Entity card;
        private boolean showLocal;

        public Builder() {
        }

        public Builder setCard(Entity card) {
            this.card = card;
            return this;
        }

        public Builder setShowLocal(boolean showLocal) {
            this.showLocal = showLocal;
            return this;
        }


        public CardEvent build() {
            return new CardEvent(card, showLocal);
        }

    }
}
