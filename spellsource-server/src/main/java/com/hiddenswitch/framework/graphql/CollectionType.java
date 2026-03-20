package com.hiddenswitch.framework.graphql;

public enum CollectionType {

    USER("USER"),
    ALLIANCE("ALLIANCE"),
    DECK("DECK");

    private final String graphqlName;

    private CollectionType(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
