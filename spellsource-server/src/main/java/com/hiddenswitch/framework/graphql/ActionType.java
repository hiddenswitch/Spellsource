package com.hiddenswitch.framework.graphql;

public enum ActionType {

    SYSTEM("SYSTEM"),
    END_TURN("END_TURN"),
    PHYSICAL_ATTACK("PHYSICAL_ATTACK"),
    SPELL("SPELL"),
    SUMMON("SUMMON"),
    HERO_POWER("HERO_POWER"),
    BATTLECRY("BATTLECRY"),
    EQUIP_WEAPON("EQUIP_WEAPON"),
    DISCOVER("DISCOVER"),
    HERO("HERO"),
    TAP("TAP");

    private final String graphqlName;

    private ActionType(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
