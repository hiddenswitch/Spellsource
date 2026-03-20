package com.hiddenswitch.framework.graphql;

public enum DeckType {

    DRAFT("DRAFT"),
    CONSTRUCTED("CONSTRUCTED"),
    ROGUE("ROGUE");

    private final String graphqlName;

    private DeckType(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
