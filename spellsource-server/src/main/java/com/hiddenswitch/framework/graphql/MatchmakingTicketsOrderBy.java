package com.hiddenswitch.framework.graphql;

/**
 * Methods to use when ordering `MatchmakingTicket`.
 */
public enum MatchmakingTicketsOrderBy {

    NATURAL("NATURAL"),
    PRIMARY_KEY_ASC("PRIMARY_KEY_ASC"),
    PRIMARY_KEY_DESC("PRIMARY_KEY_DESC"),
    TICKET_ID_ASC("TICKET_ID_ASC"),
    TICKET_ID_DESC("TICKET_ID_DESC"),
    QUEUE_ID_ASC("QUEUE_ID_ASC"),
    QUEUE_ID_DESC("QUEUE_ID_DESC"),
    USER_ID_ASC("USER_ID_ASC"),
    USER_ID_DESC("USER_ID_DESC"),
    DECK_ID_ASC("DECK_ID_ASC"),
    DECK_ID_DESC("DECK_ID_DESC"),
    BOT_DECK_ID_ASC("BOT_DECK_ID_ASC"),
    BOT_DECK_ID_DESC("BOT_DECK_ID_DESC"),
    CREATED_AT_ASC("CREATED_AT_ASC"),
    CREATED_AT_DESC("CREATED_AT_DESC");

    private final String graphqlName;

    private MatchmakingTicketsOrderBy(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
