package com.hiddenswitch.framework.graphql;


/**
 * A condition to be used against `Game` object types. All fields are tested for equality and combined with a logical ‘and.’
 */
public class GameCondition implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private GameStateEnum status;
    private String gitHash;
    private String trace;
    private String createdAt;

    public GameCondition() {
    }

    public GameCondition(String id, GameStateEnum status, String gitHash, String trace, String createdAt) {
        this.id = id;
        this.status = status;
        this.gitHash = gitHash;
        this.trace = trace;
        this.createdAt = createdAt;
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



    public static GameCondition.Builder builder() {
        return new GameCondition.Builder();
    }

    public static class Builder {

        private String id;
        private GameStateEnum status;
        private String gitHash;
        private String trace;
        private String createdAt;

        public Builder() {
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


        public GameCondition build() {
            return new GameCondition(id, status, gitHash, trace, createdAt);
        }

    }
}
