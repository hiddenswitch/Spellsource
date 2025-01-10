package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `CollectionCard`.
 */
public enum CollectionCardsOrderBy {

    NATURAL("NATURAL"),
    ID_ASC("ID_ASC"),
    ID_DESC("ID_DESC"),
    CREATED_BY_ASC("CREATED_BY_ASC"),
    CREATED_BY_DESC("CREATED_BY_DESC"),
    CARD_SCRIPT_ASC("CARD_SCRIPT_ASC"),
    CARD_SCRIPT_DESC("CARD_SCRIPT_DESC"),
    BLOCKLY_WORKSPACE_ASC("BLOCKLY_WORKSPACE_ASC"),
    BLOCKLY_WORKSPACE_DESC("BLOCKLY_WORKSPACE_DESC"),
    NAME_ASC("NAME_ASC"),
    NAME_DESC("NAME_DESC"),
    TYPE_ASC("TYPE_ASC"),
    TYPE_DESC("TYPE_DESC"),
    CLASS_ASC("CLASS_ASC"),
    CLASS_DESC("CLASS_DESC"),
    COST_ASC("COST_ASC"),
    COST_DESC("COST_DESC"),
    COLLECTIBLE_ASC("COLLECTIBLE_ASC"),
    COLLECTIBLE_DESC("COLLECTIBLE_DESC"),
    SEARCH_MESSAGE_ASC("SEARCH_MESSAGE_ASC"),
    SEARCH_MESSAGE_DESC("SEARCH_MESSAGE_DESC"),
    LAST_MODIFIED_ASC("LAST_MODIFIED_ASC"),
    LAST_MODIFIED_DESC("LAST_MODIFIED_DESC"),
    CREATED_AT_ASC("CREATED_AT_ASC"),
    CREATED_AT_DESC("CREATED_AT_DESC");

    private final String graphqlName;

    private CollectionCardsOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
