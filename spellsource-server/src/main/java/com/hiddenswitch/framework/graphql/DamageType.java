package com.hiddenswitch.framework.graphql;

public enum DamageType {

    PHYSICAL("PHYSICAL"),
    FATIGUE("FATIGUE"),
    MAGICAL("MAGICAL"),
    DECAY_DAMAGE("DECAY_DAMAGE"),
    DEFLECT_DAMAGE("DEFLECT_DAMAGE"),
    DRAIN_DAMAGE("DRAIN_DAMAGE"),
    IGNORES_ARMOR("IGNORES_ARMOR"),
    SPLASH("SPLASH");

    private final String graphqlName;

    private DamageType(String graphqlName) {
        this.graphqlName = graphqlName;
    }

    @Override
    public String toString() {
        return this.graphqlName;
    }

}
