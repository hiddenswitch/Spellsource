package com.hiddenswitch.framework.graphql;


/**
 * Represents an update to a `Game`. Fields that are set will be updated.
 */
public class GamePatch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private GameStateEnum status;
    private String gitHash;
    private String trace;
    private String createdAt;

    public GamePatch() {
    }

    public GamePatch(GameStateEnum status, String gitHash, String trace, String createdAt) {
        this.status = status;
        this.gitHash = gitHash;
        this.trace = trace;
        this.createdAt = createdAt;
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



    public static GamePatch.Builder builder() {
        return new GamePatch.Builder();
    }

    public static class Builder {

        private GameStateEnum status;
        private String gitHash;
        private String trace;
        private String createdAt;

        public Builder() {
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


        public GamePatch build() {
            return new GamePatch(status, gitHash, trace, createdAt);
        }

    }
}
