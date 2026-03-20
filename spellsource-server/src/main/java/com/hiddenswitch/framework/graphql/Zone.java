package com.hiddenswitch.framework.graphql;

public enum Zone {

    NONE("NONE"),
    HAND("HAND"),
    DECK("DECK"),
    GRAVEYARD("GRAVEYARD"),
    BATTLEFIELD("BATTLEFIELD"),
    SECRET("SECRET"),
    QUEST("QUEST"),
    HERO_POWER("HERO_POWER"),
    HERO("HERO"),
    WEAPON("WEAPON"),
    SET_ASIDE_ZONE("SET_ASIDE_ZONE"),
    HIDDEN("HIDDEN"),
    DISCOVER("DISCOVER"),
    REMOVED_FROM_PLAY("REMOVED_FROM_PLAY"),
    PLAYER("PLAYER"),
    ENCHANTMENT("ENCHANTMENT");

    private final String graphqlName;

    private Zone(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
