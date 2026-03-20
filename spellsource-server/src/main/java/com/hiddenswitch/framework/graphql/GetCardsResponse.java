package com.hiddenswitch.framework.graphql;


public class GetCardsResponse implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<CardRecord> cards;
    private String version;
    private boolean cachedOk;

    public GetCardsResponse() {
    }

    public GetCardsResponse(java.util.List<CardRecord> cards, String version, boolean cachedOk) {
        this.cards = cards;
        this.version = version;
        this.cachedOk = cachedOk;
    }

    public java.util.List<CardRecord> getCards() {
        return cards;
    }
    public void setCards(java.util.List<CardRecord> cards) {
        this.cards = cards;
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    public boolean getCachedOk() {
        return cachedOk;
    }
    public void setCachedOk(boolean cachedOk) {
        this.cachedOk = cachedOk;
    }



    public static GetCardsResponse.Builder builder() {
        return new GetCardsResponse.Builder();
    }

    public static class Builder {

        private java.util.List<CardRecord> cards;
        private String version;
        private boolean cachedOk;

        public Builder() {
        }

        public Builder setCards(java.util.List<CardRecord> cards) {
            this.cards = cards;
            return this;
        }

        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setCachedOk(boolean cachedOk) {
            this.cachedOk = cachedOk;
            return this;
        }


        public GetCardsResponse build() {
            return new GetCardsResponse(cards, version, cachedOk);
        }

    }
}
