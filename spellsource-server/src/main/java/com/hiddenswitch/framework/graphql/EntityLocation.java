package com.hiddenswitch.framework.graphql;


/**
 * ─── Core game entity types ──────────────────────────────────
 */
public class EntityLocation implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int index;
    private Zone zone;
    private int player;

    public EntityLocation() {
    }

    public EntityLocation(int index, Zone zone, int player) {
        this.index = index;
        this.zone = zone;
        this.player = player;
    }

    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    public Zone getZone() {
        return zone;
    }
    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public int getPlayer() {
        return player;
    }
    public void setPlayer(int player) {
        this.player = player;
    }



    public static EntityLocation.Builder builder() {
        return new EntityLocation.Builder();
    }

    public static class Builder {

        private int index;
        private Zone zone;
        private int player;

        public Builder() {
        }

        public Builder setIndex(int index) {
            this.index = index;
            return this;
        }

        public Builder setZone(Zone zone) {
            this.zone = zone;
            return this;
        }

        public Builder setPlayer(int player) {
            this.player = player;
            return this;
        }


        public EntityLocation build() {
            return new EntityLocation(index, zone, player);
        }

    }
}
