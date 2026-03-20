package com.hiddenswitch.framework.graphql;


public class DecksGetResponse implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private InventoryCollection collection;
    private int inventoryIdsSize;

    public DecksGetResponse() {
    }

    public DecksGetResponse(InventoryCollection collection, int inventoryIdsSize) {
        this.collection = collection;
        this.inventoryIdsSize = inventoryIdsSize;
    }

    public InventoryCollection getCollection() {
        return collection;
    }
    public void setCollection(InventoryCollection collection) {
        this.collection = collection;
    }

    public int getInventoryIdsSize() {
        return inventoryIdsSize;
    }
    public void setInventoryIdsSize(int inventoryIdsSize) {
        this.inventoryIdsSize = inventoryIdsSize;
    }



    public static DecksGetResponse.Builder builder() {
        return new DecksGetResponse.Builder();
    }

    public static class Builder {

        private InventoryCollection collection;
        private int inventoryIdsSize;

        public Builder() {
        }

        public Builder setCollection(InventoryCollection collection) {
            this.collection = collection;
            return this;
        }

        public Builder setInventoryIdsSize(int inventoryIdsSize) {
            this.inventoryIdsSize = inventoryIdsSize;
            return this;
        }


        public DecksGetResponse build() {
            return new DecksGetResponse(collection, inventoryIdsSize);
        }

    }
}
