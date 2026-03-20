package com.hiddenswitch.framework.graphql;

public enum InviteStatus {

    UNDELIVERED("UNDELIVERED"),
    PENDING("PENDING"),
    TIMEOUT("TIMEOUT"),
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED"),
    CANCELLED("CANCELLED");

    private final String graphqlName;

    private InviteStatus(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
