package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `GeneratedArt`.
 */
public enum GeneratedArtsOrderBy {

    NATURAL("NATURAL"),
    HASH_ASC("HASH_ASC"),
    HASH_DESC("HASH_DESC"),
    OWNER_ASC("OWNER_ASC"),
    OWNER_DESC("OWNER_DESC"),
    INFO_ASC("INFO_ASC"),
    INFO_DESC("INFO_DESC"),
    IS_ARCHIVED_ASC("IS_ARCHIVED_ASC"),
    IS_ARCHIVED_DESC("IS_ARCHIVED_DESC");

    private final String graphqlName;

    private GeneratedArtsOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
