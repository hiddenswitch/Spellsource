package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `Card`.
 */
public enum CardsOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC"),
    CREATED_BY_ASC("CREATED_BY_ASC"),
    CREATED_BY_DESC("CREATED_BY_DESC"),
    URI_ASC("URI_ASC"),
    URI_DESC("URI_DESC"),
    BLOCKLY_WORKSPACE_ASC("BLOCKLY_WORKSPACE_ASC"),
    BLOCKLY_WORKSPACE_DESC("BLOCKLY_WORKSPACE_DESC"),
    CARD_SCRIPT_ASC("CARD_SCRIPT_ASC"),
    CARD_SCRIPT_DESC("CARD_SCRIPT_DESC"),
    CREATED_AT_ASC("CREATED_AT_ASC"),
    CREATED_AT_DESC("CREATED_AT_DESC"),
    LAST_MODIFIED_ASC("LAST_MODIFIED_ASC"),
    LAST_MODIFIED_DESC("LAST_MODIFIED_DESC"),
    IS_ARCHIVED_ASC("IS_ARCHIVED_ASC"),
    IS_ARCHIVED_DESC("IS_ARCHIVED_DESC"),
    IS_PUBLISHED_ASC("IS_PUBLISHED_ASC"),
    IS_PUBLISHED_DESC("IS_PUBLISHED_DESC"),
    SUCCESSION_ASC("SUCCESSION_ASC"),
    SUCCESSION_DESC("SUCCESSION_DESC");

    private final String graphqlName;

    private CardsOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
