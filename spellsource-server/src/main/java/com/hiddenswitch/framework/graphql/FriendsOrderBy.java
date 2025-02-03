package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `Friend`.
 */
public enum FriendsOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC"),
    FRIEND_ASC("FRIEND_ASC"),
    FRIEND_DESC("FRIEND_DESC"),
    CREATED_AT_ASC("CREATED_AT_ASC"),
    CREATED_AT_DESC("CREATED_AT_DESC");

    private final String graphqlName;

    private FriendsOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
