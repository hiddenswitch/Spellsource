package com.hiddenswitch.framework.graphql;

public enum Presence {

    UNKNOWN("UNKNOWN"),
    OFFLINE("OFFLINE"),
    IN_GAME("IN_GAME"),
    ONLINE("ONLINE");

    private final String graphqlName;

    private Presence(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
