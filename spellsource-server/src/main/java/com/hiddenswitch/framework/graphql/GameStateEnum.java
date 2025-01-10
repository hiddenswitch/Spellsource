package com.hiddenswitch.framework.graphql;

public enum GameStateEnum {

    AWAITING_CONNECTIONS("AWAITING_CONNECTIONS"),
    STARTED("STARTED"),
    FINISHED("FINISHED");

    private final String graphqlName;

    private GameStateEnum(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
