package com.hiddenswitch.framework.graphql;

public enum PlayerEntityAttribute {

    SIGNATURE("SIGNATURE");

    private final String graphqlName;

    private PlayerEntityAttribute(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
