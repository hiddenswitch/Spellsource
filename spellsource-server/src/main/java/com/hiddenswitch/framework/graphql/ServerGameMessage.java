package com.hiddenswitch.framework.graphql;


/**
 * A message from the server during a game. The messageType field indicates which
fields are populated. Clients should switch on messageType to process the message.
 */
public class ServerGameMessage implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private MessageType messageType;
    private String id;
    private int localPlayerId;
    private boolean isReplayMessage;
    private GameState gameState;
    private GameActions actions;
    private java.util.List<Entity> startingCards;
    private GameEvent event;
    private GameOver gameOver;
    private Emote emote;
    private Timers timers;

    public ServerGameMessage() {
    }

    public ServerGameMessage(MessageType messageType, String id, int localPlayerId, boolean isReplayMessage, GameState gameState, GameActions actions, java.util.List<Entity> startingCards, GameEvent event, GameOver gameOver, Emote emote, Timers timers) {
        this.messageType = messageType;
        this.id = id;
        this.localPlayerId = localPlayerId;
        this.isReplayMessage = isReplayMessage;
        this.gameState = gameState;
        this.actions = actions;
        this.startingCards = startingCards;
        this.event = event;
        this.gameOver = gameOver;
        this.emote = emote;
        this.timers = timers;
    }

    public MessageType getMessageType() {
        return messageType;
    }
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public int getLocalPlayerId() {
        return localPlayerId;
    }
    public void setLocalPlayerId(int localPlayerId) {
        this.localPlayerId = localPlayerId;
    }

    public boolean getIsReplayMessage() {
        return isReplayMessage;
    }
    public void setIsReplayMessage(boolean isReplayMessage) {
        this.isReplayMessage = isReplayMessage;
    }

    /**
     * Present on ON_UPDATE and ON_REQUEST_ACTION messages.
     */
    public GameState getGameState() {
        return gameState;
    }
    /**
     * Present on ON_UPDATE and ON_REQUEST_ACTION messages.
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    /**
     * Present on ON_REQUEST_ACTION messages. The actions the player can take.
     */
    public GameActions getActions() {
        return actions;
    }
    /**
     * Present on ON_REQUEST_ACTION messages. The actions the player can take.
     */
    public void setActions(GameActions actions) {
        this.actions = actions;
    }

    /**
     * Present on ON_MULLIGAN messages. The starting cards to consider discarding.
     */
    public java.util.List<Entity> getStartingCards() {
        return startingCards;
    }
    /**
     * Present on ON_MULLIGAN messages. The starting cards to consider discarding.
     */
    public void setStartingCards(java.util.List<Entity> startingCards) {
        this.startingCards = startingCards;
    }

    /**
     * Present on ON_GAME_EVENT messages.
     */
    public GameEvent getEvent() {
        return event;
    }
    /**
     * Present on ON_GAME_EVENT messages.
     */
    public void setEvent(GameEvent event) {
        this.event = event;
    }

    /**
     * Present on ON_GAME_END messages.
     */
    public GameOver getGameOver() {
        return gameOver;
    }
    /**
     * Present on ON_GAME_END messages.
     */
    public void setGameOver(GameOver gameOver) {
        this.gameOver = gameOver;
    }

    /**
     * Present on EMOTE messages.
     */
    public Emote getEmote() {
        return emote;
    }
    /**
     * Present on EMOTE messages.
     */
    public void setEmote(Emote emote) {
        this.emote = emote;
    }

    /**
     * Present on TIMER messages.
     */
    public Timers getTimers() {
        return timers;
    }
    /**
     * Present on TIMER messages.
     */
    public void setTimers(Timers timers) {
        this.timers = timers;
    }



    public static ServerGameMessage.Builder builder() {
        return new ServerGameMessage.Builder();
    }

    public static class Builder {

        private MessageType messageType;
        private String id;
        private int localPlayerId;
        private boolean isReplayMessage;
        private GameState gameState;
        private GameActions actions;
        private java.util.List<Entity> startingCards;
        private GameEvent event;
        private GameOver gameOver;
        private Emote emote;
        private Timers timers;

        public Builder() {
        }

        public Builder setMessageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setLocalPlayerId(int localPlayerId) {
            this.localPlayerId = localPlayerId;
            return this;
        }

        public Builder setIsReplayMessage(boolean isReplayMessage) {
            this.isReplayMessage = isReplayMessage;
            return this;
        }

        /**
         * Present on ON_UPDATE and ON_REQUEST_ACTION messages.
         */
        public Builder setGameState(GameState gameState) {
            this.gameState = gameState;
            return this;
        }

        /**
         * Present on ON_REQUEST_ACTION messages. The actions the player can take.
         */
        public Builder setActions(GameActions actions) {
            this.actions = actions;
            return this;
        }

        /**
         * Present on ON_MULLIGAN messages. The starting cards to consider discarding.
         */
        public Builder setStartingCards(java.util.List<Entity> startingCards) {
            this.startingCards = startingCards;
            return this;
        }

        /**
         * Present on ON_GAME_EVENT messages.
         */
        public Builder setEvent(GameEvent event) {
            this.event = event;
            return this;
        }

        /**
         * Present on ON_GAME_END messages.
         */
        public Builder setGameOver(GameOver gameOver) {
            this.gameOver = gameOver;
            return this;
        }

        /**
         * Present on EMOTE messages.
         */
        public Builder setEmote(Emote emote) {
            this.emote = emote;
            return this;
        }

        /**
         * Present on TIMER messages.
         */
        public Builder setTimers(Timers timers) {
            this.timers = timers;
            return this;
        }


        public ServerGameMessage build() {
            return new ServerGameMessage(messageType, id, localPlayerId, isReplayMessage, gameState, actions, startingCards, event, gameOver, emote, timers);
        }

    }
}
