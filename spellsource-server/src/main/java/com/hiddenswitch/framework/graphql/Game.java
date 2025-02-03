package com.hiddenswitch.framework.graphql;


public class Game implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String id;
    private GameStateEnum status;
    private String gitHash;
    private String trace;
    private String createdAt;
    private GameUsersConnection gameUsersByGameId;

    public Game() {
    }

    public Game(String nodeId, String id, GameStateEnum status, String gitHash, String trace, String createdAt, GameUsersConnection gameUsersByGameId) {
        this.nodeId = nodeId;
        this.id = id;
        this.status = status;
        this.gitHash = gitHash;
        this.trace = trace;
        this.createdAt = createdAt;
        this.gameUsersByGameId = gameUsersByGameId;
    }

    /**
     * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
     */
    public String getNodeId() {
        return nodeId;
    }
    /**
     * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public GameStateEnum getStatus() {
        return status;
    }
    public void setStatus(GameStateEnum status) {
        this.status = status;
    }

    public String getGitHash() {
        return gitHash;
    }
    public void setGitHash(String gitHash) {
        this.gitHash = gitHash;
    }

    public String getTrace() {
        return trace;
    }
    public void setTrace(String trace) {
        this.trace = trace;
    }

    public String getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Reads and enables pagination through a set of `GameUser`.
     */
    public GameUsersConnection getGameUsersByGameId() {
        return gameUsersByGameId;
    }
    /**
     * Reads and enables pagination through a set of `GameUser`.
     */
    public void setGameUsersByGameId(GameUsersConnection gameUsersByGameId) {
        this.gameUsersByGameId = gameUsersByGameId;
    }



    public static Game.Builder builder() {
        return new Game.Builder();
    }

    public static class Builder {

        private String nodeId;
        private String id;
        private GameStateEnum status;
        private String gitHash;
        private String trace;
        private String createdAt;
        private GameUsersConnection gameUsersByGameId;

        public Builder() {
        }

        /**
         * A globally unique identifier. Can be used in various places throughout the system to identify this single value.
         */
        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setStatus(GameStateEnum status) {
            this.status = status;
            return this;
        }

        public Builder setGitHash(String gitHash) {
            this.gitHash = gitHash;
            return this;
        }

        public Builder setTrace(String trace) {
            this.trace = trace;
            return this;
        }

        public Builder setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `GameUser`.
         */
        public Builder setGameUsersByGameId(GameUsersConnection gameUsersByGameId) {
            this.gameUsersByGameId = gameUsersByGameId;
            return this;
        }


        public Game build() {
            return new Game(nodeId, id, status, gitHash, trace, createdAt, gameUsersByGameId);
        }

    }
}
