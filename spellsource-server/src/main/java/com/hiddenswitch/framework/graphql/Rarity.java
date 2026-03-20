package com.hiddenswitch.framework.graphql;

public enum Rarity {

    FREE("FREE"),
    COMMON("COMMON"),
    RARE("RARE"),
    EPIC("EPIC"),
    LEGENDARY("LEGENDARY"),
    ALLIANCE("ALLIANCE");

    private final String graphqlName;

    private Rarity(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
