package com.hiddenswitch.framework.graphql;


/**
 * The root query type which gives access points into the data universe.
 */
public interface QueryResolver {

    /**
     * Exposes the root query type nested one level down. This is helpful for Relay 1
which can only query top level fields if they are in a particular form.
     */
    Query query() throws Exception;

    /**
     * The root query type must be a `Node` to work well with Relay 1 mutations. This just resolves to `query`.
     */
    String nodeId() throws Exception;

    /**
     * Fetches an object given its globally unique `ID`.
     */
    Node node(String nodeId) throws Exception;

    /**
     * Get a single `BannedDraftCard`.
     */
    BannedDraftCard bannedDraftCardByCardId(String cardId) throws Exception;

    /**
     * Get a single `BotUser`.
     */
    BotUser botUserById(String id) throws Exception;

    /**
     * Get a single `HardRemovalCard`.
     */
    HardRemovalCard hardRemovalCardByCardId(String cardId) throws Exception;

    /**
     * Get a single `PublishedCard`.
     */
    PublishedCard publishedCardById(String id) throws Exception;

    /**
     * Get a single `Guest`.
     */
    Guest guestById(String id) throws Exception;

    /**
     * Get a single `DeckShare`.
     */
    DeckShare deckShareByDeckIdAndShareRecipientId(String deckId, String shareRecipientId) throws Exception;

    /**
     * Get a single `CardsInDeck`.
     */
    CardsInDeck cardsInDeckById(String id) throws Exception;

    /**
     * Get a single `DeckPlayerAttributeTuple`.
     */
    DeckPlayerAttributeTuple deckPlayerAttributeTupleById(String id) throws Exception;

    /**
     * Get a single `Friend`.
     */
    Friend friendByIdAndFriend(String id, String friend) throws Exception;

    /**
     * Get a single `MatchmakingTicket`.
     */
    MatchmakingTicket matchmakingTicketByUserId(String userId) throws Exception;

    /**
     * Get a single `Deck`.
     */
    Deck deckById(String id) throws Exception;

    /**
     * Get a single `MatchmakingQueue`.
     */
    MatchmakingQueue matchmakingQueueById(String id) throws Exception;

    /**
     * Get a single `Card`.
     */
    Card cardBySuccession(String succession) throws Exception;

    /**
     * Get a single `GeneratedArt`.
     */
    GeneratedArt generatedArtByHashAndOwner(String hash, String owner) throws Exception;

    /**
     * Get a single `Game`.
     */
    Game gameById(String id) throws Exception;

    /**
     * Get a single `GameUser`.
     */
    GameUser gameUserByGameIdAndUserId(String gameId, String userId) throws Exception;

    String getUserId() throws Exception;

    Boolean canSeeDeck(String userId, DeckInput deck) throws Exception;

    Card getLatestCard(String cardId, Boolean published) throws Exception;

    /**
     * Reads a single `BannedDraftCard` using its globally unique `ID`.
     */
    BannedDraftCard bannedDraftCard(String nodeId) throws Exception;

    /**
     * Reads a single `BotUser` using its globally unique `ID`.
     */
    BotUser botUser(String nodeId) throws Exception;

    /**
     * Reads a single `HardRemovalCard` using its globally unique `ID`.
     */
    HardRemovalCard hardRemovalCard(String nodeId) throws Exception;

    /**
     * Reads a single `PublishedCard` using its globally unique `ID`.
     */
    PublishedCard publishedCard(String nodeId) throws Exception;

    /**
     * Reads a single `Guest` using its globally unique `ID`.
     */
    Guest guest(String nodeId) throws Exception;

    /**
     * Reads a single `DeckShare` using its globally unique `ID`.
     */
    DeckShare deckShare(String nodeId) throws Exception;

    /**
     * Reads a single `CardsInDeck` using its globally unique `ID`.
     */
    CardsInDeck cardsInDeck(String nodeId) throws Exception;

    /**
     * Reads a single `DeckPlayerAttributeTuple` using its globally unique `ID`.
     */
    DeckPlayerAttributeTuple deckPlayerAttributeTuple(String nodeId) throws Exception;

    /**
     * Reads a single `Friend` using its globally unique `ID`.
     */
    Friend friend(String nodeId) throws Exception;

    /**
     * Reads a single `MatchmakingTicket` using its globally unique `ID`.
     */
    MatchmakingTicket matchmakingTicket(String nodeId) throws Exception;

    /**
     * Reads a single `Deck` using its globally unique `ID`.
     */
    Deck deck(String nodeId) throws Exception;

    /**
     * Reads a single `MatchmakingQueue` using its globally unique `ID`.
     */
    MatchmakingQueue matchmakingQueue(String nodeId) throws Exception;

    /**
     * Reads a single `Card` using its globally unique `ID`.
     */
    Card card(String nodeId) throws Exception;

    /**
     * Reads a single `Game` using its globally unique `ID`.
     */
    Game game(String nodeId) throws Exception;

    /**
     * Reads a single `GameUser` using its globally unique `ID`.
     */
    GameUser gameUser(String nodeId) throws Exception;

    /**
     * Reads and enables pagination through a set of `BannedDraftCard`.
     */
    BannedDraftCardsConnection allBannedDraftCards(Integer first, Integer last, Integer offset, String before, String after, java.util.List<BannedDraftCardsOrderBy> orderBy, BannedDraftCardCondition condition, BannedDraftCardFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `BotUser`.
     */
    BotUsersConnection allBotUsers(Integer first, Integer last, Integer offset, String before, String after, java.util.List<BotUsersOrderBy> orderBy, BotUserCondition condition, BotUserFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `HardRemovalCard`.
     */
    HardRemovalCardsConnection allHardRemovalCards(Integer first, Integer last, Integer offset, String before, String after, java.util.List<HardRemovalCardsOrderBy> orderBy, HardRemovalCardCondition condition, HardRemovalCardFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `PublishedCard`.
     */
    PublishedCardsConnection allPublishedCards(Integer first, Integer last, Integer offset, String before, String after, java.util.List<PublishedCardsOrderBy> orderBy, PublishedCardCondition condition, PublishedCardFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `Guest`.
     */
    GuestsConnection allGuests(Integer first, Integer last, Integer offset, String before, String after, java.util.List<GuestsOrderBy> orderBy, GuestCondition condition, GuestFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `DeckShare`.
     */
    DeckSharesConnection allDeckShares(Integer first, Integer last, Integer offset, String before, String after, java.util.List<DeckSharesOrderBy> orderBy, DeckShareCondition condition, DeckShareFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `CardsInDeck`.
     */
    CardsInDecksConnection allCardsInDecks(Integer first, Integer last, Integer offset, String before, String after, java.util.List<CardsInDecksOrderBy> orderBy, CardsInDeckCondition condition, CardsInDeckFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `DeckPlayerAttributeTuple`.
     */
    DeckPlayerAttributeTuplesConnection allDeckPlayerAttributeTuples(Integer first, Integer last, Integer offset, String before, String after, java.util.List<DeckPlayerAttributeTuplesOrderBy> orderBy, DeckPlayerAttributeTupleCondition condition, DeckPlayerAttributeTupleFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `Class`.
     */
    ClassesConnection allClasses(Integer first, Integer last, Integer offset, String before, String after, java.util.List<ClassesOrderBy> orderBy, ClassCondition condition, ClassFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `Friend`.
     */
    FriendsConnection allFriends(Integer first, Integer last, Integer offset, String before, String after, java.util.List<FriendsOrderBy> orderBy, FriendCondition condition, FriendFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `MatchmakingTicket`.
     */
    MatchmakingTicketsConnection allMatchmakingTickets(Integer first, Integer last, Integer offset, String before, String after, java.util.List<MatchmakingTicketsOrderBy> orderBy, MatchmakingTicketCondition condition, MatchmakingTicketFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `Deck`.
     */
    DecksConnection allDecks(Integer first, Integer last, Integer offset, String before, String after, java.util.List<DecksOrderBy> orderBy, DeckCondition condition, DeckFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `MatchmakingQueue`.
     */
    MatchmakingQueuesConnection allMatchmakingQueues(Integer first, Integer last, Integer offset, String before, String after, java.util.List<MatchmakingQueuesOrderBy> orderBy, MatchmakingQueueCondition condition, MatchmakingQueueFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `Card`.
     */
    CardsConnection allCards(Integer first, Integer last, Integer offset, String before, String after, java.util.List<CardsOrderBy> orderBy, CardCondition condition, CardFilter filter, IncludeArchivedOption includeArchived) throws Exception;

    /**
     * Reads and enables pagination through a set of `CollectionCard`.
     */
    CollectionCardsConnection allCollectionCards(Integer first, Integer last, Integer offset, String before, String after, java.util.List<CollectionCardsOrderBy> orderBy, CollectionCardCondition condition, CollectionCardFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `GeneratedArt`.
     */
    GeneratedArtsConnection allGeneratedArts(Integer first, Integer last, Integer offset, String before, String after, java.util.List<GeneratedArtsOrderBy> orderBy, GeneratedArtCondition condition, GeneratedArtFilter filter, IncludeArchivedOption includeArchived) throws Exception;

    /**
     * Reads and enables pagination through a set of `Game`.
     */
    GamesConnection allGames(Integer first, Integer last, Integer offset, String before, String after, java.util.List<GamesOrderBy> orderBy, GameCondition condition, GameFilter filter) throws Exception;

    /**
     * Reads and enables pagination through a set of `GameUser`.
     */
    GameUsersConnection allGameUsers(Integer first, Integer last, Integer offset, String before, String after, java.util.List<GameUsersOrderBy> orderBy, GameUserCondition condition, GameUserFilter filter) throws Exception;

}
