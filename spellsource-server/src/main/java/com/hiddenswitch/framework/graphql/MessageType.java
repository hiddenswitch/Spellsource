package com.hiddenswitch.framework.graphql;

public enum MessageType {

    UPDATE_ACTION("UPDATE_ACTION"),
    ON_GAME_EVENT("ON_GAME_EVENT"),
    ON_GAME_END("ON_GAME_END"),
    ON_UPDATE("ON_UPDATE"),
    ON_REQUEST_ACTION("ON_REQUEST_ACTION"),
    FIRST_MESSAGE("FIRST_MESSAGE"),
    ON_MULLIGAN("ON_MULLIGAN"),
    UPDATE_MULLIGAN("UPDATE_MULLIGAN"),
    EMOTE("EMOTE"),
    TOUCH("TOUCH"),
    CONCEDE("CONCEDE"),
    PINGPONG("PINGPONG"),
    TIMER("TIMER");

    private final String graphqlName;

    private MessageType(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
