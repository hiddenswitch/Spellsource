package com.hiddenswitch.framework.graphql;


public class Emote implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private int entityId;
    private EmoteType message;

    public Emote() {
    }

    public Emote(int entityId, EmoteType message) {
        this.entityId = entityId;
        this.message = message;
    }

    public int getEntityId() {
        return entityId;
    }
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public EmoteType getMessage() {
        return message;
    }
    public void setMessage(EmoteType message) {
        this.message = message;
    }



    public static Emote.Builder builder() {
        return new Emote.Builder();
    }

    public static class Builder {

        private int entityId;
        private EmoteType message;

        public Builder() {
        }

        public Builder setEntityId(int entityId) {
            this.entityId = entityId;
            return this;
        }

        public Builder setMessage(EmoteType message) {
            this.message = message;
            return this;
        }


        public Emote build() {
            return new Emote(entityId, message);
        }

    }
}
