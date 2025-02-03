package com.hiddenswitch.framework.graphql;


public class Deck implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private String nodeId;
    private String id;
    private String createdBy;
    private String lastEditedBy;
    private String name;
    private String heroClass;
    private boolean trashed;
    private String format;
    private int deckType;
    private boolean isPremade;
    private boolean permittedToDuplicate;
    private CardsInDecksConnection cardsInDecksByDeckId;
    private DeckPlayerAttributeTuplesConnection deckPlayerAttributeTuplesByDeckId;
    private DeckSharesConnection deckSharesByDeckId;
    private GameUsersConnection gameUsersByDeckId;
    private MatchmakingTicketsConnection matchmakingTicketsByBotDeckId;
    private MatchmakingTicketsConnection matchmakingTicketsByDeckId;

    public Deck() {
    }

    public Deck(String nodeId, String id, String createdBy, String lastEditedBy, String name, String heroClass, boolean trashed, String format, int deckType, boolean isPremade, boolean permittedToDuplicate, CardsInDecksConnection cardsInDecksByDeckId, DeckPlayerAttributeTuplesConnection deckPlayerAttributeTuplesByDeckId, DeckSharesConnection deckSharesByDeckId, GameUsersConnection gameUsersByDeckId, MatchmakingTicketsConnection matchmakingTicketsByBotDeckId, MatchmakingTicketsConnection matchmakingTicketsByDeckId) {
        this.nodeId = nodeId;
        this.id = id;
        this.createdBy = createdBy;
        this.lastEditedBy = lastEditedBy;
        this.name = name;
        this.heroClass = heroClass;
        this.trashed = trashed;
        this.format = format;
        this.deckType = deckType;
        this.isPremade = isPremade;
        this.permittedToDuplicate = permittedToDuplicate;
        this.cardsInDecksByDeckId = cardsInDecksByDeckId;
        this.deckPlayerAttributeTuplesByDeckId = deckPlayerAttributeTuplesByDeckId;
        this.deckSharesByDeckId = deckSharesByDeckId;
        this.gameUsersByDeckId = gameUsersByDeckId;
        this.matchmakingTicketsByBotDeckId = matchmakingTicketsByBotDeckId;
        this.matchmakingTicketsByDeckId = matchmakingTicketsByDeckId;
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

    /**
     * who created this deck originally
     */
    public String getCreatedBy() {
        return createdBy;
    }
    /**
     * who created this deck originally
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * who last edited this deck
     */
    public String getLastEditedBy() {
        return lastEditedBy;
    }
    /**
     * who last edited this deck
     */
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getHeroClass() {
        return heroClass;
    }
    public void setHeroClass(String heroClass) {
        this.heroClass = heroClass;
    }

    public boolean getTrashed() {
        return trashed;
    }
    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }

    public int getDeckType() {
        return deckType;
    }
    public void setDeckType(int deckType) {
        this.deckType = deckType;
    }

    /**
     * premades always shared with all users by application logic
     */
    public boolean getIsPremade() {
        return isPremade;
    }
    /**
     * premades always shared with all users by application logic
     */
    public void setIsPremade(boolean isPremade) {
        this.isPremade = isPremade;
    }

    public boolean getPermittedToDuplicate() {
        return permittedToDuplicate;
    }
    public void setPermittedToDuplicate(boolean permittedToDuplicate) {
        this.permittedToDuplicate = permittedToDuplicate;
    }

    /**
     * Reads and enables pagination through a set of `CardsInDeck`.
     */
    public CardsInDecksConnection getCardsInDecksByDeckId() {
        return cardsInDecksByDeckId;
    }
    /**
     * Reads and enables pagination through a set of `CardsInDeck`.
     */
    public void setCardsInDecksByDeckId(CardsInDecksConnection cardsInDecksByDeckId) {
        this.cardsInDecksByDeckId = cardsInDecksByDeckId;
    }

    /**
     * Reads and enables pagination through a set of `DeckPlayerAttributeTuple`.
     */
    public DeckPlayerAttributeTuplesConnection getDeckPlayerAttributeTuplesByDeckId() {
        return deckPlayerAttributeTuplesByDeckId;
    }
    /**
     * Reads and enables pagination through a set of `DeckPlayerAttributeTuple`.
     */
    public void setDeckPlayerAttributeTuplesByDeckId(DeckPlayerAttributeTuplesConnection deckPlayerAttributeTuplesByDeckId) {
        this.deckPlayerAttributeTuplesByDeckId = deckPlayerAttributeTuplesByDeckId;
    }

    /**
     * Reads and enables pagination through a set of `DeckShare`.
     */
    public DeckSharesConnection getDeckSharesByDeckId() {
        return deckSharesByDeckId;
    }
    /**
     * Reads and enables pagination through a set of `DeckShare`.
     */
    public void setDeckSharesByDeckId(DeckSharesConnection deckSharesByDeckId) {
        this.deckSharesByDeckId = deckSharesByDeckId;
    }

    /**
     * Reads and enables pagination through a set of `GameUser`.
     */
    public GameUsersConnection getGameUsersByDeckId() {
        return gameUsersByDeckId;
    }
    /**
     * Reads and enables pagination through a set of `GameUser`.
     */
    public void setGameUsersByDeckId(GameUsersConnection gameUsersByDeckId) {
        this.gameUsersByDeckId = gameUsersByDeckId;
    }

    /**
     * Reads and enables pagination through a set of `MatchmakingTicket`.
     */
    public MatchmakingTicketsConnection getMatchmakingTicketsByBotDeckId() {
        return matchmakingTicketsByBotDeckId;
    }
    /**
     * Reads and enables pagination through a set of `MatchmakingTicket`.
     */
    public void setMatchmakingTicketsByBotDeckId(MatchmakingTicketsConnection matchmakingTicketsByBotDeckId) {
        this.matchmakingTicketsByBotDeckId = matchmakingTicketsByBotDeckId;
    }

    /**
     * Reads and enables pagination through a set of `MatchmakingTicket`.
     */
    public MatchmakingTicketsConnection getMatchmakingTicketsByDeckId() {
        return matchmakingTicketsByDeckId;
    }
    /**
     * Reads and enables pagination through a set of `MatchmakingTicket`.
     */
    public void setMatchmakingTicketsByDeckId(MatchmakingTicketsConnection matchmakingTicketsByDeckId) {
        this.matchmakingTicketsByDeckId = matchmakingTicketsByDeckId;
    }



    public static Deck.Builder builder() {
        return new Deck.Builder();
    }

    public static class Builder {

        private String nodeId;
        private String id;
        private String createdBy;
        private String lastEditedBy;
        private String name;
        private String heroClass;
        private boolean trashed;
        private String format;
        private int deckType;
        private boolean isPremade;
        private boolean permittedToDuplicate;
        private CardsInDecksConnection cardsInDecksByDeckId;
        private DeckPlayerAttributeTuplesConnection deckPlayerAttributeTuplesByDeckId;
        private DeckSharesConnection deckSharesByDeckId;
        private GameUsersConnection gameUsersByDeckId;
        private MatchmakingTicketsConnection matchmakingTicketsByBotDeckId;
        private MatchmakingTicketsConnection matchmakingTicketsByDeckId;

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

        /**
         * who created this deck originally
         */
        public Builder setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * who last edited this deck
         */
        public Builder setLastEditedBy(String lastEditedBy) {
            this.lastEditedBy = lastEditedBy;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setHeroClass(String heroClass) {
            this.heroClass = heroClass;
            return this;
        }

        public Builder setTrashed(boolean trashed) {
            this.trashed = trashed;
            return this;
        }

        public Builder setFormat(String format) {
            this.format = format;
            return this;
        }

        public Builder setDeckType(int deckType) {
            this.deckType = deckType;
            return this;
        }

        /**
         * premades always shared with all users by application logic
         */
        public Builder setIsPremade(boolean isPremade) {
            this.isPremade = isPremade;
            return this;
        }

        public Builder setPermittedToDuplicate(boolean permittedToDuplicate) {
            this.permittedToDuplicate = permittedToDuplicate;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `CardsInDeck`.
         */
        public Builder setCardsInDecksByDeckId(CardsInDecksConnection cardsInDecksByDeckId) {
            this.cardsInDecksByDeckId = cardsInDecksByDeckId;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `DeckPlayerAttributeTuple`.
         */
        public Builder setDeckPlayerAttributeTuplesByDeckId(DeckPlayerAttributeTuplesConnection deckPlayerAttributeTuplesByDeckId) {
            this.deckPlayerAttributeTuplesByDeckId = deckPlayerAttributeTuplesByDeckId;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `DeckShare`.
         */
        public Builder setDeckSharesByDeckId(DeckSharesConnection deckSharesByDeckId) {
            this.deckSharesByDeckId = deckSharesByDeckId;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `GameUser`.
         */
        public Builder setGameUsersByDeckId(GameUsersConnection gameUsersByDeckId) {
            this.gameUsersByDeckId = gameUsersByDeckId;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `MatchmakingTicket`.
         */
        public Builder setMatchmakingTicketsByBotDeckId(MatchmakingTicketsConnection matchmakingTicketsByBotDeckId) {
            this.matchmakingTicketsByBotDeckId = matchmakingTicketsByBotDeckId;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `MatchmakingTicket`.
         */
        public Builder setMatchmakingTicketsByDeckId(MatchmakingTicketsConnection matchmakingTicketsByDeckId) {
            this.matchmakingTicketsByDeckId = matchmakingTicketsByDeckId;
            return this;
        }


        public Deck build() {
            return new Deck(nodeId, id, createdBy, lastEditedBy, name, heroClass, trashed, format, deckType, isPremade, permittedToDuplicate, cardsInDecksByDeckId, deckPlayerAttributeTuplesByDeckId, deckSharesByDeckId, gameUsersByDeckId, matchmakingTicketsByBotDeckId, matchmakingTicketsByDeckId);
        }

    }
}
