package com.hiddenswitch.framework.graphql;

public enum GameUserVictoryEnum {

    UNKNOWN("UNKNOWN"),
    WON("WON"),
    LOST("LOST"),
    DISCONNECTED("DISCONNECTED"),
    CONCEDED("CONCEDED");

    private final String graphqlName;

    private GameUserVictoryEnum(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
