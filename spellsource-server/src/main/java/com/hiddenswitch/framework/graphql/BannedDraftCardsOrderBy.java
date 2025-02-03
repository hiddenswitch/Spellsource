package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `BannedDraftCard`.
 */
public enum BannedDraftCardsOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    CARD_ID_ASC("CARD_ID_ASC"),
    CARD_ID_DESC("CARD_ID_DESC");

    private final String graphqlName;

    private BannedDraftCardsOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
