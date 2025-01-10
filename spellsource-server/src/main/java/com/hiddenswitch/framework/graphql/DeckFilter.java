package com.hiddenswitch.framework.graphql;


/**
 * A filter to be used against `Deck` object types. All fields are combined with a logical ‘and.’
 */
public class DeckFilter implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private StringFilter id;
    private StringFilter createdBy;
    private StringFilter lastEditedBy;
    private StringFilter name;
    private StringFilter heroClass;
    private BooleanFilter trashed;
    private StringFilter format;
    private IntFilter deckType;
    private BooleanFilter isPremade;
    private BooleanFilter permittedToDuplicate;
    private DeckToManyCardsInDeckFilter cardsInDecksByDeckId;
    private Boolean cardsInDecksByDeckIdExist;
    private DeckToManyDeckPlayerAttributeTupleFilter deckPlayerAttributeTuplesByDeckId;
    private Boolean deckPlayerAttributeTuplesByDeckIdExist;
    private DeckToManyDeckShareFilter deckSharesByDeckId;
    private Boolean deckSharesByDeckIdExist;
    private DeckToManyGameUserFilter gameUsersByDeckId;
    private Boolean gameUsersByDeckIdExist;
    private DeckToManyMatchmakingTicketFilter matchmakingTicketsByBotDeckId;
    private Boolean matchmakingTicketsByBotDeckIdExist;
    private DeckToManyMatchmakingTicketFilter matchmakingTicketsByDeckId;
    private Boolean matchmakingTicketsByDeckIdExist;
    private java.util.List<DeckFilter> and;
    private java.util.List<DeckFilter> or;
    private DeckFilter not;

    public DeckFilter() {
    }

    public DeckFilter(StringFilter id, StringFilter createdBy, StringFilter lastEditedBy, StringFilter name, StringFilter heroClass, BooleanFilter trashed, StringFilter format, IntFilter deckType, BooleanFilter isPremade, BooleanFilter permittedToDuplicate, DeckToManyCardsInDeckFilter cardsInDecksByDeckId, Boolean cardsInDecksByDeckIdExist, DeckToManyDeckPlayerAttributeTupleFilter deckPlayerAttributeTuplesByDeckId, Boolean deckPlayerAttributeTuplesByDeckIdExist, DeckToManyDeckShareFilter deckSharesByDeckId, Boolean deckSharesByDeckIdExist, DeckToManyGameUserFilter gameUsersByDeckId, Boolean gameUsersByDeckIdExist, DeckToManyMatchmakingTicketFilter matchmakingTicketsByBotDeckId, Boolean matchmakingTicketsByBotDeckIdExist, DeckToManyMatchmakingTicketFilter matchmakingTicketsByDeckId, Boolean matchmakingTicketsByDeckIdExist, java.util.List<DeckFilter> and, java.util.List<DeckFilter> or, DeckFilter not) {
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
        this.cardsInDecksByDeckIdExist = cardsInDecksByDeckIdExist;
        this.deckPlayerAttributeTuplesByDeckId = deckPlayerAttributeTuplesByDeckId;
        this.deckPlayerAttributeTuplesByDeckIdExist = deckPlayerAttributeTuplesByDeckIdExist;
        this.deckSharesByDeckId = deckSharesByDeckId;
        this.deckSharesByDeckIdExist = deckSharesByDeckIdExist;
        this.gameUsersByDeckId = gameUsersByDeckId;
        this.gameUsersByDeckIdExist = gameUsersByDeckIdExist;
        this.matchmakingTicketsByBotDeckId = matchmakingTicketsByBotDeckId;
        this.matchmakingTicketsByBotDeckIdExist = matchmakingTicketsByBotDeckIdExist;
        this.matchmakingTicketsByDeckId = matchmakingTicketsByDeckId;
        this.matchmakingTicketsByDeckIdExist = matchmakingTicketsByDeckIdExist;
        this.and = and;
        this.or = or;
        this.not = not;
    }

    public StringFilter getId() {
        return id;
    }
    public void setId(StringFilter id) {
        this.id = id;
    }

    public StringFilter getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(StringFilter createdBy) {
        this.createdBy = createdBy;
    }

    public StringFilter getLastEditedBy() {
        return lastEditedBy;
    }
    public void setLastEditedBy(StringFilter lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    public StringFilter getName() {
        return name;
    }
    public void setName(StringFilter name) {
        this.name = name;
    }

    public StringFilter getHeroClass() {
        return heroClass;
    }
    public void setHeroClass(StringFilter heroClass) {
        this.heroClass = heroClass;
    }

    public BooleanFilter getTrashed() {
        return trashed;
    }
    public void setTrashed(BooleanFilter trashed) {
        this.trashed = trashed;
    }

    public StringFilter getFormat() {
        return format;
    }
    public void setFormat(StringFilter format) {
        this.format = format;
    }

    public IntFilter getDeckType() {
        return deckType;
    }
    public void setDeckType(IntFilter deckType) {
        this.deckType = deckType;
    }

    public BooleanFilter getIsPremade() {
        return isPremade;
    }
    public void setIsPremade(BooleanFilter isPremade) {
        this.isPremade = isPremade;
    }

    public BooleanFilter getPermittedToDuplicate() {
        return permittedToDuplicate;
    }
    public void setPermittedToDuplicate(BooleanFilter permittedToDuplicate) {
        this.permittedToDuplicate = permittedToDuplicate;
    }

    public DeckToManyCardsInDeckFilter getCardsInDecksByDeckId() {
        return cardsInDecksByDeckId;
    }
    public void setCardsInDecksByDeckId(DeckToManyCardsInDeckFilter cardsInDecksByDeckId) {
        this.cardsInDecksByDeckId = cardsInDecksByDeckId;
    }

    public Boolean getCardsInDecksByDeckIdExist() {
        return cardsInDecksByDeckIdExist;
    }
    public void setCardsInDecksByDeckIdExist(Boolean cardsInDecksByDeckIdExist) {
        this.cardsInDecksByDeckIdExist = cardsInDecksByDeckIdExist;
    }

    public DeckToManyDeckPlayerAttributeTupleFilter getDeckPlayerAttributeTuplesByDeckId() {
        return deckPlayerAttributeTuplesByDeckId;
    }
    public void setDeckPlayerAttributeTuplesByDeckId(DeckToManyDeckPlayerAttributeTupleFilter deckPlayerAttributeTuplesByDeckId) {
        this.deckPlayerAttributeTuplesByDeckId = deckPlayerAttributeTuplesByDeckId;
    }

    public Boolean getDeckPlayerAttributeTuplesByDeckIdExist() {
        return deckPlayerAttributeTuplesByDeckIdExist;
    }
    public void setDeckPlayerAttributeTuplesByDeckIdExist(Boolean deckPlayerAttributeTuplesByDeckIdExist) {
        this.deckPlayerAttributeTuplesByDeckIdExist = deckPlayerAttributeTuplesByDeckIdExist;
    }

    public DeckToManyDeckShareFilter getDeckSharesByDeckId() {
        return deckSharesByDeckId;
    }
    public void setDeckSharesByDeckId(DeckToManyDeckShareFilter deckSharesByDeckId) {
        this.deckSharesByDeckId = deckSharesByDeckId;
    }

    public Boolean getDeckSharesByDeckIdExist() {
        return deckSharesByDeckIdExist;
    }
    public void setDeckSharesByDeckIdExist(Boolean deckSharesByDeckIdExist) {
        this.deckSharesByDeckIdExist = deckSharesByDeckIdExist;
    }

    public DeckToManyGameUserFilter getGameUsersByDeckId() {
        return gameUsersByDeckId;
    }
    public void setGameUsersByDeckId(DeckToManyGameUserFilter gameUsersByDeckId) {
        this.gameUsersByDeckId = gameUsersByDeckId;
    }

    public Boolean getGameUsersByDeckIdExist() {
        return gameUsersByDeckIdExist;
    }
    public void setGameUsersByDeckIdExist(Boolean gameUsersByDeckIdExist) {
        this.gameUsersByDeckIdExist = gameUsersByDeckIdExist;
    }

    public DeckToManyMatchmakingTicketFilter getMatchmakingTicketsByBotDeckId() {
        return matchmakingTicketsByBotDeckId;
    }
    public void setMatchmakingTicketsByBotDeckId(DeckToManyMatchmakingTicketFilter matchmakingTicketsByBotDeckId) {
        this.matchmakingTicketsByBotDeckId = matchmakingTicketsByBotDeckId;
    }

    public Boolean getMatchmakingTicketsByBotDeckIdExist() {
        return matchmakingTicketsByBotDeckIdExist;
    }
    public void setMatchmakingTicketsByBotDeckIdExist(Boolean matchmakingTicketsByBotDeckIdExist) {
        this.matchmakingTicketsByBotDeckIdExist = matchmakingTicketsByBotDeckIdExist;
    }

    public DeckToManyMatchmakingTicketFilter getMatchmakingTicketsByDeckId() {
        return matchmakingTicketsByDeckId;
    }
    public void setMatchmakingTicketsByDeckId(DeckToManyMatchmakingTicketFilter matchmakingTicketsByDeckId) {
        this.matchmakingTicketsByDeckId = matchmakingTicketsByDeckId;
    }

    public Boolean getMatchmakingTicketsByDeckIdExist() {
        return matchmakingTicketsByDeckIdExist;
    }
    public void setMatchmakingTicketsByDeckIdExist(Boolean matchmakingTicketsByDeckIdExist) {
        this.matchmakingTicketsByDeckIdExist = matchmakingTicketsByDeckIdExist;
    }

    public java.util.List<DeckFilter> getAnd() {
        return and;
    }
    public void setAnd(java.util.List<DeckFilter> and) {
        this.and = and;
    }

    public java.util.List<DeckFilter> getOr() {
        return or;
    }
    public void setOr(java.util.List<DeckFilter> or) {
        this.or = or;
    }

    public DeckFilter getNot() {
        return not;
    }
    public void setNot(DeckFilter not) {
        this.not = not;
    }



    public static DeckFilter.Builder builder() {
        return new DeckFilter.Builder();
    }

    public static class Builder {

        private StringFilter id;
        private StringFilter createdBy;
        private StringFilter lastEditedBy;
        private StringFilter name;
        private StringFilter heroClass;
        private BooleanFilter trashed;
        private StringFilter format;
        private IntFilter deckType;
        private BooleanFilter isPremade;
        private BooleanFilter permittedToDuplicate;
        private DeckToManyCardsInDeckFilter cardsInDecksByDeckId;
        private Boolean cardsInDecksByDeckIdExist;
        private DeckToManyDeckPlayerAttributeTupleFilter deckPlayerAttributeTuplesByDeckId;
        private Boolean deckPlayerAttributeTuplesByDeckIdExist;
        private DeckToManyDeckShareFilter deckSharesByDeckId;
        private Boolean deckSharesByDeckIdExist;
        private DeckToManyGameUserFilter gameUsersByDeckId;
        private Boolean gameUsersByDeckIdExist;
        private DeckToManyMatchmakingTicketFilter matchmakingTicketsByBotDeckId;
        private Boolean matchmakingTicketsByBotDeckIdExist;
        private DeckToManyMatchmakingTicketFilter matchmakingTicketsByDeckId;
        private Boolean matchmakingTicketsByDeckIdExist;
        private java.util.List<DeckFilter> and;
        private java.util.List<DeckFilter> or;
        private DeckFilter not;

        public Builder() {
        }

        public Builder setId(StringFilter id) {
            this.id = id;
            return this;
        }

        public Builder setCreatedBy(StringFilter createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder setLastEditedBy(StringFilter lastEditedBy) {
            this.lastEditedBy = lastEditedBy;
            return this;
        }

        public Builder setName(StringFilter name) {
            this.name = name;
            return this;
        }

        public Builder setHeroClass(StringFilter heroClass) {
            this.heroClass = heroClass;
            return this;
        }

        public Builder setTrashed(BooleanFilter trashed) {
            this.trashed = trashed;
            return this;
        }

        public Builder setFormat(StringFilter format) {
            this.format = format;
            return this;
        }

        public Builder setDeckType(IntFilter deckType) {
            this.deckType = deckType;
            return this;
        }

        public Builder setIsPremade(BooleanFilter isPremade) {
            this.isPremade = isPremade;
            return this;
        }

        public Builder setPermittedToDuplicate(BooleanFilter permittedToDuplicate) {
            this.permittedToDuplicate = permittedToDuplicate;
            return this;
        }

        public Builder setCardsInDecksByDeckId(DeckToManyCardsInDeckFilter cardsInDecksByDeckId) {
            this.cardsInDecksByDeckId = cardsInDecksByDeckId;
            return this;
        }

        public Builder setCardsInDecksByDeckIdExist(Boolean cardsInDecksByDeckIdExist) {
            this.cardsInDecksByDeckIdExist = cardsInDecksByDeckIdExist;
            return this;
        }

        public Builder setDeckPlayerAttributeTuplesByDeckId(DeckToManyDeckPlayerAttributeTupleFilter deckPlayerAttributeTuplesByDeckId) {
            this.deckPlayerAttributeTuplesByDeckId = deckPlayerAttributeTuplesByDeckId;
            return this;
        }

        public Builder setDeckPlayerAttributeTuplesByDeckIdExist(Boolean deckPlayerAttributeTuplesByDeckIdExist) {
            this.deckPlayerAttributeTuplesByDeckIdExist = deckPlayerAttributeTuplesByDeckIdExist;
            return this;
        }

        public Builder setDeckSharesByDeckId(DeckToManyDeckShareFilter deckSharesByDeckId) {
            this.deckSharesByDeckId = deckSharesByDeckId;
            return this;
        }

        public Builder setDeckSharesByDeckIdExist(Boolean deckSharesByDeckIdExist) {
            this.deckSharesByDeckIdExist = deckSharesByDeckIdExist;
            return this;
        }

        public Builder setGameUsersByDeckId(DeckToManyGameUserFilter gameUsersByDeckId) {
            this.gameUsersByDeckId = gameUsersByDeckId;
            return this;
        }

        public Builder setGameUsersByDeckIdExist(Boolean gameUsersByDeckIdExist) {
            this.gameUsersByDeckIdExist = gameUsersByDeckIdExist;
            return this;
        }

        public Builder setMatchmakingTicketsByBotDeckId(DeckToManyMatchmakingTicketFilter matchmakingTicketsByBotDeckId) {
            this.matchmakingTicketsByBotDeckId = matchmakingTicketsByBotDeckId;
            return this;
        }

        public Builder setMatchmakingTicketsByBotDeckIdExist(Boolean matchmakingTicketsByBotDeckIdExist) {
            this.matchmakingTicketsByBotDeckIdExist = matchmakingTicketsByBotDeckIdExist;
            return this;
        }

        public Builder setMatchmakingTicketsByDeckId(DeckToManyMatchmakingTicketFilter matchmakingTicketsByDeckId) {
            this.matchmakingTicketsByDeckId = matchmakingTicketsByDeckId;
            return this;
        }

        public Builder setMatchmakingTicketsByDeckIdExist(Boolean matchmakingTicketsByDeckIdExist) {
            this.matchmakingTicketsByDeckIdExist = matchmakingTicketsByDeckIdExist;
            return this;
        }

        public Builder setAnd(java.util.List<DeckFilter> and) {
            this.and = and;
            return this;
        }

        public Builder setOr(java.util.List<DeckFilter> or) {
            this.or = or;
            return this;
        }

        public Builder setNot(DeckFilter not) {
            this.not = not;
            return this;
        }


        public DeckFilter build() {
            return new DeckFilter(id, createdBy, lastEditedBy, name, heroClass, trashed, format, deckType, isPremade, permittedToDuplicate, cardsInDecksByDeckId, cardsInDecksByDeckIdExist, deckPlayerAttributeTuplesByDeckId, deckPlayerAttributeTuplesByDeckIdExist, deckSharesByDeckId, deckSharesByDeckIdExist, gameUsersByDeckId, gameUsersByDeckIdExist, matchmakingTicketsByBotDeckId, matchmakingTicketsByBotDeckIdExist, matchmakingTicketsByDeckId, matchmakingTicketsByDeckIdExist, and, or, not);
        }

    }
}
