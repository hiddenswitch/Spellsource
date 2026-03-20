package com.hiddenswitch.framework.graphql;

/**
 * ─── Enums ────────────────────────────────────────────────────
 */
public enum CardType {

    HERO("HERO"),
    MINION("MINION"),
    SPELL("SPELL"),
    WEAPON("WEAPON"),
    HERO_POWER("HERO_POWER"),
    GROUP("GROUP"),
    CHOOSE_ONE("CHOOSE_ONE"),
    ENCHANTMENT("ENCHANTMENT"),
    CLASS("CLASS"),
    FORMAT("FORMAT"),
    ROGUE_CHOICE("ROGUE_CHOICE");

    private final String graphqlName;

    private CardType(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
