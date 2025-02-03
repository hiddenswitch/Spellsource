package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `Class`.
 */
public enum ClassesOrderBy {

    NATURAL("NATURAL"),
    CREATED_BY_ASC("CREATED_BY_ASC"),
    CREATED_BY_DESC("CREATED_BY_DESC"),
    CLASS_ASC("CLASS_ASC"),
    CLASS_DESC("CLASS_DESC"),
    IS_PUBLISHED_ASC("IS_PUBLISHED_ASC"),
    IS_PUBLISHED_DESC("IS_PUBLISHED_DESC"),
    COLLECTIBLE_ASC("COLLECTIBLE_ASC"),
    COLLECTIBLE_DESC("COLLECTIBLE_DESC"),
    CARD_SCRIPT_ASC("CARD_SCRIPT_ASC"),
    CARD_SCRIPT_DESC("CARD_SCRIPT_DESC"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC"),
    NAME_ASC("NAME_ASC"),
    NAME_DESC("NAME_DESC");

    private final String graphqlName;

    private ClassesOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
