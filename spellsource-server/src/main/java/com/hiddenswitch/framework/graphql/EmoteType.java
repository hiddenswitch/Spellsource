package com.hiddenswitch.framework.graphql;

public enum EmoteType {

    HELLO("HELLO"),
    AMAZING("AMAZING"),
    WHOOPS("WHOOPS"),
    GOOD_GAME("GOOD_GAME"),
    FACE_MY_WRATH("FACE_MY_WRATH"),
    WELL_PLAYED("WELL_PLAYED");

    private final String graphqlName;

    private EmoteType(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
