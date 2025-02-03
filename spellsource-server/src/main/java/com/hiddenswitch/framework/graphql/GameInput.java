package com.hiddenswitch.framework.graphql;


/**
 * An input for mutations affecting `Game`
 */
public class GameInput implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private GameStateEnum status;
    private String gitHash;
    private String trace;
    private String createdAt;

    public GameInput() {
    }

    public GameInput(GameStateEnum status, String gitHash, String trace, String createdAt) {
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



    public static GameInput.Builder builder() {
        return new GameInput.Builder();
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


        public GameInput build() {
            return new GameInput(status, gitHash, trace, createdAt);
        }

    }
}
