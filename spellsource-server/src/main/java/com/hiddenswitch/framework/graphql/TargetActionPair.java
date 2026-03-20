package com.hiddenswitch.framework.graphql;


/**
 * ─── Game types (real-time game state) ────────────────────────
 */
public class TargetActionPair implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int action;
    private int target;
    private int friendlyBattlefieldIndex;

    public TargetActionPair() {
    }

    public TargetActionPair(int action, int target, int friendlyBattlefieldIndex) {
        this.action = action;
        this.target = target;
        this.friendlyBattlefieldIndex = friendlyBattlefieldIndex;
    }

    public int getAction() {
        return action;
    }
    public void setAction(int action) {
        this.action = action;
    }

    public int getTarget() {
        return target;
    }
    public void setTarget(int target) {
        this.target = target;
    }

    public int getFriendlyBattlefieldIndex() {
        return friendlyBattlefieldIndex;
    }
    public void setFriendlyBattlefieldIndex(int friendlyBattlefieldIndex) {
        this.friendlyBattlefieldIndex = friendlyBattlefieldIndex;
    }



    public static TargetActionPair.Builder builder() {
        return new TargetActionPair.Builder();
    }

    public static class Builder {

        private int action;
        private int target;
        private int friendlyBattlefieldIndex;

        public Builder() {
        }

        public Builder setAction(int action) {
            this.action = action;
            return this;
        }

        public Builder setTarget(int target) {
            this.target = target;
            return this;
        }

        public Builder setFriendlyBattlefieldIndex(int friendlyBattlefieldIndex) {
            this.friendlyBattlefieldIndex = friendlyBattlefieldIndex;
            return this;
        }


        public TargetActionPair build() {
            return new TargetActionPair(action, target, friendlyBattlefieldIndex);
        }

    }
}
