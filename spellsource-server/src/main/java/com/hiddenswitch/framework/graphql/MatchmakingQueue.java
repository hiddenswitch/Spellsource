package com.hiddenswitch.framework.graphql;


public class MatchmakingQueue implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String queueId;
    private String name;
    private String description;
    private String tooltip;
    private MatchmakingQueueRequires requires;

    public MatchmakingQueue() {
    }

    public MatchmakingQueue(String queueId, String name, String description, String tooltip, MatchmakingQueueRequires requires) {
        this.queueId = queueId;
        this.name = name;
        this.description = description;
        this.tooltip = tooltip;
        this.requires = requires;
    }

    public String getQueueId() {
        return queueId;
    }
    public void setQueueId(String queueId) {
        this.queueId = queueId;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getTooltip() {
        return tooltip;
    }
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public MatchmakingQueueRequires getRequires() {
        return requires;
    }
    public void setRequires(MatchmakingQueueRequires requires) {
        this.requires = requires;
    }



    public static MatchmakingQueue.Builder builder() {
        return new MatchmakingQueue.Builder();
    }

    public static class Builder {

        private String queueId;
        private String name;
        private String description;
        private String tooltip;
        private MatchmakingQueueRequires requires;

        public Builder() {
        }

        public Builder setQueueId(String queueId) {
            this.queueId = queueId;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setTooltip(String tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder setRequires(MatchmakingQueueRequires requires) {
            this.requires = requires;
            return this;
        }


        public MatchmakingQueue build() {
            return new MatchmakingQueue(queueId, name, description, tooltip, requires);
        }

    }
}
