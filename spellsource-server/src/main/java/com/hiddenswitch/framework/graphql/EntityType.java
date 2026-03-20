package com.hiddenswitch.framework.graphql;

public enum EntityType {

    ANY("ANY"),
    ACTOR("ACTOR"),
    HERO("HERO"),
    MINION("MINION"),
    WEAPON("WEAPON"),
    CARD("CARD"),
    PLAYER("PLAYER"),
    ENCHANTMENT("ENCHANTMENT"),
    QUEST("QUEST"),
    SECRET("SECRET");

    private final String graphqlName;

    private EntityType(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
