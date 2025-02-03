package com.hiddenswitch.framework.graphql;


/**
 * The root mutation type which contains root level fields which mutate data.
 */
public interface MutationResolver {

    CardCatalogueGetBannedDraftCardsPayload cardCatalogueGetBannedDraftCards(CardCatalogueGetBannedDraftCardsInput input) throws Exception;

    CardCatalogueGetHardRemovalCardsPayload cardCatalogueGetHardRemovalCards(CardCatalogueGetHardRemovalCardsInput input) throws Exception;

    ArchiveCardPayload archiveCard(ArchiveCardInput input) throws Exception;

    PublishCardPayload publishCard(PublishCardInput input) throws Exception;

    SetUserAttributePayload setUserAttribute(SetUserAttributeInput input) throws Exception;

    GetUserAttributePayload getUserAttribute(GetUserAttributeInput input) throws Exception;

    ClusteredGamesUpdateGameAndUsersPayload clusteredGamesUpdateGameAndUsers(ClusteredGamesUpdateGameAndUsersInput input) throws Exception;

    GetClassesPayload getClasses(GetClassesInput input) throws Exception;

    GetCollectionCardsPayload getCollectionCards(GetCollectionCardsInput input) throws Exception;

    SetCardsInDeckPayload setCardsInDeck(SetCardsInDeckInput input) throws Exception;

    CreateDeckWithCardsPayload createDeckWithCards(CreateDeckWithCardsInput input) throws Exception;

    CardCatalogueFormatsPayload cardCatalogueFormats(CardCatalogueFormatsInput input) throws Exception;

    CardCatalogueGetClassCardsPayload cardCatalogueGetClassCards(CardCatalogueGetClassCardsInput input) throws Exception;

    CardCatalogueGetBaseClassesPayload cardCatalogueGetBaseClasses(CardCatalogueGetBaseClassesInput input) throws Exception;

    CardCatalogueGetCardByIdPayload cardCatalogueGetCardById(CardCatalogueGetCardByIdInput input) throws Exception;

    CardCatalogueGetCardByNamePayload cardCatalogueGetCardByName(CardCatalogueGetCardByNameInput input) throws Exception;

    CardCatalogueGetFormatPayload cardCatalogueGetFormat(CardCatalogueGetFormatInput input) throws Exception;

    CardCatalogueGetHeroCardPayload cardCatalogueGetHeroCard(CardCatalogueGetHeroCardInput input) throws Exception;

    CardCatalogueGetCardByNameAndClassPayload cardCatalogueGetCardByNameAndClass(CardCatalogueGetCardByNameAndClassInput input) throws Exception;

    PublishGitCardPayload publishGitCard(PublishGitCardInput input) throws Exception;

    SaveCardPayload saveCard(SaveCardInput input) throws Exception;

    CardCatalogueQueryPayload cardCatalogueQuery(CardCatalogueQueryInput input) throws Exception;

    SaveGeneratedArtPayload saveGeneratedArt(SaveGeneratedArtInput input) throws Exception;

    /**
     * Creates a single `BannedDraftCard`.
     */
    CreateBannedDraftCardPayload createBannedDraftCard(CreateBannedDraftCardInput input) throws Exception;

    /**
     * Creates a single `BotUser`.
     */
    CreateBotUserPayload createBotUser(CreateBotUserInput input) throws Exception;

    /**
     * Creates a single `HardRemovalCard`.
     */
    CreateHardRemovalCardPayload createHardRemovalCard(CreateHardRemovalCardInput input) throws Exception;

    /**
     * Creates a single `PublishedCard`.
     */
    CreatePublishedCardPayload createPublishedCard(CreatePublishedCardInput input) throws Exception;

    /**
     * Creates a single `Guest`.
     */
    CreateGuestPayload createGuest(CreateGuestInput input) throws Exception;

    /**
     * Creates a single `DeckShare`.
     */
    CreateDeckSharePayload createDeckShare(CreateDeckShareInput input) throws Exception;

    /**
     * Creates a single `CardsInDeck`.
     */
    CreateCardsInDeckPayload createCardsInDeck(CreateCardsInDeckInput input) throws Exception;

    /**
     * Creates a single `DeckPlayerAttributeTuple`.
     */
    CreateDeckPlayerAttributeTuplePayload createDeckPlayerAttributeTuple(CreateDeckPlayerAttributeTupleInput input) throws Exception;

    /**
     * Creates a single `Friend`.
     */
    CreateFriendPayload createFriend(CreateFriendInput input) throws Exception;

    /**
     * Creates a single `MatchmakingTicket`.
     */
    CreateMatchmakingTicketPayload createMatchmakingTicket(CreateMatchmakingTicketInput input) throws Exception;

    /**
     * Creates a single `Deck`.
     */
    CreateDeckPayload createDeck(CreateDeckInput input) throws Exception;

    /**
     * Creates a single `MatchmakingQueue`.
     */
    CreateMatchmakingQueuePayload createMatchmakingQueue(CreateMatchmakingQueueInput input) throws Exception;

    /**
     * Creates a single `Card`.
     */
    CreateCardPayload createCard(CreateCardInput input) throws Exception;

    /**
     * Creates a single `GeneratedArt`.
     */
    CreateGeneratedArtPayload createGeneratedArt(CreateGeneratedArtInput input) throws Exception;

    /**
     * Creates a single `Game`.
     */
    CreateGamePayload createGame(CreateGameInput input) throws Exception;

    /**
     * Creates a single `GameUser`.
     */
    CreateGameUserPayload createGameUser(CreateGameUserInput input) throws Exception;

    /**
     * Updates a single `BannedDraftCard` using its globally unique id and a patch.
     */
    UpdateBannedDraftCardPayload updateBannedDraftCard(UpdateBannedDraftCardInput input) throws Exception;

    /**
     * Updates a single `BannedDraftCard` using a unique key and a patch.
     */
    UpdateBannedDraftCardPayload updateBannedDraftCardByCardId(UpdateBannedDraftCardByCardIdInput input) throws Exception;

    /**
     * Updates a single `BotUser` using its globally unique id and a patch.
     */
    UpdateBotUserPayload updateBotUser(UpdateBotUserInput input) throws Exception;

    /**
     * Updates a single `BotUser` using a unique key and a patch.
     */
    UpdateBotUserPayload updateBotUserById(UpdateBotUserByIdInput input) throws Exception;

    /**
     * Updates a single `HardRemovalCard` using its globally unique id and a patch.
     */
    UpdateHardRemovalCardPayload updateHardRemovalCard(UpdateHardRemovalCardInput input) throws Exception;

    /**
     * Updates a single `HardRemovalCard` using a unique key and a patch.
     */
    UpdateHardRemovalCardPayload updateHardRemovalCardByCardId(UpdateHardRemovalCardByCardIdInput input) throws Exception;

    /**
     * Updates a single `PublishedCard` using its globally unique id and a patch.
     */
    UpdatePublishedCardPayload updatePublishedCard(UpdatePublishedCardInput input) throws Exception;

    /**
     * Updates a single `PublishedCard` using a unique key and a patch.
     */
    UpdatePublishedCardPayload updatePublishedCardById(UpdatePublishedCardByIdInput input) throws Exception;

    /**
     * Updates a single `Guest` using its globally unique id and a patch.
     */
    UpdateGuestPayload updateGuest(UpdateGuestInput input) throws Exception;

    /**
     * Updates a single `Guest` using a unique key and a patch.
     */
    UpdateGuestPayload updateGuestById(UpdateGuestByIdInput input) throws Exception;

    /**
     * Updates a single `DeckShare` using its globally unique id and a patch.
     */
    UpdateDeckSharePayload updateDeckShare(UpdateDeckShareInput input) throws Exception;

    /**
     * Updates a single `DeckShare` using a unique key and a patch.
     */
    UpdateDeckSharePayload updateDeckShareByDeckIdAndShareRecipientId(UpdateDeckShareByDeckIdAndShareRecipientIdInput input) throws Exception;

    /**
     * Updates a single `CardsInDeck` using its globally unique id and a patch.
     */
    UpdateCardsInDeckPayload updateCardsInDeck(UpdateCardsInDeckInput input) throws Exception;

    /**
     * Updates a single `CardsInDeck` using a unique key and a patch.
     */
    UpdateCardsInDeckPayload updateCardsInDeckById(UpdateCardsInDeckByIdInput input) throws Exception;

    /**
     * Updates a single `DeckPlayerAttributeTuple` using its globally unique id and a patch.
     */
    UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTuple(UpdateDeckPlayerAttributeTupleInput input) throws Exception;

    /**
     * Updates a single `DeckPlayerAttributeTuple` using a unique key and a patch.
     */
    UpdateDeckPlayerAttributeTuplePayload updateDeckPlayerAttributeTupleById(UpdateDeckPlayerAttributeTupleByIdInput input) throws Exception;

    /**
     * Updates a single `Friend` using its globally unique id and a patch.
     */
    UpdateFriendPayload updateFriend(UpdateFriendInput input) throws Exception;

    /**
     * Updates a single `Friend` using a unique key and a patch.
     */
    UpdateFriendPayload updateFriendByIdAndFriend(UpdateFriendByIdAndFriendInput input) throws Exception;

    /**
     * Updates a single `MatchmakingTicket` using its globally unique id and a patch.
     */
    UpdateMatchmakingTicketPayload updateMatchmakingTicket(UpdateMatchmakingTicketInput input) throws Exception;

    /**
     * Updates a single `MatchmakingTicket` using a unique key and a patch.
     */
    UpdateMatchmakingTicketPayload updateMatchmakingTicketByUserId(UpdateMatchmakingTicketByUserIdInput input) throws Exception;

    /**
     * Updates a single `Deck` using its globally unique id and a patch.
     */
    UpdateDeckPayload updateDeck(UpdateDeckInput input) throws Exception;

    /**
     * Updates a single `Deck` using a unique key and a patch.
     */
    UpdateDeckPayload updateDeckById(UpdateDeckByIdInput input) throws Exception;

    /**
     * Updates a single `MatchmakingQueue` using its globally unique id and a patch.
     */
    UpdateMatchmakingQueuePayload updateMatchmakingQueue(UpdateMatchmakingQueueInput input) throws Exception;

    /**
     * Updates a single `MatchmakingQueue` using a unique key and a patch.
     */
    UpdateMatchmakingQueuePayload updateMatchmakingQueueById(UpdateMatchmakingQueueByIdInput input) throws Exception;

    /**
     * Updates a single `Card` using its globally unique id and a patch.
     */
    UpdateCardPayload updateCard(UpdateCardInput input) throws Exception;

    /**
     * Updates a single `Card` using a unique key and a patch.
     */
    UpdateCardPayload updateCardBySuccession(UpdateCardBySuccessionInput input) throws Exception;

    /**
     * Updates a single `GeneratedArt` using a unique key and a patch.
     */
    UpdateGeneratedArtPayload updateGeneratedArtByHashAndOwner(UpdateGeneratedArtByHashAndOwnerInput input) throws Exception;

    /**
     * Updates a single `Game` using its globally unique id and a patch.
     */
    UpdateGamePayload updateGame(UpdateGameInput input) throws Exception;

    /**
     * Updates a single `Game` using a unique key and a patch.
     */
    UpdateGamePayload updateGameById(UpdateGameByIdInput input) throws Exception;

    /**
     * Updates a single `GameUser` using its globally unique id and a patch.
     */
    UpdateGameUserPayload updateGameUser(UpdateGameUserInput input) throws Exception;

    /**
     * Updates a single `GameUser` using a unique key and a patch.
     */
    UpdateGameUserPayload updateGameUserByGameIdAndUserId(UpdateGameUserByGameIdAndUserIdInput input) throws Exception;

    /**
     * Deletes a single `BannedDraftCard` using its globally unique id.
     */
    DeleteBannedDraftCardPayload deleteBannedDraftCard(DeleteBannedDraftCardInput input) throws Exception;

    /**
     * Deletes a single `BannedDraftCard` using a unique key.
     */
    DeleteBannedDraftCardPayload deleteBannedDraftCardByCardId(DeleteBannedDraftCardByCardIdInput input) throws Exception;

    /**
     * Deletes a single `BotUser` using its globally unique id.
     */
    DeleteBotUserPayload deleteBotUser(DeleteBotUserInput input) throws Exception;

    /**
     * Deletes a single `BotUser` using a unique key.
     */
    DeleteBotUserPayload deleteBotUserById(DeleteBotUserByIdInput input) throws Exception;

    /**
     * Deletes a single `HardRemovalCard` using its globally unique id.
     */
    DeleteHardRemovalCardPayload deleteHardRemovalCard(DeleteHardRemovalCardInput input) throws Exception;

    /**
     * Deletes a single `HardRemovalCard` using a unique key.
     */
    DeleteHardRemovalCardPayload deleteHardRemovalCardByCardId(DeleteHardRemovalCardByCardIdInput input) throws Exception;

    /**
     * Deletes a single `PublishedCard` using its globally unique id.
     */
    DeletePublishedCardPayload deletePublishedCard(DeletePublishedCardInput input) throws Exception;

    /**
     * Deletes a single `PublishedCard` using a unique key.
     */
    DeletePublishedCardPayload deletePublishedCardById(DeletePublishedCardByIdInput input) throws Exception;

    /**
     * Deletes a single `Guest` using its globally unique id.
     */
    DeleteGuestPayload deleteGuest(DeleteGuestInput input) throws Exception;

    /**
     * Deletes a single `Guest` using a unique key.
     */
    DeleteGuestPayload deleteGuestById(DeleteGuestByIdInput input) throws Exception;

    /**
     * Deletes a single `DeckShare` using its globally unique id.
     */
    DeleteDeckSharePayload deleteDeckShare(DeleteDeckShareInput input) throws Exception;

    /**
     * Deletes a single `DeckShare` using a unique key.
     */
    DeleteDeckSharePayload deleteDeckShareByDeckIdAndShareRecipientId(DeleteDeckShareByDeckIdAndShareRecipientIdInput input) throws Exception;

    /**
     * Deletes a single `CardsInDeck` using its globally unique id.
     */
    DeleteCardsInDeckPayload deleteCardsInDeck(DeleteCardsInDeckInput input) throws Exception;

    /**
     * Deletes a single `CardsInDeck` using a unique key.
     */
    DeleteCardsInDeckPayload deleteCardsInDeckById(DeleteCardsInDeckByIdInput input) throws Exception;

    /**
     * Deletes a single `DeckPlayerAttributeTuple` using its globally unique id.
     */
    DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTuple(DeleteDeckPlayerAttributeTupleInput input) throws Exception;

    /**
     * Deletes a single `DeckPlayerAttributeTuple` using a unique key.
     */
    DeleteDeckPlayerAttributeTuplePayload deleteDeckPlayerAttributeTupleById(DeleteDeckPlayerAttributeTupleByIdInput input) throws Exception;

    /**
     * Deletes a single `Friend` using its globally unique id.
     */
    DeleteFriendPayload deleteFriend(DeleteFriendInput input) throws Exception;

    /**
     * Deletes a single `Friend` using a unique key.
     */
    DeleteFriendPayload deleteFriendByIdAndFriend(DeleteFriendByIdAndFriendInput input) throws Exception;

    /**
     * Deletes a single `MatchmakingTicket` using its globally unique id.
     */
    DeleteMatchmakingTicketPayload deleteMatchmakingTicket(DeleteMatchmakingTicketInput input) throws Exception;

    /**
     * Deletes a single `MatchmakingTicket` using a unique key.
     */
    DeleteMatchmakingTicketPayload deleteMatchmakingTicketByUserId(DeleteMatchmakingTicketByUserIdInput input) throws Exception;

    /**
     * Deletes a single `Deck` using its globally unique id.
     */
    DeleteDeckPayload deleteDeck(DeleteDeckInput input) throws Exception;

    /**
     * Deletes a single `Deck` using a unique key.
     */
    DeleteDeckPayload deleteDeckById(DeleteDeckByIdInput input) throws Exception;

    /**
     * Deletes a single `MatchmakingQueue` using its globally unique id.
     */
    DeleteMatchmakingQueuePayload deleteMatchmakingQueue(DeleteMatchmakingQueueInput input) throws Exception;

    /**
     * Deletes a single `MatchmakingQueue` using a unique key.
     */
    DeleteMatchmakingQueuePayload deleteMatchmakingQueueById(DeleteMatchmakingQueueByIdInput input) throws Exception;

    /**
     * Deletes a single `Card` using its globally unique id.
     */
    DeleteCardPayload deleteCard(DeleteCardInput input) throws Exception;

    /**
     * Deletes a single `Card` using a unique key.
     */
    DeleteCardPayload deleteCardBySuccession(DeleteCardBySuccessionInput input) throws Exception;

    /**
     * Deletes a single `GeneratedArt` using a unique key.
     */
    DeleteGeneratedArtPayload deleteGeneratedArtByHashAndOwner(DeleteGeneratedArtByHashAndOwnerInput input) throws Exception;

    /**
     * Deletes a single `Game` using its globally unique id.
     */
    DeleteGamePayload deleteGame(DeleteGameInput input) throws Exception;

    /**
     * Deletes a single `Game` using a unique key.
     */
    DeleteGamePayload deleteGameById(DeleteGameByIdInput input) throws Exception;

    /**
     * Deletes a single `GameUser` using its globally unique id.
     */
    DeleteGameUserPayload deleteGameUser(DeleteGameUserInput input) throws Exception;

    /**
     * Deletes a single `GameUser` using a unique key.
     */
    DeleteGameUserPayload deleteGameUserByGameIdAndUserId(DeleteGameUserByGameIdAndUserIdInput input) throws Exception;

}
