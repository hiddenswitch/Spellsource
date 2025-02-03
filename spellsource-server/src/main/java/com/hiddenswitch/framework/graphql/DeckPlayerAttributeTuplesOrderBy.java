package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `DeckPlayerAttributeTuple`.
 */
public enum DeckPlayerAttributeTuplesOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC"),
    DECK_ID_ASC("DECK_ID_ASC"),
    DECK_ID_DESC("DECK_ID_DESC"),
    ATTRIBUTE_ASC("ATTRIBUTE_ASC"),
    ATTRIBUTE_DESC("ATTRIBUTE_DESC"),
    STRING_VALUE_ASC("STRING_VALUE_ASC"),
    STRING_VALUE_DESC("STRING_VALUE_DESC");

    private final String graphqlName;

    private DeckPlayerAttributeTuplesOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
