package com.hiddenswitch.framework.graphql;


public class GameActions implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<SpellAction> all;
    private java.util.List<Integer> compatibility;

    public GameActions() {
    }

    public GameActions(java.util.List<SpellAction> all, java.util.List<Integer> compatibility) {
        this.all = all;
        this.compatibility = compatibility;
    }

    public java.util.List<SpellAction> getAll() {
        return all;
    }
    public void setAll(java.util.List<SpellAction> all) {
        this.all = all;
    }

    public java.util.List<Integer> getCompatibility() {
        return compatibility;
    }
    public void setCompatibility(java.util.List<Integer> compatibility) {
        this.compatibility = compatibility;
    }



    public static GameActions.Builder builder() {
        return new GameActions.Builder();
    }

    public static class Builder {

        private java.util.List<SpellAction> all;
        private java.util.List<Integer> compatibility;

        public Builder() {
        }

        public Builder setAll(java.util.List<SpellAction> all) {
            this.all = all;
            return this;
        }

        public Builder setCompatibility(java.util.List<Integer> compatibility) {
            this.compatibility = compatibility;
            return this;
        }


        public GameActions build() {
            return new GameActions(all, compatibility);
        }

    }
}
