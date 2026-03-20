package com.hiddenswitch.framework.graphql;


public class GameState implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private java.util.List<Entity> entities;
    private boolean isLocalPlayerTurn;
    private int turnNumber;
    private String turnState;
    private java.lang.Long timestamp;

    public GameState() {
    }

    public GameState(java.util.List<Entity> entities, boolean isLocalPlayerTurn, int turnNumber, String turnState, java.lang.Long timestamp) {
        this.entities = entities;
        this.isLocalPlayerTurn = isLocalPlayerTurn;
        this.turnNumber = turnNumber;
        this.turnState = turnState;
        this.timestamp = timestamp;
    }

    public java.util.List<Entity> getEntities() {
        return entities;
    }
    public void setEntities(java.util.List<Entity> entities) {
        this.entities = entities;
    }

    public boolean getIsLocalPlayerTurn() {
        return isLocalPlayerTurn;
    }
    public void setIsLocalPlayerTurn(boolean isLocalPlayerTurn) {
        this.isLocalPlayerTurn = isLocalPlayerTurn;
    }

    public int getTurnNumber() {
        return turnNumber;
    }
    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public String getTurnState() {
        return turnState;
    }
    public void setTurnState(String turnState) {
        this.turnState = turnState;
    }

    public java.lang.Long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(java.lang.Long timestamp) {
        this.timestamp = timestamp;
    }



    public static GameState.Builder builder() {
        return new GameState.Builder();
    }

    public static class Builder {

        private java.util.List<Entity> entities;
        private boolean isLocalPlayerTurn;
        private int turnNumber;
        private String turnState;
        private java.lang.Long timestamp;

        public Builder() {
        }

        public Builder setEntities(java.util.List<Entity> entities) {
            this.entities = entities;
            return this;
        }

        public Builder setIsLocalPlayerTurn(boolean isLocalPlayerTurn) {
            this.isLocalPlayerTurn = isLocalPlayerTurn;
            return this;
        }

        public Builder setTurnNumber(int turnNumber) {
            this.turnNumber = turnNumber;
            return this;
        }

        public Builder setTurnState(String turnState) {
            this.turnState = turnState;
            return this;
        }

        public Builder setTimestamp(java.lang.Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }


        public GameState build() {
            return new GameState(entities, isLocalPlayerTurn, turnNumber, turnState, timestamp);
        }

    }
}
