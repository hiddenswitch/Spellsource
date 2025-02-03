package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `BotUser`.
 */
public enum BotUsersOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC");

    private final String graphqlName;

    private BotUsersOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
