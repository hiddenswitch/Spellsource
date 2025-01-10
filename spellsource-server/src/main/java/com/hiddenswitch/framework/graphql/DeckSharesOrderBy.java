package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `DeckShare`.
 */
public enum DeckSharesOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    DECK_ID_ASC("DECK_ID_ASC"),
    DECK_ID_DESC("DECK_ID_DESC"),
    SHARE_RECIPIENT_ID_ASC("SHARE_RECIPIENT_ID_ASC"),
    SHARE_RECIPIENT_ID_DESC("SHARE_RECIPIENT_ID_DESC"),
    TRASHED_BY_RECIPIENT_ASC("TRASHED_BY_RECIPIENT_ASC"),
    TRASHED_BY_RECIPIENT_DESC("TRASHED_BY_RECIPIENT_DESC");

    private final String graphqlName;

    private DeckSharesOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
