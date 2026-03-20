package com.hiddenswitch.framework.graphql;


public class DecksPutResponse implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private InventoryCollection collection;
    private String deckId;

    public DecksPutResponse() {
    }

    public DecksPutResponse(InventoryCollection collection, String deckId) {
        this.collection = collection;
        this.deckId = deckId;
    }

    public InventoryCollection getCollection() {
        return collection;
    }
    public void setCollection(InventoryCollection collection) {
        this.collection = collection;
    }

    public String getDeckId() {
        return deckId;
    }
    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }



    public static DecksPutResponse.Builder builder() {
        return new DecksPutResponse.Builder();
    }

    public static class Builder {

        private InventoryCollection collection;
        private String deckId;

        public Builder() {
        }

        public Builder setCollection(InventoryCollection collection) {
            this.collection = collection;
            return this;
        }

        public Builder setDeckId(String deckId) {
            this.deckId = deckId;
            return this;
        }


        public DecksPutResponse build() {
            return new DecksPutResponse(collection, deckId);
        }

    }
}
