package com.hiddenswitch.framework.graphql;


/**
 * The root mutation type which contains root level fields which mutate data.
 */
public class Mutation implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private CardCatalogueGetBannedDraftCardsPayload cardCatalogueGetBannedDraftCards;
    private CardCatalogueGetHardRemovalCardsPayload cardCatalogueGetHardRemovalCards;
    private ArchiveCardPayload archiveCard;
    private PublishCardPayload publishCard;
    private SetUserAttributePayload setUserAttribute;
    private GetUserAttributePayload getUserAttribute;
    private ClusteredGamesUpdateGameAndUsersPayload clusteredGamesUpdateGameAndUsers;
    private GetClassesPayload getClasses;
    private GetCollectionCardsPayload getCollectionCards;
    private SetCardsInDeckPayload setCardsInDeck;
    private CreateDeckWithCardsPayload createDeckWithCards;
    private CardCatalogueFormatsPayload cardCatalogueFormats;
    private CardCatalogueGetClassCardsPayload cardCatalogueGetClassCards;
    private CardCatalogueGetBaseClassesPayload cardCatalogueGetBaseClasses;
    private CardCatalogueGetCardByIdPayload cardCatalogueGetCardById;
    private CardCatalogueGetCardByNamePayload cardCatalogueGetCardByName;
    private CardCatalogueGetFormatPayload cardCatalogueGetFormat;
    private CardCatalogueGetHeroCardPayload cardCatalogueGetHeroCard;
    private CardCatalogueGetCardByNameAndClassPayload cardCatalogueGetCardByNameAndClass;
    private PublishGitCardPayload publishGitCard;
    private SaveCardPayload saveCard;
    private CardCatalogueQueryPayload cardCatalogueQuery;
    private SaveGeneratedArtPayload saveGeneratedArt;
    private CreateBannedDraftCardPayload createBannedDraftCard;
    private CreateBotUserPayload createBotUser;
    private CreateHardRemovalCardPayload createHardRemovalCard;
    private CreatePublishedCardPayload createPublishedCard;
    private CreateGuestPayload createGuest;
    private CreateDeckSharePayload createDeckShare;
    private CreateCardsInDeckPayload createCardsInDeck;
    private CreateDeckPlayerAttributeTuplePayload createDeckPlayerAttributeTuple;
    private CreateFriendPayload createFriend;
    private CreateMatchmakingTicketPayload createMatchmakingTicket;
    private CreateDeckPayload createDeck;
    private CreateMatchmakingQueuePayload createMatchmakingQueue;
    private CreateCardPayload createCard;
    private CreateGeneratedArtPayload createGeneratedArt;
    private CreateGamePayload createGame;
    private CreateGameUserPayload createGameUser;
    private UpdateBannedDraftCardPayload updateBannedDraftCard;
    private UpdateBannedDraftCardPayload updateBannedDraftCardByCardId;
    private UpdateBotUserPayload updateBotUser;
    private UpdateBotUserPayload updateBotUserById;
    private UpdateHardRemovalCardPayload updateHardRemovalCard;
    private UpdateHardRemovalCardPayload updateHardRemovalCardByCardId;
    private UpdatePublishedCardPayload updatePublishedCard;
    private UpdatePublishedCardPayload updatePublishedCardById;
    private UpdateGuestPayload updateGuest;
    private UpdateGuestPayload updateGuestById;
    private UpdateDeckSharePayload updateDeckShare;
    private UpdateDeckSharePayload updateDeckShareByDeckIdAndShareRecipientId;
    private UpdateCardsInDeckPayload updateCardsInDeck;
    private UpdateCardsInDeckPayload updateCardsInDeckById;
    private UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTuple;
    private UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTupleById;
    private UpdateFriendPayload updateFriend;
    private UpdateFriendPayload updateFriendByIdAndFriend;
    private UpdateMatchmakingTicketPayload updateMatchmakingTicket;
    private UpdateMatchmakingTicketPayload updateMatchmakingTicketByUserId;
    private UpdateDeckPayload updateDeck;
    private UpdateDeckPayload updateDeckById;
    private UpdateMatchmakingQueuePayload updateMatchmakingQueue;
    private UpdateMatchmakingQueuePayload updateMatchmakingQueueById;
    private UpdateCardPayload updateCard;
    private UpdateCardPayload updateCardBySuccession;
    private UpdateGeneratedArtPayload updateGeneratedArtByHashAndOwner;
    private UpdateGamePayload updateGame;
    private UpdateGamePayload updateGameById;
    private UpdateGameUserPayload updateGameUser;
    private UpdateGameUserPayload updateGameUserByGameIdAndUserId;
    private DeleteBannedDraftCardPayload deleteBannedDraftCard;
    private DeleteBannedDraftCardPayload deleteBannedDraftCardByCardId;
    private DeleteBotUserPayload deleteBotUser;
    private DeleteBotUserPayload deleteBotUserById;
    private DeleteHardRemovalCardPayload deleteHardRemovalCard;
    private DeleteHardRemovalCardPayload deleteHardRemovalCardByCardId;
    private DeletePublishedCardPayload deletePublishedCard;
    private DeletePublishedCardPayload deletePublishedCardById;
    private DeleteGuestPayload deleteGuest;
    private DeleteGuestPayload deleteGuestById;
    private DeleteDeckSharePayload deleteDeckShare;
    private DeleteDeckSharePayload deleteDeckShareByDeckIdAndShareRecipientId;
    private DeleteCardsInDeckPayload deleteCardsInDeck;
    private DeleteCardsInDeckPayload deleteCardsInDeckById;
    private DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTuple;
    private DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTupleById;
    private DeleteFriendPayload deleteFriend;
    private DeleteFriendPayload deleteFriendByIdAndFriend;
    private DeleteMatchmakingTicketPayload deleteMatchmakingTicket;
    private DeleteMatchmakingTicketPayload deleteMatchmakingTicketByUserId;
    private DeleteDeckPayload deleteDeck;
    private DeleteDeckPayload deleteDeckById;
    private DeleteMatchmakingQueuePayload deleteMatchmakingQueue;
    private DeleteMatchmakingQueuePayload deleteMatchmakingQueueById;
    private DeleteCardPayload deleteCard;
    private DeleteCardPayload deleteCardBySuccession;
    private DeleteGeneratedArtPayload deleteGeneratedArtByHashAndOwner;
    private DeleteGamePayload deleteGame;
    private DeleteGamePayload deleteGameById;
    private DeleteGameUserPayload deleteGameUser;
    private DeleteGameUserPayload deleteGameUserByGameIdAndUserId;

    public Mutation() {
    }

    public Mutation(CardCatalogueGetBannedDraftCardsPayload cardCatalogueGetBannedDraftCards, CardCatalogueGetHardRemovalCardsPayload cardCatalogueGetHardRemovalCards, ArchiveCardPayload archiveCard, PublishCardPayload publishCard, SetUserAttributePayload setUserAttribute, GetUserAttributePayload getUserAttribute, ClusteredGamesUpdateGameAndUsersPayload clusteredGamesUpdateGameAndUsers, GetClassesPayload getClasses, GetCollectionCardsPayload getCollectionCards, SetCardsInDeckPayload setCardsInDeck, CreateDeckWithCardsPayload createDeckWithCards, CardCatalogueFormatsPayload cardCatalogueFormats, CardCatalogueGetClassCardsPayload cardCatalogueGetClassCards, CardCatalogueGetBaseClassesPayload cardCatalogueGetBaseClasses, CardCatalogueGetCardByIdPayload cardCatalogueGetCardById, CardCatalogueGetCardByNamePayload cardCatalogueGetCardByName, CardCatalogueGetFormatPayload cardCatalogueGetFormat, CardCatalogueGetHeroCardPayload cardCatalogueGetHeroCard, CardCatalogueGetCardByNameAndClassPayload cardCatalogueGetCardByNameAndClass, PublishGitCardPayload publishGitCard, SaveCardPayload saveCard, CardCatalogueQueryPayload cardCatalogueQuery, SaveGeneratedArtPayload saveGeneratedArt, CreateBannedDraftCardPayload createBannedDraftCard, CreateBotUserPayload createBotUser, CreateHardRemovalCardPayload createHardRemovalCard, CreatePublishedCardPayload createPublishedCard, CreateGuestPayload createGuest, CreateDeckSharePayload createDeckShare, CreateCardsInDeckPayload createCardsInDeck, CreateDeckPlayerAttributeTuplePayload createDeckPlayerAttributeTuple, CreateFriendPayload createFriend, CreateMatchmakingTicketPayload createMatchmakingTicket, CreateDeckPayload createDeck, CreateMatchmakingQueuePayload createMatchmakingQueue, CreateCardPayload createCard, CreateGeneratedArtPayload createGeneratedArt, CreateGamePayload createGame, CreateGameUserPayload createGameUser, UpdateBannedDraftCardPayload updateBannedDraftCard, UpdateBannedDraftCardPayload updateBannedDraftCardByCardId, UpdateBotUserPayload updateBotUser, UpdateBotUserPayload updateBotUserById, UpdateHardRemovalCardPayload updateHardRemovalCard, UpdateHardRemovalCardPayload updateHardRemovalCardByCardId, UpdatePublishedCardPayload updatePublishedCard, UpdatePublishedCardPayload updatePublishedCardById, UpdateGuestPayload updateGuest, UpdateGuestPayload updateGuestById, UpdateDeckSharePayload updateDeckShare, UpdateDeckSharePayload updateDeckShareByDeckIdAndShareRecipientId, UpdateCardsInDeckPayload updateCardsInDeck, UpdateCardsInDeckPayload updateCardsInDeckById, UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTuple, UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTupleById, UpdateFriendPayload updateFriend, UpdateFriendPayload updateFriendByIdAndFriend, UpdateMatchmakingTicketPayload updateMatchmakingTicket, UpdateMatchmakingTicketPayload updateMatchmakingTicketByUserId, UpdateDeckPayload updateDeck, UpdateDeckPayload updateDeckById, UpdateMatchmakingQueuePayload updateMatchmakingQueue, UpdateMatchmakingQueuePayload updateMatchmakingQueueById, UpdateCardPayload updateCard, UpdateCardPayload updateCardBySuccession, UpdateGeneratedArtPayload updateGeneratedArtByHashAndOwner, UpdateGamePayload updateGame, UpdateGamePayload updateGameById, UpdateGameUserPayload updateGameUser, UpdateGameUserPayload updateGameUserByGameIdAndUserId, DeleteBannedDraftCardPayload deleteBannedDraftCard, DeleteBannedDraftCardPayload deleteBannedDraftCardByCardId, DeleteBotUserPayload deleteBotUser, DeleteBotUserPayload deleteBotUserById, DeleteHardRemovalCardPayload deleteHardRemovalCard, DeleteHardRemovalCardPayload deleteHardRemovalCardByCardId, DeletePublishedCardPayload deletePublishedCard, DeletePublishedCardPayload deletePublishedCardById, DeleteGuestPayload deleteGuest, DeleteGuestPayload deleteGuestById, DeleteDeckSharePayload deleteDeckShare, DeleteDeckSharePayload deleteDeckShareByDeckIdAndShareRecipientId, DeleteCardsInDeckPayload deleteCardsInDeck, DeleteCardsInDeckPayload deleteCardsInDeckById, DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTuple, DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTupleById, DeleteFriendPayload deleteFriend, DeleteFriendPayload deleteFriendByIdAndFriend, DeleteMatchmakingTicketPayload deleteMatchmakingTicket, DeleteMatchmakingTicketPayload deleteMatchmakingTicketByUserId, DeleteDeckPayload deleteDeck, DeleteDeckPayload deleteDeckById, DeleteMatchmakingQueuePayload deleteMatchmakingQueue, DeleteMatchmakingQueuePayload deleteMatchmakingQueueById, DeleteCardPayload deleteCard, DeleteCardPayload deleteCardBySuccession, DeleteGeneratedArtPayload deleteGeneratedArtByHashAndOwner, DeleteGamePayload deleteGame, DeleteGamePayload deleteGameById, DeleteGameUserPayload deleteGameUser, DeleteGameUserPayload deleteGameUserByGameIdAndUserId) {
        this.cardCatalogueGetBannedDraftCards = cardCatalogueGetBannedDraftCards;
        this.cardCatalogueGetHardRemovalCards = cardCatalogueGetHardRemovalCards;
        this.archiveCard = archiveCard;
        this.publishCard = publishCard;
        this.setUserAttribute = setUserAttribute;
        this.getUserAttribute = getUserAttribute;
        this.clusteredGamesUpdateGameAndUsers = clusteredGamesUpdateGameAndUsers;
        this.getClasses = getClasses;
        this.getCollectionCards = getCollectionCards;
        this.setCardsInDeck = setCardsInDeck;
        this.createDeckWithCards = createDeckWithCards;
        this.cardCatalogueFormats = cardCatalogueFormats;
        this.cardCatalogueGetClassCards = cardCatalogueGetClassCards;
        this.cardCatalogueGetBaseClasses = cardCatalogueGetBaseClasses;
        this.cardCatalogueGetCardById = cardCatalogueGetCardById;
        this.cardCatalogueGetCardByName = cardCatalogueGetCardByName;
        this.cardCatalogueGetFormat = cardCatalogueGetFormat;
        this.cardCatalogueGetHeroCard = cardCatalogueGetHeroCard;
        this.cardCatalogueGetCardByNameAndClass = cardCatalogueGetCardByNameAndClass;
        this.publishGitCard = publishGitCard;
        this.saveCard = saveCard;
        this.cardCatalogueQuery = cardCatalogueQuery;
        this.saveGeneratedArt = saveGeneratedArt;
        this.createBannedDraftCard = createBannedDraftCard;
        this.createBotUser = createBotUser;
        this.createHardRemovalCard = createHardRemovalCard;
        this.createPublishedCard = createPublishedCard;
        this.createGuest = createGuest;
        this.createDeckShare = createDeckShare;
        this.createCardsInDeck = createCardsInDeck;
        this.createDeckPlayerAttributeTuple = createDeckPlayerAttributeTuple;
        this.createFriend = createFriend;
        this.createMatchmakingTicket = createMatchmakingTicket;
        this.createDeck = createDeck;
        this.createMatchmakingQueue = createMatchmakingQueue;
        this.createCard = createCard;
        this.createGeneratedArt = createGeneratedArt;
        this.createGame = createGame;
        this.createGameUser = createGameUser;
        this.updateBannedDraftCard = updateBannedDraftCard;
        this.updateBannedDraftCardByCardId = updateBannedDraftCardByCardId;
        this.updateBotUser = updateBotUser;
        this.updateBotUserById = updateBotUserById;
        this.updateHardRemovalCard = updateHardRemovalCard;
        this.updateHardRemovalCardByCardId = updateHardRemovalCardByCardId;
        this.updatePublishedCard = updatePublishedCard;
        this.updatePublishedCardById = updatePublishedCardById;
        this.updateGuest = updateGuest;
        this.updateGuestById = updateGuestById;
        this.updateDeckShare = updateDeckShare;
        this.updateDeckShareByDeckIdAndShareRecipientId = updateDeckShareByDeckIdAndShareRecipientId;
        this.updateCardsInDeck = updateCardsInDeck;
        this.updateCardsInDeckById = updateCardsInDeckById;
        this.updateDeckPlayerAttributeTuple = updateDeckPlayerAttributeTuple;
        this.updateDeckPlayerAttributeTupleById = updateDeckPlayerAttributeTupleById;
        this.updateFriend = updateFriend;
        this.updateFriendByIdAndFriend = updateFriendByIdAndFriend;
        this.updateMatchmakingTicket = updateMatchmakingTicket;
        this.updateMatchmakingTicketByUserId = updateMatchmakingTicketByUserId;
        this.updateDeck = updateDeck;
        this.updateDeckById = updateDeckById;
        this.updateMatchmakingQueue = updateMatchmakingQueue;
        this.updateMatchmakingQueueById = updateMatchmakingQueueById;
        this.updateCard = updateCard;
        this.updateCardBySuccession = updateCardBySuccession;
        this.updateGeneratedArtByHashAndOwner = updateGeneratedArtByHashAndOwner;
        this.updateGame = updateGame;
        this.updateGameById = updateGameById;
        this.updateGameUser = updateGameUser;
        this.updateGameUserByGameIdAndUserId = updateGameUserByGameIdAndUserId;
        this.deleteBannedDraftCard = deleteBannedDraftCard;
        this.deleteBannedDraftCardByCardId = deleteBannedDraftCardByCardId;
        this.deleteBotUser = deleteBotUser;
        this.deleteBotUserById = deleteBotUserById;
        this.deleteHardRemovalCard = deleteHardRemovalCard;
        this.deleteHardRemovalCardByCardId = deleteHardRemovalCardByCardId;
        this.deletePublishedCard = deletePublishedCard;
        this.deletePublishedCardById = deletePublishedCardById;
        this.deleteGuest = deleteGuest;
        this.deleteGuestById = deleteGuestById;
        this.deleteDeckShare = deleteDeckShare;
        this.deleteDeckShareByDeckIdAndShareRecipientId = deleteDeckShareByDeckIdAndShareRecipientId;
        this.deleteCardsInDeck = deleteCardsInDeck;
        this.deleteCardsInDeckById = deleteCardsInDeckById;
        this.deleteDeckPlayerAttributeTuple = deleteDeckPlayerAttributeTuple;
        this.deleteDeckPlayerAttributeTupleById = deleteDeckPlayerAttributeTupleById;
        this.deleteFriend = deleteFriend;
        this.deleteFriendByIdAndFriend = deleteFriendByIdAndFriend;
        this.deleteMatchmakingTicket = deleteMatchmakingTicket;
        this.deleteMatchmakingTicketByUserId = deleteMatchmakingTicketByUserId;
        this.deleteDeck = deleteDeck;
        this.deleteDeckById = deleteDeckById;
        this.deleteMatchmakingQueue = deleteMatchmakingQueue;
        this.deleteMatchmakingQueueById = deleteMatchmakingQueueById;
        this.deleteCard = deleteCard;
        this.deleteCardBySuccession = deleteCardBySuccession;
        this.deleteGeneratedArtByHashAndOwner = deleteGeneratedArtByHashAndOwner;
        this.deleteGame = deleteGame;
        this.deleteGameById = deleteGameById;
        this.deleteGameUser = deleteGameUser;
        this.deleteGameUserByGameIdAndUserId = deleteGameUserByGameIdAndUserId;
    }

    public CardCatalogueGetBannedDraftCardsPayload getCardCatalogueGetBannedDraftCards() {
        return cardCatalogueGetBannedDraftCards;
    }
    public void setCardCatalogueGetBannedDraftCards(CardCatalogueGetBannedDraftCardsPayload cardCatalogueGetBannedDraftCards) {
        this.cardCatalogueGetBannedDraftCards = cardCatalogueGetBannedDraftCards;
    }

    public CardCatalogueGetHardRemovalCardsPayload getCardCatalogueGetHardRemovalCards() {
        return cardCatalogueGetHardRemovalCards;
    }
    public void setCardCatalogueGetHardRemovalCards(CardCatalogueGetHardRemovalCardsPayload cardCatalogueGetHardRemovalCards) {
        this.cardCatalogueGetHardRemovalCards = cardCatalogueGetHardRemovalCards;
    }

    public ArchiveCardPayload getArchiveCard() {
        return archiveCard;
    }
    public void setArchiveCard(ArchiveCardPayload archiveCard) {
        this.archiveCard = archiveCard;
    }

    public PublishCardPayload getPublishCard() {
        return publishCard;
    }
    public void setPublishCard(PublishCardPayload publishCard) {
        this.publishCard = publishCard;
    }

    public SetUserAttributePayload getSetUserAttribute() {
        return setUserAttribute;
    }
    public void setSetUserAttribute(SetUserAttributePayload setUserAttribute) {
        this.setUserAttribute = setUserAttribute;
    }

    public GetUserAttributePayload getGetUserAttribute() {
        return getUserAttribute;
    }
    public void setGetUserAttribute(GetUserAttributePayload getUserAttribute) {
        this.getUserAttribute = getUserAttribute;
    }

    public ClusteredGamesUpdateGameAndUsersPayload getClusteredGamesUpdateGameAndUsers() {
        return clusteredGamesUpdateGameAndUsers;
    }
    public void setClusteredGamesUpdateGameAndUsers(ClusteredGamesUpdateGameAndUsersPayload clusteredGamesUpdateGameAndUsers) {
        this.clusteredGamesUpdateGameAndUsers = clusteredGamesUpdateGameAndUsers;
    }

    public GetClassesPayload getGetClasses() {
        return getClasses;
    }
    public void setGetClasses(GetClassesPayload getClasses) {
        this.getClasses = getClasses;
    }

    public GetCollectionCardsPayload getGetCollectionCards() {
        return getCollectionCards;
    }
    public void setGetCollectionCards(GetCollectionCardsPayload getCollectionCards) {
        this.getCollectionCards = getCollectionCards;
    }

    public SetCardsInDeckPayload getSetCardsInDeck() {
        return setCardsInDeck;
    }
    public void setSetCardsInDeck(SetCardsInDeckPayload setCardsInDeck) {
        this.setCardsInDeck = setCardsInDeck;
    }

    public CreateDeckWithCardsPayload getCreateDeckWithCards() {
        return createDeckWithCards;
    }
    public void setCreateDeckWithCards(CreateDeckWithCardsPayload createDeckWithCards) {
        this.createDeckWithCards = createDeckWithCards;
    }

    public CardCatalogueFormatsPayload getCardCatalogueFormats() {
        return cardCatalogueFormats;
    }
    public void setCardCatalogueFormats(CardCatalogueFormatsPayload cardCatalogueFormats) {
        this.cardCatalogueFormats = cardCatalogueFormats;
    }

    public CardCatalogueGetClassCardsPayload getCardCatalogueGetClassCards() {
        return cardCatalogueGetClassCards;
    }
    public void setCardCatalogueGetClassCards(CardCatalogueGetClassCardsPayload cardCatalogueGetClassCards) {
        this.cardCatalogueGetClassCards = cardCatalogueGetClassCards;
    }

    public CardCatalogueGetBaseClassesPayload getCardCatalogueGetBaseClasses() {
        return cardCatalogueGetBaseClasses;
    }
    public void setCardCatalogueGetBaseClasses(CardCatalogueGetBaseClassesPayload cardCatalogueGetBaseClasses) {
        this.cardCatalogueGetBaseClasses = cardCatalogueGetBaseClasses;
    }

    public CardCatalogueGetCardByIdPayload getCardCatalogueGetCardById() {
        return cardCatalogueGetCardById;
    }
    public void setCardCatalogueGetCardById(CardCatalogueGetCardByIdPayload cardCatalogueGetCardById) {
        this.cardCatalogueGetCardById = cardCatalogueGetCardById;
    }

    public CardCatalogueGetCardByNamePayload getCardCatalogueGetCardByName() {
        return cardCatalogueGetCardByName;
    }
    public void setCardCatalogueGetCardByName(CardCatalogueGetCardByNamePayload cardCatalogueGetCardByName) {
        this.cardCatalogueGetCardByName = cardCatalogueGetCardByName;
    }

    public CardCatalogueGetFormatPayload getCardCatalogueGetFormat() {
        return cardCatalogueGetFormat;
    }
    public void setCardCatalogueGetFormat(CardCatalogueGetFormatPayload cardCatalogueGetFormat) {
        this.cardCatalogueGetFormat = cardCatalogueGetFormat;
    }

    public CardCatalogueGetHeroCardPayload getCardCatalogueGetHeroCard() {
        return cardCatalogueGetHeroCard;
    }
    public void setCardCatalogueGetHeroCard(CardCatalogueGetHeroCardPayload cardCatalogueGetHeroCard) {
        this.cardCatalogueGetHeroCard = cardCatalogueGetHeroCard;
    }

    public CardCatalogueGetCardByNameAndClassPayload getCardCatalogueGetCardByNameAndClass() {
        return cardCatalogueGetCardByNameAndClass;
    }
    public void setCardCatalogueGetCardByNameAndClass(CardCatalogueGetCardByNameAndClassPayload cardCatalogueGetCardByNameAndClass) {
        this.cardCatalogueGetCardByNameAndClass = cardCatalogueGetCardByNameAndClass;
    }

    public PublishGitCardPayload getPublishGitCard() {
        return publishGitCard;
    }
    public void setPublishGitCard(PublishGitCardPayload publishGitCard) {
        this.publishGitCard = publishGitCard;
    }

    public SaveCardPayload getSaveCard() {
        return saveCard;
    }
    public void setSaveCard(SaveCardPayload saveCard) {
        this.saveCard = saveCard;
    }

    public CardCatalogueQueryPayload getCardCatalogueQuery() {
        return cardCatalogueQuery;
    }
    public void setCardCatalogueQuery(CardCatalogueQueryPayload cardCatalogueQuery) {
        this.cardCatalogueQuery = cardCatalogueQuery;
    }

    public SaveGeneratedArtPayload getSaveGeneratedArt() {
        return saveGeneratedArt;
    }
    public void setSaveGeneratedArt(SaveGeneratedArtPayload saveGeneratedArt) {
        this.saveGeneratedArt = saveGeneratedArt;
    }

    /**
     * Creates a single `BannedDraftCard`.
     */
    public CreateBannedDraftCardPayload getCreateBannedDraftCard() {
        return createBannedDraftCard;
    }
    /**
     * Creates a single `BannedDraftCard`.
     */
    public void setCreateBannedDraftCard(CreateBannedDraftCardPayload createBannedDraftCard) {
        this.createBannedDraftCard = createBannedDraftCard;
    }

    /**
     * Creates a single `BotUser`.
     */
    public CreateBotUserPayload getCreateBotUser() {
        return createBotUser;
    }
    /**
     * Creates a single `BotUser`.
     */
    public void setCreateBotUser(CreateBotUserPayload createBotUser) {
        this.createBotUser = createBotUser;
    }

    /**
     * Creates a single `HardRemovalCard`.
     */
    public CreateHardRemovalCardPayload getCreateHardRemovalCard() {
        return createHardRemovalCard;
    }
    /**
     * Creates a single `HardRemovalCard`.
     */
    public void setCreateHardRemovalCard(CreateHardRemovalCardPayload createHardRemovalCard) {
        this.createHardRemovalCard = createHardRemovalCard;
    }

    /**
     * Creates a single `PublishedCard`.
     */
    public CreatePublishedCardPayload getCreatePublishedCard() {
        return createPublishedCard;
    }
    /**
     * Creates a single `PublishedCard`.
     */
    public void setCreatePublishedCard(CreatePublishedCardPayload createPublishedCard) {
        this.createPublishedCard = createPublishedCard;
    }

    /**
     * Creates a single `Guest`.
     */
    public CreateGuestPayload getCreateGuest() {
        return createGuest;
    }
    /**
     * Creates a single `Guest`.
     */
    public void setCreateGuest(CreateGuestPayload createGuest) {
        this.createGuest = createGuest;
    }

    /**
     * Creates a single `DeckShare`.
     */
    public CreateDeckSharePayload getCreateDeckShare() {
        return createDeckShare;
    }
    /**
     * Creates a single `DeckShare`.
     */
    public void setCreateDeckShare(CreateDeckSharePayload createDeckShare) {
        this.createDeckShare = createDeckShare;
    }

    /**
     * Creates a single `CardsInDeck`.
     */
    public CreateCardsInDeckPayload getCreateCardsInDeck() {
        return createCardsInDeck;
    }
    /**
     * Creates a single `CardsInDeck`.
     */
    public void setCreateCardsInDeck(CreateCardsInDeckPayload createCardsInDeck) {
        this.createCardsInDeck = createCardsInDeck;
    }

    /**
     * Creates a single `DeckPlayerAttributeTuple`.
     */
    public CreateDeckPlayerAttributeTuplePayload getCreateDeckPlayerAttributeTuple() {
        return createDeckPlayerAttributeTuple;
    }
    /**
     * Creates a single `DeckPlayerAttributeTuple`.
     */
    public void setCreateDeckPlayerAttributeTuple(CreateDeckPlayerAttributeTuplePayload createDeckPlayerAttributeTuple) {
        this.createDeckPlayerAttributeTuple = createDeckPlayerAttributeTuple;
    }

    /**
     * Creates a single `Friend`.
     */
    public CreateFriendPayload getCreateFriend() {
        return createFriend;
    }
    /**
     * Creates a single `Friend`.
     */
    public void setCreateFriend(CreateFriendPayload createFriend) {
        this.createFriend = createFriend;
    }

    /**
     * Creates a single `MatchmakingTicket`.
     */
    public CreateMatchmakingTicketPayload getCreateMatchmakingTicket() {
        return createMatchmakingTicket;
    }
    /**
     * Creates a single `MatchmakingTicket`.
     */
    public void setCreateMatchmakingTicket(CreateMatchmakingTicketPayload createMatchmakingTicket) {
        this.createMatchmakingTicket = createMatchmakingTicket;
    }

    /**
     * Creates a single `Deck`.
     */
    public CreateDeckPayload getCreateDeck() {
        return createDeck;
    }
    /**
     * Creates a single `Deck`.
     */
    public void setCreateDeck(CreateDeckPayload createDeck) {
        this.createDeck = createDeck;
    }

    /**
     * Creates a single `MatchmakingQueue`.
     */
    public CreateMatchmakingQueuePayload getCreateMatchmakingQueue() {
        return createMatchmakingQueue;
    }
    /**
     * Creates a single `MatchmakingQueue`.
     */
    public void setCreateMatchmakingQueue(CreateMatchmakingQueuePayload createMatchmakingQueue) {
        this.createMatchmakingQueue = createMatchmakingQueue;
    }

    /**
     * Creates a single `Card`.
     */
    public CreateCardPayload getCreateCard() {
        return createCard;
    }
    /**
     * Creates a single `Card`.
     */
    public void setCreateCard(CreateCardPayload createCard) {
        this.createCard = createCard;
    }

    /**
     * Creates a single `GeneratedArt`.
     */
    public CreateGeneratedArtPayload getCreateGeneratedArt() {
        return createGeneratedArt;
    }
    /**
     * Creates a single `GeneratedArt`.
     */
    public void setCreateGeneratedArt(CreateGeneratedArtPayload createGeneratedArt) {
        this.createGeneratedArt = createGeneratedArt;
    }

    /**
     * Creates a single `Game`.
     */
    public CreateGamePayload getCreateGame() {
        return createGame;
    }
    /**
     * Creates a single `Game`.
     */
    public void setCreateGame(CreateGamePayload createGame) {
        this.createGame = createGame;
    }

    /**
     * Creates a single `GameUser`.
     */
    public CreateGameUserPayload getCreateGameUser() {
        return createGameUser;
    }
    /**
     * Creates a single `GameUser`.
     */
    public void setCreateGameUser(CreateGameUserPayload createGameUser) {
        this.createGameUser = createGameUser;
    }

    /**
     * Updates a single `BannedDraftCard` using its globally unique id and a patch.
     */
    public UpdateBannedDraftCardPayload getUpdateBannedDraftCard() {
        return updateBannedDraftCard;
    }
    /**
     * Updates a single `BannedDraftCard` using its globally unique id and a patch.
     */
    public void setUpdateBannedDraftCard(UpdateBannedDraftCardPayload updateBannedDraftCard) {
        this.updateBannedDraftCard = updateBannedDraftCard;
    }

    /**
     * Updates a single `BannedDraftCard` using a unique key and a patch.
     */
    public UpdateBannedDraftCardPayload getUpdateBannedDraftCardByCardId() {
        return updateBannedDraftCardByCardId;
    }
    /**
     * Updates a single `BannedDraftCard` using a unique key and a patch.
     */
    public void setUpdateBannedDraftCardByCardId(UpdateBannedDraftCardPayload updateBannedDraftCardByCardId) {
        this.updateBannedDraftCardByCardId = updateBannedDraftCardByCardId;
    }

    /**
     * Updates a single `BotUser` using its globally unique id and a patch.
     */
    public UpdateBotUserPayload getUpdateBotUser() {
        return updateBotUser;
    }
    /**
     * Updates a single `BotUser` using its globally unique id and a patch.
     */
    public void setUpdateBotUser(UpdateBotUserPayload updateBotUser) {
        this.updateBotUser = updateBotUser;
    }

    /**
     * Updates a single `BotUser` using a unique key and a patch.
     */
    public UpdateBotUserPayload getUpdateBotUserById() {
        return updateBotUserById;
    }
    /**
     * Updates a single `BotUser` using a unique key and a patch.
     */
    public void setUpdateBotUserById(UpdateBotUserPayload updateBotUserById) {
        this.updateBotUserById = updateBotUserById;
    }

    /**
     * Updates a single `HardRemovalCard` using its globally unique id and a patch.
     */
    public UpdateHardRemovalCardPayload getUpdateHardRemovalCard() {
        return updateHardRemovalCard;
    }
    /**
     * Updates a single `HardRemovalCard` using its globally unique id and a patch.
     */
    public void setUpdateHardRemovalCard(UpdateHardRemovalCardPayload updateHardRemovalCard) {
        this.updateHardRemovalCard = updateHardRemovalCard;
    }

    /**
     * Updates a single `HardRemovalCard` using a unique key and a patch.
     */
    public UpdateHardRemovalCardPayload getUpdateHardRemovalCardByCardId() {
        return updateHardRemovalCardByCardId;
    }
    /**
     * Updates a single `HardRemovalCard` using a unique key and a patch.
     */
    public void setUpdateHardRemovalCardByCardId(UpdateHardRemovalCardPayload updateHardRemovalCardByCardId) {
        this.updateHardRemovalCardByCardId = updateHardRemovalCardByCardId;
    }

    /**
     * Updates a single `PublishedCard` using its globally unique id and a patch.
     */
    public UpdatePublishedCardPayload getUpdatePublishedCard() {
        return updatePublishedCard;
    }
    /**
     * Updates a single `PublishedCard` using its globally unique id and a patch.
     */
    public void setUpdatePublishedCard(UpdatePublishedCardPayload updatePublishedCard) {
        this.updatePublishedCard = updatePublishedCard;
    }

    /**
     * Updates a single `PublishedCard` using a unique key and a patch.
     */
    public UpdatePublishedCardPayload getUpdatePublishedCardById() {
        return updatePublishedCardById;
    }
    /**
     * Updates a single `PublishedCard` using a unique key and a patch.
     */
    public void setUpdatePublishedCardById(UpdatePublishedCardPayload updatePublishedCardById) {
        this.updatePublishedCardById = updatePublishedCardById;
    }

    /**
     * Updates a single `Guest` using its globally unique id and a patch.
     */
    public UpdateGuestPayload getUpdateGuest() {
        return updateGuest;
    }
    /**
     * Updates a single `Guest` using its globally unique id and a patch.
     */
    public void setUpdateGuest(UpdateGuestPayload updateGuest) {
        this.updateGuest = updateGuest;
    }

    /**
     * Updates a single `Guest` using a unique key and a patch.
     */
    public UpdateGuestPayload getUpdateGuestById() {
        return updateGuestById;
    }
    /**
     * Updates a single `Guest` using a unique key and a patch.
     */
    public void setUpdateGuestById(UpdateGuestPayload updateGuestById) {
        this.updateGuestById = updateGuestById;
    }

    /**
     * Updates a single `DeckShare` using its globally unique id and a patch.
     */
    public UpdateDeckSharePayload getUpdateDeckShare() {
        return updateDeckShare;
    }
    /**
     * Updates a single `DeckShare` using its globally unique id and a patch.
     */
    public void setUpdateDeckShare(UpdateDeckSharePayload updateDeckShare) {
        this.updateDeckShare = updateDeckShare;
    }

    /**
     * Updates a single `DeckShare` using a unique key and a patch.
     */
    public UpdateDeckSharePayload getUpdateDeckShareByDeckIdAndShareRecipientId() {
        return updateDeckShareByDeckIdAndShareRecipientId;
    }
    /**
     * Updates a single `DeckShare` using a unique key and a patch.
     */
    public void setUpdateDeckShareByDeckIdAndShareRecipientId(UpdateDeckSharePayload updateDeckShareByDeckIdAndShareRecipientId) {
        this.updateDeckShareByDeckIdAndShareRecipientId = updateDeckShareByDeckIdAndShareRecipientId;
    }

    /**
     * Updates a single `CardsInDeck` using its globally unique id and a patch.
     */
    public UpdateCardsInDeckPayload getUpdateCardsInDeck() {
        return updateCardsInDeck;
    }
    /**
     * Updates a single `CardsInDeck` using its globally unique id and a patch.
     */
    public void setUpdateCardsInDeck(UpdateCardsInDeckPayload updateCardsInDeck) {
        this.updateCardsInDeck = updateCardsInDeck;
    }

    /**
     * Updates a single `CardsInDeck` using a unique key and a patch.
     */
    public UpdateCardsInDeckPayload getUpdateCardsInDeckById() {
        return updateCardsInDeckById;
    }
    /**
     * Updates a single `CardsInDeck` using a unique key and a patch.
     */
    public void setUpdateCardsInDeckById(UpdateCardsInDeckPayload updateCardsInDeckById) {
        this.updateCardsInDeckById = updateCardsInDeckById;
    }

    /**
     * Updates a single `DeckPlayerAttributeTuple` using its globally unique id and a patch.
     */
    public UpdateDeckPlayerAttributeTuplePayload getUpdateDeckPlayerAttributeTuple() {
        return updateDeckPlayerAttributeTuple;
    }
    /**
     * Updates a single `DeckPlayerAttributeTuple` using its globally unique id and a patch.
     */
    public void setUpdateDeckPlayerAttributeTuple(UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTuple) {
        this.updateDeckPlayerAttributeTuple = updateDeckPlayerAttributeTuple;
    }

    /**
     * Updates a single `DeckPlayerAttributeTuple` using a unique key and a patch.
     */
    public UpdateDeckPlayerAttributeTuplePayload getUpdateDeckPlayerAttributeTupleById() {
        return updateDeckPlayerAttributeTupleById;
    }
    /**
     * Updates a single `DeckPlayerAttributeTuple` using a unique key and a patch.
     */
    public void setUpdateDeckPlayerAttributeTupleById(UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTupleById) {
        this.updateDeckPlayerAttributeTupleById = updateDeckPlayerAttributeTupleById;
    }

    /**
     * Updates a single `Friend` using its globally unique id and a patch.
     */
    public UpdateFriendPayload getUpdateFriend() {
        return updateFriend;
    }
    /**
     * Updates a single `Friend` using its globally unique id and a patch.
     */
    public void setUpdateFriend(UpdateFriendPayload updateFriend) {
        this.updateFriend = updateFriend;
    }

    /**
     * Updates a single `Friend` using a unique key and a patch.
     */
    public UpdateFriendPayload getUpdateFriendByIdAndFriend() {
        return updateFriendByIdAndFriend;
    }
    /**
     * Updates a single `Friend` using a unique key and a patch.
     */
    public void setUpdateFriendByIdAndFriend(UpdateFriendPayload updateFriendByIdAndFriend) {
        this.updateFriendByIdAndFriend = updateFriendByIdAndFriend;
    }

    /**
     * Updates a single `MatchmakingTicket` using its globally unique id and a patch.
     */
    public UpdateMatchmakingTicketPayload getUpdateMatchmakingTicket() {
        return updateMatchmakingTicket;
    }
    /**
     * Updates a single `MatchmakingTicket` using its globally unique id and a patch.
     */
    public void setUpdateMatchmakingTicket(UpdateMatchmakingTicketPayload updateMatchmakingTicket) {
        this.updateMatchmakingTicket = updateMatchmakingTicket;
    }

    /**
     * Updates a single `MatchmakingTicket` using a unique key and a patch.
     */
    public UpdateMatchmakingTicketPayload getUpdateMatchmakingTicketByUserId() {
        return updateMatchmakingTicketByUserId;
    }
    /**
     * Updates a single `MatchmakingTicket` using a unique key and a patch.
     */
    public void setUpdateMatchmakingTicketByUserId(UpdateMatchmakingTicketPayload updateMatchmakingTicketByUserId) {
        this.updateMatchmakingTicketByUserId = updateMatchmakingTicketByUserId;
    }

    /**
     * Updates a single `Deck` using its globally unique id and a patch.
     */
    public UpdateDeckPayload getUpdateDeck() {
        return updateDeck;
    }
    /**
     * Updates a single `Deck` using its globally unique id and a patch.
     */
    public void setUpdateDeck(UpdateDeckPayload updateDeck) {
        this.updateDeck = updateDeck;
    }

    /**
     * Updates a single `Deck` using a unique key and a patch.
     */
    public UpdateDeckPayload getUpdateDeckById() {
        return updateDeckById;
    }
    /**
     * Updates a single `Deck` using a unique key and a patch.
     */
    public void setUpdateDeckById(UpdateDeckPayload updateDeckById) {
        this.updateDeckById = updateDeckById;
    }

    /**
     * Updates a single `MatchmakingQueue` using its globally unique id and a patch.
     */
    public UpdateMatchmakingQueuePayload getUpdateMatchmakingQueue() {
        return updateMatchmakingQueue;
    }
    /**
     * Updates a single `MatchmakingQueue` using its globally unique id and a patch.
     */
    public void setUpdateMatchmakingQueue(UpdateMatchmakingQueuePayload updateMatchmakingQueue) {
        this.updateMatchmakingQueue = updateMatchmakingQueue;
    }

    /**
     * Updates a single `MatchmakingQueue` using a unique key and a patch.
     */
    public UpdateMatchmakingQueuePayload getUpdateMatchmakingQueueById() {
        return updateMatchmakingQueueById;
    }
    /**
     * Updates a single `MatchmakingQueue` using a unique key and a patch.
     */
    public void setUpdateMatchmakingQueueById(UpdateMatchmakingQueuePayload updateMatchmakingQueueById) {
        this.updateMatchmakingQueueById = updateMatchmakingQueueById;
    }

    /**
     * Updates a single `Card` using its globally unique id and a patch.
     */
    public UpdateCardPayload getUpdateCard() {
        return updateCard;
    }
    /**
     * Updates a single `Card` using its globally unique id and a patch.
     */
    public void setUpdateCard(UpdateCardPayload updateCard) {
        this.updateCard = updateCard;
    }

    /**
     * Updates a single `Card` using a unique key and a patch.
     */
    public UpdateCardPayload getUpdateCardBySuccession() {
        return updateCardBySuccession;
    }
    /**
     * Updates a single `Card` using a unique key and a patch.
     */
    public void setUpdateCardBySuccession(UpdateCardPayload updateCardBySuccession) {
        this.updateCardBySuccession = updateCardBySuccession;
    }

    /**
     * Updates a single `GeneratedArt` using a unique key and a patch.
     */
    public UpdateGeneratedArtPayload getUpdateGeneratedArtByHashAndOwner() {
        return updateGeneratedArtByHashAndOwner;
    }
    /**
     * Updates a single `GeneratedArt` using a unique key and a patch.
     */
    public void setUpdateGeneratedArtByHashAndOwner(UpdateGeneratedArtPayload updateGeneratedArtByHashAndOwner) {
        this.updateGeneratedArtByHashAndOwner = updateGeneratedArtByHashAndOwner;
    }

    /**
     * Updates a single `Game` using its globally unique id and a patch.
     */
    public UpdateGamePayload getUpdateGame() {
        return updateGame;
    }
    /**
     * Updates a single `Game` using its globally unique id and a patch.
     */
    public void setUpdateGame(UpdateGamePayload updateGame) {
        this.updateGame = updateGame;
    }

    /**
     * Updates a single `Game` using a unique key and a patch.
     */
    public UpdateGamePayload getUpdateGameById() {
        return updateGameById;
    }
    /**
     * Updates a single `Game` using a unique key and a patch.
     */
    public void setUpdateGameById(UpdateGamePayload updateGameById) {
        this.updateGameById = updateGameById;
    }

    /**
     * Updates a single `GameUser` using its globally unique id and a patch.
     */
    public UpdateGameUserPayload getUpdateGameUser() {
        return updateGameUser;
    }
    /**
     * Updates a single `GameUser` using its globally unique id and a patch.
     */
    public void setUpdateGameUser(UpdateGameUserPayload updateGameUser) {
        this.updateGameUser = updateGameUser;
    }

    /**
     * Updates a single `GameUser` using a unique key and a patch.
     */
    public UpdateGameUserPayload getUpdateGameUserByGameIdAndUserId() {
        return updateGameUserByGameIdAndUserId;
    }
    /**
     * Updates a single `GameUser` using a unique key and a patch.
     */
    public void setUpdateGameUserByGameIdAndUserId(UpdateGameUserPayload updateGameUserByGameIdAndUserId) {
        this.updateGameUserByGameIdAndUserId = updateGameUserByGameIdAndUserId;
    }

    /**
     * Deletes a single `BannedDraftCard` using its globally unique id.
     */
    public DeleteBannedDraftCardPayload getDeleteBannedDraftCard() {
        return deleteBannedDraftCard;
    }
    /**
     * Deletes a single `BannedDraftCard` using its globally unique id.
     */
    public void setDeleteBannedDraftCard(DeleteBannedDraftCardPayload deleteBannedDraftCard) {
        this.deleteBannedDraftCard = deleteBannedDraftCard;
    }

    /**
     * Deletes a single `BannedDraftCard` using a unique key.
     */
    public DeleteBannedDraftCardPayload getDeleteBannedDraftCardByCardId() {
        return deleteBannedDraftCardByCardId;
    }
    /**
     * Deletes a single `BannedDraftCard` using a unique key.
     */
    public void setDeleteBannedDraftCardByCardId(DeleteBannedDraftCardPayload deleteBannedDraftCardByCardId) {
        this.deleteBannedDraftCardByCardId = deleteBannedDraftCardByCardId;
    }

    /**
     * Deletes a single `BotUser` using its globally unique id.
     */
    public DeleteBotUserPayload getDeleteBotUser() {
        return deleteBotUser;
    }
    /**
     * Deletes a single `BotUser` using its globally unique id.
     */
    public void setDeleteBotUser(DeleteBotUserPayload deleteBotUser) {
        this.deleteBotUser = deleteBotUser;
    }

    /**
     * Deletes a single `BotUser` using a unique key.
     */
    public DeleteBotUserPayload getDeleteBotUserById() {
        return deleteBotUserById;
    }
    /**
     * Deletes a single `BotUser` using a unique key.
     */
    public void setDeleteBotUserById(DeleteBotUserPayload deleteBotUserById) {
        this.deleteBotUserById = deleteBotUserById;
    }

    /**
     * Deletes a single `HardRemovalCard` using its globally unique id.
     */
    public DeleteHardRemovalCardPayload getDeleteHardRemovalCard() {
        return deleteHardRemovalCard;
    }
    /**
     * Deletes a single `HardRemovalCard` using its globally unique id.
     */
    public void setDeleteHardRemovalCard(DeleteHardRemovalCardPayload deleteHardRemovalCard) {
        this.deleteHardRemovalCard = deleteHardRemovalCard;
    }

    /**
     * Deletes a single `HardRemovalCard` using a unique key.
     */
    public DeleteHardRemovalCardPayload getDeleteHardRemovalCardByCardId() {
        return deleteHardRemovalCardByCardId;
    }
    /**
     * Deletes a single `HardRemovalCard` using a unique key.
     */
    public void setDeleteHardRemovalCardByCardId(DeleteHardRemovalCardPayload deleteHardRemovalCardByCardId) {
        this.deleteHardRemovalCardByCardId = deleteHardRemovalCardByCardId;
    }

    /**
     * Deletes a single `PublishedCard` using its globally unique id.
     */
    public DeletePublishedCardPayload getDeletePublishedCard() {
        return deletePublishedCard;
    }
    /**
     * Deletes a single `PublishedCard` using its globally unique id.
     */
    public void setDeletePublishedCard(DeletePublishedCardPayload deletePublishedCard) {
        this.deletePublishedCard = deletePublishedCard;
    }

    /**
     * Deletes a single `PublishedCard` using a unique key.
     */
    public DeletePublishedCardPayload getDeletePublishedCardById() {
        return deletePublishedCardById;
    }
    /**
     * Deletes a single `PublishedCard` using a unique key.
     */
    public void setDeletePublishedCardById(DeletePublishedCardPayload deletePublishedCardById) {
        this.deletePublishedCardById = deletePublishedCardById;
    }

    /**
     * Deletes a single `Guest` using its globally unique id.
     */
    public DeleteGuestPayload getDeleteGuest() {
        return deleteGuest;
    }
    /**
     * Deletes a single `Guest` using its globally unique id.
     */
    public void setDeleteGuest(DeleteGuestPayload deleteGuest) {
        this.deleteGuest = deleteGuest;
    }

    /**
     * Deletes a single `Guest` using a unique key.
     */
    public DeleteGuestPayload getDeleteGuestById() {
        return deleteGuestById;
    }
    /**
     * Deletes a single `Guest` using a unique key.
     */
    public void setDeleteGuestById(DeleteGuestPayload deleteGuestById) {
        this.deleteGuestById = deleteGuestById;
    }

    /**
     * Deletes a single `DeckShare` using its globally unique id.
     */
    public DeleteDeckSharePayload getDeleteDeckShare() {
        return deleteDeckShare;
    }
    /**
     * Deletes a single `DeckShare` using its globally unique id.
     */
    public void setDeleteDeckShare(DeleteDeckSharePayload deleteDeckShare) {
        this.deleteDeckShare = deleteDeckShare;
    }

    /**
     * Deletes a single `DeckShare` using a unique key.
     */
    public DeleteDeckSharePayload getDeleteDeckShareByDeckIdAndShareRecipientId() {
        return deleteDeckShareByDeckIdAndShareRecipientId;
    }
    /**
     * Deletes a single `DeckShare` using a unique key.
     */
    public void setDeleteDeckShareByDeckIdAndShareRecipientId(DeleteDeckSharePayload deleteDeckShareByDeckIdAndShareRecipientId) {
        this.deleteDeckShareByDeckIdAndShareRecipientId = deleteDeckShareByDeckIdAndShareRecipientId;
    }

    /**
     * Deletes a single `CardsInDeck` using its globally unique id.
     */
    public DeleteCardsInDeckPayload getDeleteCardsInDeck() {
        return deleteCardsInDeck;
    }
    /**
     * Deletes a single `CardsInDeck` using its globally unique id.
     */
    public void setDeleteCardsInDeck(DeleteCardsInDeckPayload deleteCardsInDeck) {
        this.deleteCardsInDeck = deleteCardsInDeck;
    }

    /**
     * Deletes a single `CardsInDeck` using a unique key.
     */
    public DeleteCardsInDeckPayload getDeleteCardsInDeckById() {
        return deleteCardsInDeckById;
    }
    /**
     * Deletes a single `CardsInDeck` using a unique key.
     */
    public void setDeleteCardsInDeckById(DeleteCardsInDeckPayload deleteCardsInDeckById) {
        this.deleteCardsInDeckById = deleteCardsInDeckById;
    }

    /**
     * Deletes a single `DeckPlayerAttributeTuple` using its globally unique id.
     */
    public DeleteDeckPlayerAttributeTuplePayload getDeleteDeckPlayerAttributeTuple() {
        return deleteDeckPlayerAttributeTuple;
    }
    /**
     * Deletes a single `DeckPlayerAttributeTuple` using its globally unique id.
     */
    public void setDeleteDeckPlayerAttributeTuple(DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTuple) {
        this.deleteDeckPlayerAttributeTuple = deleteDeckPlayerAttributeTuple;
    }

    /**
     * Deletes a single `DeckPlayerAttributeTuple` using a unique key.
     */
    public DeleteDeckPlayerAttributeTuplePayload getDeleteDeckPlayerAttributeTupleById() {
        return deleteDeckPlayerAttributeTupleById;
    }
    /**
     * Deletes a single `DeckPlayerAttributeTuple` using a unique key.
     */
    public void setDeleteDeckPlayerAttributeTupleById(DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTupleById) {
        this.deleteDeckPlayerAttributeTupleById = deleteDeckPlayerAttributeTupleById;
    }

    /**
     * Deletes a single `Friend` using its globally unique id.
     */
    public DeleteFriendPayload getDeleteFriend() {
        return deleteFriend;
    }
    /**
     * Deletes a single `Friend` using its globally unique id.
     */
    public void setDeleteFriend(DeleteFriendPayload deleteFriend) {
        this.deleteFriend = deleteFriend;
    }

    /**
     * Deletes a single `Friend` using a unique key.
     */
    public DeleteFriendPayload getDeleteFriendByIdAndFriend() {
        return deleteFriendByIdAndFriend;
    }
    /**
     * Deletes a single `Friend` using a unique key.
     */
    public void setDeleteFriendByIdAndFriend(DeleteFriendPayload deleteFriendByIdAndFriend) {
        this.deleteFriendByIdAndFriend = deleteFriendByIdAndFriend;
    }

    /**
     * Deletes a single `MatchmakingTicket` using its globally unique id.
     */
    public DeleteMatchmakingTicketPayload getDeleteMatchmakingTicket() {
        return deleteMatchmakingTicket;
    }
    /**
     * Deletes a single `MatchmakingTicket` using its globally unique id.
     */
    public void setDeleteMatchmakingTicket(DeleteMatchmakingTicketPayload deleteMatchmakingTicket) {
        this.deleteMatchmakingTicket = deleteMatchmakingTicket;
    }

    /**
     * Deletes a single `MatchmakingTicket` using a unique key.
     */
    public DeleteMatchmakingTicketPayload getDeleteMatchmakingTicketByUserId() {
        return deleteMatchmakingTicketByUserId;
    }
    /**
     * Deletes a single `MatchmakingTicket` using a unique key.
     */
    public void setDeleteMatchmakingTicketByUserId(DeleteMatchmakingTicketPayload deleteMatchmakingTicketByUserId) {
        this.deleteMatchmakingTicketByUserId = deleteMatchmakingTicketByUserId;
    }

    /**
     * Deletes a single `Deck` using its globally unique id.
     */
    public DeleteDeckPayload getDeleteDeck() {
        return deleteDeck;
    }
    /**
     * Deletes a single `Deck` using its globally unique id.
     */
    public void setDeleteDeck(DeleteDeckPayload deleteDeck) {
        this.deleteDeck = deleteDeck;
    }

    /**
     * Deletes a single `Deck` using a unique key.
     */
    public DeleteDeckPayload getDeleteDeckById() {
        return deleteDeckById;
    }
    /**
     * Deletes a single `Deck` using a unique key.
     */
    public void setDeleteDeckById(DeleteDeckPayload deleteDeckById) {
        this.deleteDeckById = deleteDeckById;
    }

    /**
     * Deletes a single `MatchmakingQueue` using its globally unique id.
     */
    public DeleteMatchmakingQueuePayload getDeleteMatchmakingQueue() {
        return deleteMatchmakingQueue;
    }
    /**
     * Deletes a single `MatchmakingQueue` using its globally unique id.
     */
    public void setDeleteMatchmakingQueue(DeleteMatchmakingQueuePayload deleteMatchmakingQueue) {
        this.deleteMatchmakingQueue = deleteMatchmakingQueue;
    }

    /**
     * Deletes a single `MatchmakingQueue` using a unique key.
     */
    public DeleteMatchmakingQueuePayload getDeleteMatchmakingQueueById() {
        return deleteMatchmakingQueueById;
    }
    /**
     * Deletes a single `MatchmakingQueue` using a unique key.
     */
    public void setDeleteMatchmakingQueueById(DeleteMatchmakingQueuePayload deleteMatchmakingQueueById) {
        this.deleteMatchmakingQueueById = deleteMatchmakingQueueById;
    }

    /**
     * Deletes a single `Card` using its globally unique id.
     */
    public DeleteCardPayload getDeleteCard() {
        return deleteCard;
    }
    /**
     * Deletes a single `Card` using its globally unique id.
     */
    public void setDeleteCard(DeleteCardPayload deleteCard) {
        this.deleteCard = deleteCard;
    }

    /**
     * Deletes a single `Card` using a unique key.
     */
    public DeleteCardPayload getDeleteCardBySuccession() {
        return deleteCardBySuccession;
    }
    /**
     * Deletes a single `Card` using a unique key.
     */
    public void setDeleteCardBySuccession(DeleteCardPayload deleteCardBySuccession) {
        this.deleteCardBySuccession = deleteCardBySuccession;
    }

    /**
     * Deletes a single `GeneratedArt` using a unique key.
     */
    public DeleteGeneratedArtPayload getDeleteGeneratedArtByHashAndOwner() {
        return deleteGeneratedArtByHashAndOwner;
    }
    /**
     * Deletes a single `GeneratedArt` using a unique key.
     */
    public void setDeleteGeneratedArtByHashAndOwner(DeleteGeneratedArtPayload deleteGeneratedArtByHashAndOwner) {
        this.deleteGeneratedArtByHashAndOwner = deleteGeneratedArtByHashAndOwner;
    }

    /**
     * Deletes a single `Game` using its globally unique id.
     */
    public DeleteGamePayload getDeleteGame() {
        return deleteGame;
    }
    /**
     * Deletes a single `Game` using its globally unique id.
     */
    public void setDeleteGame(DeleteGamePayload deleteGame) {
        this.deleteGame = deleteGame;
    }

    /**
     * Deletes a single `Game` using a unique key.
     */
    public DeleteGamePayload getDeleteGameById() {
        return deleteGameById;
    }
    /**
     * Deletes a single `Game` using a unique key.
     */
    public void setDeleteGameById(DeleteGamePayload deleteGameById) {
        this.deleteGameById = deleteGameById;
    }

    /**
     * Deletes a single `GameUser` using its globally unique id.
     */
    public DeleteGameUserPayload getDeleteGameUser() {
        return deleteGameUser;
    }
    /**
     * Deletes a single `GameUser` using its globally unique id.
     */
    public void setDeleteGameUser(DeleteGameUserPayload deleteGameUser) {
        this.deleteGameUser = deleteGameUser;
    }

    /**
     * Deletes a single `GameUser` using a unique key.
     */
    public DeleteGameUserPayload getDeleteGameUserByGameIdAndUserId() {
        return deleteGameUserByGameIdAndUserId;
    }
    /**
     * Deletes a single `GameUser` using a unique key.
     */
    public void setDeleteGameUserByGameIdAndUserId(DeleteGameUserPayload deleteGameUserByGameIdAndUserId) {
        this.deleteGameUserByGameIdAndUserId = deleteGameUserByGameIdAndUserId;
    }



    public static Mutation.Builder builder() {
        return new Mutation.Builder();
    }

    public static class Builder {

        private CardCatalogueGetBannedDraftCardsPayload cardCatalogueGetBannedDraftCards;
        private CardCatalogueGetHardRemovalCardsPayload cardCatalogueGetHardRemovalCards;
        private ArchiveCardPayload archiveCard;
        private PublishCardPayload publishCard;
        private SetUserAttributePayload setUserAttribute;
        private GetUserAttributePayload getUserAttribute;
        private ClusteredGamesUpdateGameAndUsersPayload clusteredGamesUpdateGameAndUsers;
        private GetClassesPayload getClasses;
        private GetCollectionCardsPayload getCollectionCards;
        private SetCardsInDeckPayload setCardsInDeck;
        private CreateDeckWithCardsPayload createDeckWithCards;
        private CardCatalogueFormatsPayload cardCatalogueFormats;
        private CardCatalogueGetClassCardsPayload cardCatalogueGetClassCards;
        private CardCatalogueGetBaseClassesPayload cardCatalogueGetBaseClasses;
        private CardCatalogueGetCardByIdPayload cardCatalogueGetCardById;
        private CardCatalogueGetCardByNamePayload cardCatalogueGetCardByName;
        private CardCatalogueGetFormatPayload cardCatalogueGetFormat;
        private CardCatalogueGetHeroCardPayload cardCatalogueGetHeroCard;
        private CardCatalogueGetCardByNameAndClassPayload cardCatalogueGetCardByNameAndClass;
        private PublishGitCardPayload publishGitCard;
        private SaveCardPayload saveCard;
        private CardCatalogueQueryPayload cardCatalogueQuery;
        private SaveGeneratedArtPayload saveGeneratedArt;
        private CreateBannedDraftCardPayload createBannedDraftCard;
        private CreateBotUserPayload createBotUser;
        private CreateHardRemovalCardPayload createHardRemovalCard;
        private CreatePublishedCardPayload createPublishedCard;
        private CreateGuestPayload createGuest;
        private CreateDeckSharePayload createDeckShare;
        private CreateCardsInDeckPayload createCardsInDeck;
        private CreateDeckPlayerAttributeTuplePayload createDeckPlayerAttributeTuple;
        private CreateFriendPayload createFriend;
        private CreateMatchmakingTicketPayload createMatchmakingTicket;
        private CreateDeckPayload createDeck;
        private CreateMatchmakingQueuePayload createMatchmakingQueue;
        private CreateCardPayload createCard;
        private CreateGeneratedArtPayload createGeneratedArt;
        private CreateGamePayload createGame;
        private CreateGameUserPayload createGameUser;
        private UpdateBannedDraftCardPayload updateBannedDraftCard;
        private UpdateBannedDraftCardPayload updateBannedDraftCardByCardId;
        private UpdateBotUserPayload updateBotUser;
        private UpdateBotUserPayload updateBotUserById;
        private UpdateHardRemovalCardPayload updateHardRemovalCard;
        private UpdateHardRemovalCardPayload updateHardRemovalCardByCardId;
        private UpdatePublishedCardPayload updatePublishedCard;
        private UpdatePublishedCardPayload updatePublishedCardById;
        private UpdateGuestPayload updateGuest;
        private UpdateGuestPayload updateGuestById;
        private UpdateDeckSharePayload updateDeckShare;
        private UpdateDeckSharePayload updateDeckShareByDeckIdAndShareRecipientId;
        private UpdateCardsInDeckPayload updateCardsInDeck;
        private UpdateCardsInDeckPayload updateCardsInDeckById;
        private UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTuple;
        private UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTupleById;
        private UpdateFriendPayload updateFriend;
        private UpdateFriendPayload updateFriendByIdAndFriend;
        private UpdateMatchmakingTicketPayload updateMatchmakingTicket;
        private UpdateMatchmakingTicketPayload updateMatchmakingTicketByUserId;
        private UpdateDeckPayload updateDeck;
        private UpdateDeckPayload updateDeckById;
        private UpdateMatchmakingQueuePayload updateMatchmakingQueue;
        private UpdateMatchmakingQueuePayload updateMatchmakingQueueById;
        private UpdateCardPayload updateCard;
        private UpdateCardPayload updateCardBySuccession;
        private UpdateGeneratedArtPayload updateGeneratedArtByHashAndOwner;
        private UpdateGamePayload updateGame;
        private UpdateGamePayload updateGameById;
        private UpdateGameUserPayload updateGameUser;
        private UpdateGameUserPayload updateGameUserByGameIdAndUserId;
        private DeleteBannedDraftCardPayload deleteBannedDraftCard;
        private DeleteBannedDraftCardPayload deleteBannedDraftCardByCardId;
        private DeleteBotUserPayload deleteBotUser;
        private DeleteBotUserPayload deleteBotUserById;
        private DeleteHardRemovalCardPayload deleteHardRemovalCard;
        private DeleteHardRemovalCardPayload deleteHardRemovalCardByCardId;
        private DeletePublishedCardPayload deletePublishedCard;
        private DeletePublishedCardPayload deletePublishedCardById;
        private DeleteGuestPayload deleteGuest;
        private DeleteGuestPayload deleteGuestById;
        private DeleteDeckSharePayload deleteDeckShare;
        private DeleteDeckSharePayload deleteDeckShareByDeckIdAndShareRecipientId;
        private DeleteCardsInDeckPayload deleteCardsInDeck;
        private DeleteCardsInDeckPayload deleteCardsInDeckById;
        private DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTuple;
        private DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTupleById;
        private DeleteFriendPayload deleteFriend;
        private DeleteFriendPayload deleteFriendByIdAndFriend;
        private DeleteMatchmakingTicketPayload deleteMatchmakingTicket;
        private DeleteMatchmakingTicketPayload deleteMatchmakingTicketByUserId;
        private DeleteDeckPayload deleteDeck;
        private DeleteDeckPayload deleteDeckById;
        private DeleteMatchmakingQueuePayload deleteMatchmakingQueue;
        private DeleteMatchmakingQueuePayload deleteMatchmakingQueueById;
        private DeleteCardPayload deleteCard;
        private DeleteCardPayload deleteCardBySuccession;
        private DeleteGeneratedArtPayload deleteGeneratedArtByHashAndOwner;
        private DeleteGamePayload deleteGame;
        private DeleteGamePayload deleteGameById;
        private DeleteGameUserPayload deleteGameUser;
        private DeleteGameUserPayload deleteGameUserByGameIdAndUserId;

        public Builder() {
        }

        public Builder setCardCatalogueGetBannedDraftCards(CardCatalogueGetBannedDraftCardsPayload cardCatalogueGetBannedDraftCards) {
            this.cardCatalogueGetBannedDraftCards = cardCatalogueGetBannedDraftCards;
            return this;
        }

        public Builder setCardCatalogueGetHardRemovalCards(CardCatalogueGetHardRemovalCardsPayload cardCatalogueGetHardRemovalCards) {
            this.cardCatalogueGetHardRemovalCards = cardCatalogueGetHardRemovalCards;
            return this;
        }

        public Builder setArchiveCard(ArchiveCardPayload archiveCard) {
            this.archiveCard = archiveCard;
            return this;
        }

        public Builder setPublishCard(PublishCardPayload publishCard) {
            this.publishCard = publishCard;
            return this;
        }

        public Builder setSetUserAttribute(SetUserAttributePayload setUserAttribute) {
            this.setUserAttribute = setUserAttribute;
            return this;
        }

        public Builder setGetUserAttribute(GetUserAttributePayload getUserAttribute) {
            this.getUserAttribute = getUserAttribute;
            return this;
        }

        public Builder setClusteredGamesUpdateGameAndUsers(ClusteredGamesUpdateGameAndUsersPayload clusteredGamesUpdateGameAndUsers) {
            this.clusteredGamesUpdateGameAndUsers = clusteredGamesUpdateGameAndUsers;
            return this;
        }

        public Builder setGetClasses(GetClassesPayload getClasses) {
            this.getClasses = getClasses;
            return this;
        }

        public Builder setGetCollectionCards(GetCollectionCardsPayload getCollectionCards) {
            this.getCollectionCards = getCollectionCards;
            return this;
        }

        public Builder setSetCardsInDeck(SetCardsInDeckPayload setCardsInDeck) {
            this.setCardsInDeck = setCardsInDeck;
            return this;
        }

        public Builder setCreateDeckWithCards(CreateDeckWithCardsPayload createDeckWithCards) {
            this.createDeckWithCards = createDeckWithCards;
            return this;
        }

        public Builder setCardCatalogueFormats(CardCatalogueFormatsPayload cardCatalogueFormats) {
            this.cardCatalogueFormats = cardCatalogueFormats;
            return this;
        }

        public Builder setCardCatalogueGetClassCards(CardCatalogueGetClassCardsPayload cardCatalogueGetClassCards) {
            this.cardCatalogueGetClassCards = cardCatalogueGetClassCards;
            return this;
        }

        public Builder setCardCatalogueGetBaseClasses(CardCatalogueGetBaseClassesPayload cardCatalogueGetBaseClasses) {
            this.cardCatalogueGetBaseClasses = cardCatalogueGetBaseClasses;
            return this;
        }

        public Builder setCardCatalogueGetCardById(CardCatalogueGetCardByIdPayload cardCatalogueGetCardById) {
            this.cardCatalogueGetCardById = cardCatalogueGetCardById;
            return this;
        }

        public Builder setCardCatalogueGetCardByName(CardCatalogueGetCardByNamePayload cardCatalogueGetCardByName) {
            this.cardCatalogueGetCardByName = cardCatalogueGetCardByName;
            return this;
        }

        public Builder setCardCatalogueGetFormat(CardCatalogueGetFormatPayload cardCatalogueGetFormat) {
            this.cardCatalogueGetFormat = cardCatalogueGetFormat;
            return this;
        }

        public Builder setCardCatalogueGetHeroCard(CardCatalogueGetHeroCardPayload cardCatalogueGetHeroCard) {
            this.cardCatalogueGetHeroCard = cardCatalogueGetHeroCard;
            return this;
        }

        public Builder setCardCatalogueGetCardByNameAndClass(CardCatalogueGetCardByNameAndClassPayload cardCatalogueGetCardByNameAndClass) {
            this.cardCatalogueGetCardByNameAndClass = cardCatalogueGetCardByNameAndClass;
            return this;
        }

        public Builder setPublishGitCard(PublishGitCardPayload publishGitCard) {
            this.publishGitCard = publishGitCard;
            return this;
        }

        public Builder setSaveCard(SaveCardPayload saveCard) {
            this.saveCard = saveCard;
            return this;
        }

        public Builder setCardCatalogueQuery(CardCatalogueQueryPayload cardCatalogueQuery) {
            this.cardCatalogueQuery = cardCatalogueQuery;
            return this;
        }

        public Builder setSaveGeneratedArt(SaveGeneratedArtPayload saveGeneratedArt) {
            this.saveGeneratedArt = saveGeneratedArt;
            return this;
        }

        /**
         * Creates a single `BannedDraftCard`.
         */
        public Builder setCreateBannedDraftCard(CreateBannedDraftCardPayload createBannedDraftCard) {
            this.createBannedDraftCard = createBannedDraftCard;
            return this;
        }

        /**
         * Creates a single `BotUser`.
         */
        public Builder setCreateBotUser(CreateBotUserPayload createBotUser) {
            this.createBotUser = createBotUser;
            return this;
        }

        /**
         * Creates a single `HardRemovalCard`.
         */
        public Builder setCreateHardRemovalCard(CreateHardRemovalCardPayload createHardRemovalCard) {
            this.createHardRemovalCard = createHardRemovalCard;
            return this;
        }

        /**
         * Creates a single `PublishedCard`.
         */
        public Builder setCreatePublishedCard(CreatePublishedCardPayload createPublishedCard) {
            this.createPublishedCard = createPublishedCard;
            return this;
        }

        /**
         * Creates a single `Guest`.
         */
        public Builder setCreateGuest(CreateGuestPayload createGuest) {
            this.createGuest = createGuest;
            return this;
        }

        /**
         * Creates a single `DeckShare`.
         */
        public Builder setCreateDeckShare(CreateDeckSharePayload createDeckShare) {
            this.createDeckShare = createDeckShare;
            return this;
        }

        /**
         * Creates a single `CardsInDeck`.
         */
        public Builder setCreateCardsInDeck(CreateCardsInDeckPayload createCardsInDeck) {
            this.createCardsInDeck = createCardsInDeck;
            return this;
        }

        /**
         * Creates a single `DeckPlayerAttributeTuple`.
         */
        public Builder setCreateDeckPlayerAttributeTuple(CreateDeckPlayerAttributeTuplePayload createDeckPlayerAttributeTuple) {
            this.createDeckPlayerAttributeTuple = createDeckPlayerAttributeTuple;
            return this;
        }

        /**
         * Creates a single `Friend`.
         */
        public Builder setCreateFriend(CreateFriendPayload createFriend) {
            this.createFriend = createFriend;
            return this;
        }

        /**
         * Creates a single `MatchmakingTicket`.
         */
        public Builder setCreateMatchmakingTicket(CreateMatchmakingTicketPayload createMatchmakingTicket) {
            this.createMatchmakingTicket = createMatchmakingTicket;
            return this;
        }

        /**
         * Creates a single `Deck`.
         */
        public Builder setCreateDeck(CreateDeckPayload createDeck) {
            this.createDeck = createDeck;
            return this;
        }

        /**
         * Creates a single `MatchmakingQueue`.
         */
        public Builder setCreateMatchmakingQueue(CreateMatchmakingQueuePayload createMatchmakingQueue) {
            this.createMatchmakingQueue = createMatchmakingQueue;
            return this;
        }

        /**
         * Creates a single `Card`.
         */
        public Builder setCreateCard(CreateCardPayload createCard) {
            this.createCard = createCard;
            return this;
        }

        /**
         * Creates a single `GeneratedArt`.
         */
        public Builder setCreateGeneratedArt(CreateGeneratedArtPayload createGeneratedArt) {
            this.createGeneratedArt = createGeneratedArt;
            return this;
        }

        /**
         * Creates a single `Game`.
         */
        public Builder setCreateGame(CreateGamePayload createGame) {
            this.createGame = createGame;
            return this;
        }

        /**
         * Creates a single `GameUser`.
         */
        public Builder setCreateGameUser(CreateGameUserPayload createGameUser) {
            this.createGameUser = createGameUser;
            return this;
        }

        /**
         * Updates a single `BannedDraftCard` using its globally unique id and a patch.
         */
        public Builder setUpdateBannedDraftCard(UpdateBannedDraftCardPayload updateBannedDraftCard) {
            this.updateBannedDraftCard = updateBannedDraftCard;
            return this;
        }

        /**
         * Updates a single `BannedDraftCard` using a unique key and a patch.
         */
        public Builder setUpdateBannedDraftCardByCardId(UpdateBannedDraftCardPayload updateBannedDraftCardByCardId) {
            this.updateBannedDraftCardByCardId = updateBannedDraftCardByCardId;
            return this;
        }

        /**
         * Updates a single `BotUser` using its globally unique id and a patch.
         */
        public Builder setUpdateBotUser(UpdateBotUserPayload updateBotUser) {
            this.updateBotUser = updateBotUser;
            return this;
        }

        /**
         * Updates a single `BotUser` using a unique key and a patch.
         */
        public Builder setUpdateBotUserById(UpdateBotUserPayload updateBotUserById) {
            this.updateBotUserById = updateBotUserById;
            return this;
        }

        /**
         * Updates a single `HardRemovalCard` using its globally unique id and a patch.
         */
        public Builder setUpdateHardRemovalCard(UpdateHardRemovalCardPayload updateHardRemovalCard) {
            this.updateHardRemovalCard = updateHardRemovalCard;
            return this;
        }

        /**
         * Updates a single `HardRemovalCard` using a unique key and a patch.
         */
        public Builder setUpdateHardRemovalCardByCardId(UpdateHardRemovalCardPayload updateHardRemovalCardByCardId) {
            this.updateHardRemovalCardByCardId = updateHardRemovalCardByCardId;
            return this;
        }

        /**
         * Updates a single `PublishedCard` using its globally unique id and a patch.
         */
        public Builder setUpdatePublishedCard(UpdatePublishedCardPayload updatePublishedCard) {
            this.updatePublishedCard = updatePublishedCard;
            return this;
        }

        /**
         * Updates a single `PublishedCard` using a unique key and a patch.
         */
        public Builder setUpdatePublishedCardById(UpdatePublishedCardPayload updatePublishedCardById) {
            this.updatePublishedCardById = updatePublishedCardById;
            return this;
        }

        /**
         * Updates a single `Guest` using its globally unique id and a patch.
         */
        public Builder setUpdateGuest(UpdateGuestPayload updateGuest) {
            this.updateGuest = updateGuest;
            return this;
        }

        /**
         * Updates a single `Guest` using a unique key and a patch.
         */
        public Builder setUpdateGuestById(UpdateGuestPayload updateGuestById) {
            this.updateGuestById = updateGuestById;
            return this;
        }

        /**
         * Updates a single `DeckShare` using its globally unique id and a patch.
         */
        public Builder setUpdateDeckShare(UpdateDeckSharePayload updateDeckShare) {
            this.updateDeckShare = updateDeckShare;
            return this;
        }

        /**
         * Updates a single `DeckShare` using a unique key and a patch.
         */
        public Builder setUpdateDeckShareByDeckIdAndShareRecipientId(UpdateDeckSharePayload updateDeckShareByDeckIdAndShareRecipientId) {
            this.updateDeckShareByDeckIdAndShareRecipientId = updateDeckShareByDeckIdAndShareRecipientId;
            return this;
        }

        /**
         * Updates a single `CardsInDeck` using its globally unique id and a patch.
         */
        public Builder setUpdateCardsInDeck(UpdateCardsInDeckPayload updateCardsInDeck) {
            this.updateCardsInDeck = updateCardsInDeck;
            return this;
        }

        /**
         * Updates a single `CardsInDeck` using a unique key and a patch.
         */
        public Builder setUpdateCardsInDeckById(UpdateCardsInDeckPayload updateCardsInDeckById) {
            this.updateCardsInDeckById = updateCardsInDeckById;
            return this;
        }

        /**
         * Updates a single `DeckPlayerAttributeTuple` using its globally unique id and a patch.
         */
        public Builder setUpdateDeckPlayerAttributeTuple(UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTuple) {
            this.updateDeckPlayerAttributeTuple = updateDeckPlayerAttributeTuple;
            return this;
        }

        /**
         * Updates a single `DeckPlayerAttributeTuple` using a unique key and a patch.
         */
        public Builder setUpdateDeckPlayerAttributeTupleById(UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTupleById) {
            this.updateDeckPlayerAttributeTupleById = updateDeckPlayerAttributeTupleById;
            return this;
        }

        /**
         * Updates a single `Friend` using its globally unique id and a patch.
         */
        public Builder setUpdateFriend(UpdateFriendPayload updateFriend) {
            this.updateFriend = updateFriend;
            return this;
        }

        /**
         * Updates a single `Friend` using a unique key and a patch.
         */
        public Builder setUpdateFriendByIdAndFriend(UpdateFriendPayload updateFriendByIdAndFriend) {
            this.updateFriendByIdAndFriend = updateFriendByIdAndFriend;
            return this;
        }

        /**
         * Updates a single `MatchmakingTicket` using its globally unique id and a patch.
         */
        public Builder setUpdateMatchmakingTicket(UpdateMatchmakingTicketPayload updateMatchmakingTicket) {
            this.updateMatchmakingTicket = updateMatchmakingTicket;
            return this;
        }

        /**
         * Updates a single `MatchmakingTicket` using a unique key and a patch.
         */
        public Builder setUpdateMatchmakingTicketByUserId(UpdateMatchmakingTicketPayload updateMatchmakingTicketByUserId) {
            this.updateMatchmakingTicketByUserId = updateMatchmakingTicketByUserId;
            return this;
        }

        /**
         * Updates a single `Deck` using its globally unique id and a patch.
         */
        public Builder setUpdateDeck(UpdateDeckPayload updateDeck) {
            this.updateDeck = updateDeck;
            return this;
        }

        /**
         * Updates a single `Deck` using a unique key and a patch.
         */
        public Builder setUpdateDeckById(UpdateDeckPayload updateDeckById) {
            this.updateDeckById = updateDeckById;
            return this;
        }

        /**
         * Updates a single `MatchmakingQueue` using its globally unique id and a patch.
         */
        public Builder setUpdateMatchmakingQueue(UpdateMatchmakingQueuePayload updateMatchmakingQueue) {
            this.updateMatchmakingQueue = updateMatchmakingQueue;
            return this;
        }

        /**
         * Updates a single `MatchmakingQueue` using a unique key and a patch.
         */
        public Builder setUpdateMatchmakingQueueById(UpdateMatchmakingQueuePayload updateMatchmakingQueueById) {
            this.updateMatchmakingQueueById = updateMatchmakingQueueById;
            return this;
        }

        /**
         * Updates a single `Card` using its globally unique id and a patch.
         */
        public Builder setUpdateCard(UpdateCardPayload updateCard) {
            this.updateCard = updateCard;
            return this;
        }

        /**
         * Updates a single `Card` using a unique key and a patch.
         */
        public Builder setUpdateCardBySuccession(UpdateCardPayload updateCardBySuccession) {
            this.updateCardBySuccession = updateCardBySuccession;
            return this;
        }

        /**
         * Updates a single `GeneratedArt` using a unique key and a patch.
         */
        public Builder setUpdateGeneratedArtByHashAndOwner(UpdateGeneratedArtPayload updateGeneratedArtByHashAndOwner) {
            this.updateGeneratedArtByHashAndOwner = updateGeneratedArtByHashAndOwner;
            return this;
        }

        /**
         * Updates a single `Game` using its globally unique id and a patch.
         */
        public Builder setUpdateGame(UpdateGamePayload updateGame) {
            this.updateGame = updateGame;
            return this;
        }

        /**
         * Updates a single `Game` using a unique key and a patch.
         */
        public Builder setUpdateGameById(UpdateGamePayload updateGameById) {
            this.updateGameById = updateGameById;
            return this;
        }

        /**
         * Updates a single `GameUser` using its globally unique id and a patch.
         */
        public Builder setUpdateGameUser(UpdateGameUserPayload updateGameUser) {
            this.updateGameUser = updateGameUser;
            return this;
        }

        /**
         * Updates a single `GameUser` using a unique key and a patch.
         */
        public Builder setUpdateGameUserByGameIdAndUserId(UpdateGameUserPayload updateGameUserByGameIdAndUserId) {
            this.updateGameUserByGameIdAndUserId = updateGameUserByGameIdAndUserId;
            return this;
        }

        /**
         * Deletes a single `BannedDraftCard` using its globally unique id.
         */
        public Builder setDeleteBannedDraftCard(DeleteBannedDraftCardPayload deleteBannedDraftCard) {
            this.deleteBannedDraftCard = deleteBannedDraftCard;
            return this;
        }

        /**
         * Deletes a single `BannedDraftCard` using a unique key.
         */
        public Builder setDeleteBannedDraftCardByCardId(DeleteBannedDraftCardPayload deleteBannedDraftCardByCardId) {
            this.deleteBannedDraftCardByCardId = deleteBannedDraftCardByCardId;
            return this;
        }

        /**
         * Deletes a single `BotUser` using its globally unique id.
         */
        public Builder setDeleteBotUser(DeleteBotUserPayload deleteBotUser) {
            this.deleteBotUser = deleteBotUser;
            return this;
        }

        /**
         * Deletes a single `BotUser` using a unique key.
         */
        public Builder setDeleteBotUserById(DeleteBotUserPayload deleteBotUserById) {
            this.deleteBotUserById = deleteBotUserById;
            return this;
        }

        /**
         * Deletes a single `HardRemovalCard` using its globally unique id.
         */
        public Builder setDeleteHardRemovalCard(DeleteHardRemovalCardPayload deleteHardRemovalCard) {
            this.deleteHardRemovalCard = deleteHardRemovalCard;
            return this;
        }

        /**
         * Deletes a single `HardRemovalCard` using a unique key.
         */
        public Builder setDeleteHardRemovalCardByCardId(DeleteHardRemovalCardPayload deleteHardRemovalCardByCardId) {
            this.deleteHardRemovalCardByCardId = deleteHardRemovalCardByCardId;
            return this;
        }

        /**
         * Deletes a single `PublishedCard` using its globally unique id.
         */
        public Builder setDeletePublishedCard(DeletePublishedCardPayload deletePublishedCard) {
            this.deletePublishedCard = deletePublishedCard;
            return this;
        }

        /**
         * Deletes a single `PublishedCard` using a unique key.
         */
        public Builder setDeletePublishedCardById(DeletePublishedCardPayload deletePublishedCardById) {
            this.deletePublishedCardById = deletePublishedCardById;
            return this;
        }

        /**
         * Deletes a single `Guest` using its globally unique id.
         */
        public Builder setDeleteGuest(DeleteGuestPayload deleteGuest) {
            this.deleteGuest = deleteGuest;
            return this;
        }

        /**
         * Deletes a single `Guest` using a unique key.
         */
        public Builder setDeleteGuestById(DeleteGuestPayload deleteGuestById) {
            this.deleteGuestById = deleteGuestById;
            return this;
        }

        /**
         * Deletes a single `DeckShare` using its globally unique id.
         */
        public Builder setDeleteDeckShare(DeleteDeckSharePayload deleteDeckShare) {
            this.deleteDeckShare = deleteDeckShare;
            return this;
        }

        /**
         * Deletes a single `DeckShare` using a unique key.
         */
        public Builder setDeleteDeckShareByDeckIdAndShareRecipientId(DeleteDeckSharePayload deleteDeckShareByDeckIdAndShareRecipientId) {
            this.deleteDeckShareByDeckIdAndShareRecipientId = deleteDeckShareByDeckIdAndShareRecipientId;
            return this;
        }

        /**
         * Deletes a single `CardsInDeck` using its globally unique id.
         */
        public Builder setDeleteCardsInDeck(DeleteCardsInDeckPayload deleteCardsInDeck) {
            this.deleteCardsInDeck = deleteCardsInDeck;
            return this;
        }

        /**
         * Deletes a single `CardsInDeck` using a unique key.
         */
        public Builder setDeleteCardsInDeckById(DeleteCardsInDeckPayload deleteCardsInDeckById) {
            this.deleteCardsInDeckById = deleteCardsInDeckById;
            return this;
        }

        /**
         * Deletes a single `DeckPlayerAttributeTuple` using its globally unique id.
         */
        public Builder setDeleteDeckPlayerAttributeTuple(DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTuple) {
            this.deleteDeckPlayerAttributeTuple = deleteDeckPlayerAttributeTuple;
            return this;
        }

        /**
         * Deletes a single `DeckPlayerAttributeTuple` using a unique key.
         */
        public Builder setDeleteDeckPlayerAttributeTupleById(DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTupleById) {
            this.deleteDeckPlayerAttributeTupleById = deleteDeckPlayerAttributeTupleById;
            return this;
        }

        /**
         * Deletes a single `Friend` using its globally unique id.
         */
        public Builder setDeleteFriend(DeleteFriendPayload deleteFriend) {
            this.deleteFriend = deleteFriend;
            return this;
        }

        /**
         * Deletes a single `Friend` using a unique key.
         */
        public Builder setDeleteFriendByIdAndFriend(DeleteFriendPayload deleteFriendByIdAndFriend) {
            this.deleteFriendByIdAndFriend = deleteFriendByIdAndFriend;
            return this;
        }

        /**
         * Deletes a single `MatchmakingTicket` using its globally unique id.
         */
        public Builder setDeleteMatchmakingTicket(DeleteMatchmakingTicketPayload deleteMatchmakingTicket) {
            this.deleteMatchmakingTicket = deleteMatchmakingTicket;
            return this;
        }

        /**
         * Deletes a single `MatchmakingTicket` using a unique key.
         */
        public Builder setDeleteMatchmakingTicketByUserId(DeleteMatchmakingTicketPayload deleteMatchmakingTicketByUserId) {
            this.deleteMatchmakingTicketByUserId = deleteMatchmakingTicketByUserId;
            return this;
        }

        /**
         * Deletes a single `Deck` using its globally unique id.
         */
        public Builder setDeleteDeck(DeleteDeckPayload deleteDeck) {
            this.deleteDeck = deleteDeck;
            return this;
        }

        /**
         * Deletes a single `Deck` using a unique key.
         */
        public Builder setDeleteDeckById(DeleteDeckPayload deleteDeckById) {
            this.deleteDeckById = deleteDeckById;
            return this;
        }

        /**
         * Deletes a single `MatchmakingQueue` using its globally unique id.
         */
        public Builder setDeleteMatchmakingQueue(DeleteMatchmakingQueuePayload deleteMatchmakingQueue) {
            this.deleteMatchmakingQueue = deleteMatchmakingQueue;
            return this;
        }

        /**
         * Deletes a single `MatchmakingQueue` using a unique key.
         */
        public Builder setDeleteMatchmakingQueueById(DeleteMatchmakingQueuePayload deleteMatchmakingQueueById) {
            this.deleteMatchmakingQueueById = deleteMatchmakingQueueById;
            return this;
        }

        /**
         * Deletes a single `Card` using its globally unique id.
         */
        public Builder setDeleteCard(DeleteCardPayload deleteCard) {
            this.deleteCard = deleteCard;
            return this;
        }

        /**
         * Deletes a single `Card` using a unique key.
         */
        public Builder setDeleteCardBySuccession(DeleteCardPayload deleteCardBySuccession) {
            this.deleteCardBySuccession = deleteCardBySuccession;
            return this;
        }

        /**
         * Deletes a single `GeneratedArt` using a unique key.
         */
        public Builder setDeleteGeneratedArtByHashAndOwner(DeleteGeneratedArtPayload deleteGeneratedArtByHashAndOwner) {
            this.deleteGeneratedArtByHashAndOwner = deleteGeneratedArtByHashAndOwner;
            return this;
        }

        /**
         * Deletes a single `Game` using its globally unique id.
         */
        public Builder setDeleteGame(DeleteGamePayload deleteGame) {
            this.deleteGame = deleteGame;
            return this;
        }

        /**
         * Deletes a single `Game` using a unique key.
         */
        public Builder setDeleteGameById(DeleteGamePayload deleteGameById) {
            this.deleteGameById = deleteGameById;
            return this;
        }

        /**
         * Deletes a single `GameUser` using its globally unique id.
         */
        public Builder setDeleteGameUser(DeleteGameUserPayload deleteGameUser) {
            this.deleteGameUser = deleteGameUser;
            return this;
        }

        /**
         * Deletes a single `GameUser` using a unique key.
         */
        public Builder setDeleteGameUserByGameIdAndUserId(DeleteGameUserPayload deleteGameUserByGameIdAndUserId) {
            this.deleteGameUserByGameIdAndUserId = deleteGameUserByGameIdAndUserId;
            return this;
        }


        public Mutation build() {
            return new Mutation(cardCatalogueGetBannedDraftCards, cardCatalogueGetHardRemovalCards, archiveCard, publishCard, setUserAttribute, getUserAttribute, clusteredGamesUpdateGameAndUsers, getClasses, getCollectionCards, setCardsInDeck, createDeckWithCards, cardCatalogueFormats, cardCatalogueGetClassCards, cardCatalogueGetBaseClasses, cardCatalogueGetCardById, cardCatalogueGetCardByName, cardCatalogueGetFormat, cardCatalogueGetHeroCard, cardCatalogueGetCardByNameAndClass, publishGitCard, saveCard, cardCatalogueQuery, saveGeneratedArt, createBannedDraftCard, createBotUser, createHardRemovalCard, createPublishedCard, createGuest, createDeckShare, createCardsInDeck, createDeckPlayerAttributeTuple, createFriend, createMatchmakingTicket, createDeck, createMatchmakingQueue, createCard, createGeneratedArt, createGame, createGameUser, updateBannedDraftCard, updateBannedDraftCardByCardId, updateBotUser, updateBotUserById, updateHardRemovalCard, updateHardRemovalCardByCardId, updatePublishedCard, updatePublishedCardById, updateGuest, updateGuestById, updateDeckShare, updateDeckShareByDeckIdAndShareRecipientId, updateCardsInDeck, updateCardsInDeckById, updateDeckPlayerAttributeTuple, updateDeckPlayerAttributeTupleById, updateFriend, updateFriendByIdAndFriend, updateMatchmakingTicket, updateMatchmakingTicketByUserId, updateDeck, updateDeckById, updateMatchmakingQueue, updateMatchmakingQueueById, updateCard, updateCardBySuccession, updateGeneratedArtByHashAndOwner, updateGame, updateGameById, updateGameUser, updateGameUserByGameIdAndUserId, deleteBannedDraftCard, deleteBannedDraftCardByCardId, deleteBotUser, deleteBotUserById, deleteHardRemovalCard, deleteHardRemovalCardByCardId, deletePublishedCard, deletePublishedCardById, deleteGuest, deleteGuestById, deleteDeckShare, deleteDeckShareByDeckIdAndShareRecipientId, deleteCardsInDeck, deleteCardsInDeckById, deleteDeckPlayerAttributeTuple, deleteDeckPlayerAttributeTupleById, deleteFriend, deleteFriendByIdAndFriend, deleteMatchmakingTicket, deleteMatchmakingTicketByUserId, deleteDeck, deleteDeckById, deleteMatchmakingQueue, deleteMatchmakingQueueById, deleteCard, deleteCardBySuccession, deleteGeneratedArtByHashAndOwner, deleteGame, deleteGameById, deleteGameUser, deleteGameUserByGameIdAndUserId);
        }

    }
}
