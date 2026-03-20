package com.hiddenswitch.framework.graphql;

public enum DraftStatus {

    IN_PROGRESS("IN_PROGRESS"),
    SELECT_HERO("SELECT_HERO"),
    COMPLETE("COMPLETE"),
    RETIRED("RETIRED");

    private final String graphqlName;

    private DraftStatus(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
