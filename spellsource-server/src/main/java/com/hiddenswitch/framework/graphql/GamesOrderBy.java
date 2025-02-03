package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `Game`.
 */
public enum GamesOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC"),
    STATUS_ASC("STATUS_ASC"),
    STATUS_DESC("STATUS_DESC"),
    GIT_HASH_ASC("GIT_HASH_ASC"),
    GIT_HASH_DESC("GIT_HASH_DESC"),
    TRACE_ASC("TRACE_ASC"),
    TRACE_DESC("TRACE_DESC"),
    CREATED_AT_ASC("CREATED_AT_ASC"),
    CREATED_AT_DESC("CREATED_AT_DESC");

    private final String graphqlName;

    private GamesOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
