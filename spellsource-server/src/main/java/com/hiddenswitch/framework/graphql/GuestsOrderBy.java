package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `Guest`.
 */
public enum GuestsOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC"),
    USER_ID_ASC("USER_ID_ASC"),
    USER_ID_DESC("USER_ID_DESC");

    private final String graphqlName;

    private GuestsOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
