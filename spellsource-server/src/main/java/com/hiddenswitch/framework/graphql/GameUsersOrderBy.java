package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `GameUser`.
 */
public enum GameUsersOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    PLAYER_INDEX_ASC("PLAYER_INDEX_ASC"),
    PLAYER_INDEX_DESC("PLAYER_INDEX_DESC"),
    GAME_ID_ASC("GAME_ID_ASC"),
    GAME_ID_DESC("GAME_ID_DESC"),
    USER_ID_ASC("USER_ID_ASC"),
    USER_ID_DESC("USER_ID_DESC"),
    DECK_ID_ASC("DECK_ID_ASC"),
    DECK_ID_DESC("DECK_ID_DESC"),
    VICTORY_STATUS_ASC("VICTORY_STATUS_ASC"),
    VICTORY_STATUS_DESC("VICTORY_STATUS_DESC");

    private final String graphqlName;

    private GameUsersOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
