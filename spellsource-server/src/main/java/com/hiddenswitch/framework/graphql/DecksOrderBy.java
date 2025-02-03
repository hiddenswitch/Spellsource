package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `Deck`.
 */
public enum DecksOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC"),
    CREATED_BY_ASC("CREATED_BY_ASC"),
    CREATED_BY_DESC("CREATED_BY_DESC"),
    LAST_EDITED_BY_ASC("LAST_EDITED_BY_ASC"),
    LAST_EDITED_BY_DESC("LAST_EDITED_BY_DESC"),
    NAME_ASC("NAME_ASC"),
    NAME_DESC("NAME_DESC"),
    HERO_CLASS_ASC("HERO_CLASS_ASC"),
    HERO_CLASS_DESC("HERO_CLASS_DESC"),
    TRASHED_ASC("TRASHED_ASC"),
    TRASHED_DESC("TRASHED_DESC"),
    FORMAT_ASC("FORMAT_ASC"),
    FORMAT_DESC("FORMAT_DESC"),
    DECK_TYPE_ASC("DECK_TYPE_ASC"),
    DECK_TYPE_DESC("DECK_TYPE_DESC"),
    IS_PREMADE_ASC("IS_PREMADE_ASC"),
    IS_PREMADE_DESC("IS_PREMADE_DESC"),
    PERMITTED_TO_DUPLICATE_ASC("PERMITTED_TO_DUPLICATE_ASC"),
    PERMITTED_TO_DUPLICATE_DESC("PERMITTED_TO_DUPLICATE_DESC");

    private final String graphqlName;

    private DecksOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
