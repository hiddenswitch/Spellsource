package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `PublishedCard`.
 */
public enum PublishedCardsOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC"),
    SUCCESSION_ASC("SUCCESSION_ASC"),
    SUCCESSION_DESC("SUCCESSION_DESC");

    private final String graphqlName;

    private PublishedCardsOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
