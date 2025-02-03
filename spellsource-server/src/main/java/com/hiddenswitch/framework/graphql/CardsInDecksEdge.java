package com.hiddenswitch.framework.graphql;


/**
 * A `CardsInDeck` edge in the connection.
 */
public class CardsInDecksEdge implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String cursor;
    private CardsInDeck node;

    public CardsInDecksEdge() {
    }

    public CardsInDecksEdge(String cursor, CardsInDeck node) {
        this.cursor = cursor;
        this.node = node;
    }

    /**
     * A cursor for use in pagination.
     */
    public String getCursor() {
        return cursor;
    }
    /**
     * A cursor for use in pagination.
     */
    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    /**
     * The `CardsInDeck` at the end of the edge.
     */
    public CardsInDeck getNode() {
        return node;
    }
    /**
     * The `CardsInDeck` at the end of the edge.
     */
    public void setNode(CardsInDeck node) {
        this.node = node;
    }



    public static CardsInDecksEdge.Builder builder() {
        return new CardsInDecksEdge.Builder();
    }

    public static class Builder {

        private String cursor;
        private CardsInDeck node;

        public Builder() {
        }

        /**
         * A cursor for use in pagination.
         */
        public Builder setCursor(String cursor) {
            this.cursor = cursor;
            return this;
        }

        /**
         * The `CardsInDeck` at the end of the edge.
         */
        public Builder setNode(CardsInDeck node) {
            this.node = node;
            return this;
        }


        public CardsInDecksEdge build() {
            return new CardsInDecksEdge(cursor, node);
        }

    }
}
