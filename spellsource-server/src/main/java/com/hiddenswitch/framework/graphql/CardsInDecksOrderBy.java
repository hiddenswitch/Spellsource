package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `CardsInDeck`.
 */
public enum CardsInDecksOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC"),
    DECK_ID_ASC("DECK_ID_ASC"),
    DECK_ID_DESC("DECK_ID_DESC"),
    CARD_ID_ASC("CARD_ID_ASC"),
    CARD_ID_DESC("CARD_ID_DESC");

    private final String graphqlName;

    private CardsInDecksOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
