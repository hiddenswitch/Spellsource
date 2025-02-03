package com.hiddenswitch.framework.graphql;


/**
 * The root query type which gives access points into the data universe.
 */
public class Query implements java.io.Serializable, Node {

    private static final long serialVersionUID = 1L;

    private Query query;
    private String nodeId;
    private Node node;
    private BannedDraftCard bannedDraftCardByCardId;
    private BotUser botUserById;
    private HardRemovalCard hardRemovalCardByCardId;
    private PublishedCard publishedCardById;
    private Guest guestById;
    private DeckShare deckShareByDeckIdAndShareRecipientId;
    private CardsInDeck cardsInDeckById;
    private DeckPlayerAttributeTuple deckPlayerAttributeTupleById;
    private Friend friendByIdAndFriend;
    private MatchmakingTicket matchmakingTicketByUserId;
    private Deck deckById;
    private MatchmakingQueue matchmakingQueueById;
    private Card cardBySuccession;
    private GeneratedArt generatedArtByHashAndOwner;
    private Game gameById;
    private GameUser gameUserByGameIdAndUserId;
    private String getUserId;
    private Boolean canSeeDeck;
    private Card getLatestCard;
    private BannedDraftCard bannedDraftCard;
    private BotUser botUser;
    private HardRemovalCard hardRemovalCard;
    private PublishedCard publishedCard;
    private Guest guest;
    private DeckShare deckShare;
    private CardsInDeck cardsInDeck;
    private DeckPlayerAttributeTuple deckPlayerAttributeTuple;
    private Friend friend;
    private MatchmakingTicket matchmakingTicket;
    private Deck deck;
    private MatchmakingQueue matchmakingQueue;
    private Card card;
    private Game game;
    private GameUser gameUser;
    private BannedDraftCardsConnection allBannedDraftCards;
    private BotUsersConnection allBotUsers;
    private HardRemovalCardsConnection allHardRemovalCards;
    private PublishedCardsConnection allPublishedCards;
    private GuestsConnection allGuests;
    private DeckSharesConnection allDeckShares;
    private CardsInDecksConnection allCardsInDecks;
    private DeckPlayerAttributeTuplesConnection allDeckPlayerAttributeTuples;
    private ClassesConnection allClasses;
    private FriendsConnection allFriends;
    private MatchmakingTicketsConnection allMatchmakingTickets;
    private DecksConnection allDecks;
    private MatchmakingQueuesConnection allMatchmakingQueues;
    private CardsConnection allCards;
    private CollectionCardsConnection allCollectionCards;
    private GeneratedArtsConnection allGeneratedArts;
    private GamesConnection allGames;
    private GameUsersConnection allGameUsers;

    public Query() {
    }

    public Query(Query query, String nodeId, Node node, BannedDraftCard bannedDraftCardByCardId, BotUser botUserById, HardRemovalCard hardRemovalCardByCardId, PublishedCard publishedCardById, Guest guestById, DeckShare deckShareByDeckIdAndShareRecipientId, CardsInDeck cardsInDeckById, DeckPlayerAttributeTuple deckPlayerAttributeTupleById, Friend friendByIdAndFriend, MatchmakingTicket matchmakingTicketByUserId, Deck deckById, MatchmakingQueue matchmakingQueueById, Card cardBySuccession, GeneratedArt generatedArtByHashAndOwner, Game gameById, GameUser gameUserByGameIdAndUserId, String getUserId, Boolean canSeeDeck, Card getLatestCard, BannedDraftCard bannedDraftCard, BotUser botUser, HardRemovalCard hardRemovalCard, PublishedCard publishedCard, Guest guest, DeckShare deckShare, CardsInDeck cardsInDeck, DeckPlayerAttributeTuple deckPlayerAttributeTuple, Friend friend, MatchmakingTicket matchmakingTicket, Deck deck, MatchmakingQueue matchmakingQueue, Card card, Game game, GameUser gameUser, BannedDraftCardsConnection allBannedDraftCards, BotUsersConnection allBotUsers, HardRemovalCardsConnection allHardRemovalCards, PublishedCardsConnection allPublishedCards, GuestsConnection allGuests, DeckSharesConnection allDeckShares, CardsInDecksConnection allCardsInDecks, DeckPlayerAttributeTuplesConnection allDeckPlayerAttributeTuples, ClassesConnection allClasses, FriendsConnection allFriends, MatchmakingTicketsConnection allMatchmakingTickets, DecksConnection allDecks, MatchmakingQueuesConnection allMatchmakingQueues, CardsConnection allCards, CollectionCardsConnection allCollectionCards, GeneratedArtsConnection allGeneratedArts, GamesConnection allGames, GameUsersConnection allGameUsers) {
        this.query = query;
        this.nodeId = nodeId;
        this.node = node;
        this.bannedDraftCardByCardId = bannedDraftCardByCardId;
        this.botUserById = botUserById;
        this.hardRemovalCardByCardId = hardRemovalCardByCardId;
        this.publishedCardById = publishedCardById;
        this.guestById = guestById;
        this.deckShareByDeckIdAndShareRecipientId = deckShareByDeckIdAndShareRecipientId;
        this.cardsInDeckById = cardsInDeckById;
        this.deckPlayerAttributeTupleById = deckPlayerAttributeTupleById;
        this.friendByIdAndFriend = friendByIdAndFriend;
        this.matchmakingTicketByUserId = matchmakingTicketByUserId;
        this.deckById = deckById;
        this.matchmakingQueueById = matchmakingQueueById;
        this.cardBySuccession = cardBySuccession;
        this.generatedArtByHashAndOwner = generatedArtByHashAndOwner;
        this.gameById = gameById;
        this.gameUserByGameIdAndUserId = gameUserByGameIdAndUserId;
        this.getUserId = getUserId;
        this.canSeeDeck = canSeeDeck;
        this.getLatestCard = getLatestCard;
        this.bannedDraftCard = bannedDraftCard;
        this.botUser = botUser;
        this.hardRemovalCard = hardRemovalCard;
        this.publishedCard = publishedCard;
        this.guest = guest;
        this.deckShare = deckShare;
        this.cardsInDeck = cardsInDeck;
        this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
        this.friend = friend;
        this.matchmakingTicket = matchmakingTicket;
        this.deck = deck;
        this.matchmakingQueue = matchmakingQueue;
        this.card = card;
        this.game = game;
        this.gameUser = gameUser;
        this.allBannedDraftCards = allBannedDraftCards;
        this.allBotUsers = allBotUsers;
        this.allHardRemovalCards = allHardRemovalCards;
        this.allPublishedCards = allPublishedCards;
        this.allGuests = allGuests;
        this.allDeckShares = allDeckShares;
        this.allCardsInDecks = allCardsInDecks;
        this.allDeckPlayerAttributeTuples = allDeckPlayerAttributeTuples;
        this.allClasses = allClasses;
        this.allFriends = allFriends;
        this.allMatchmakingTickets = allMatchmakingTickets;
        this.allDecks = allDecks;
        this.allMatchmakingQueues = allMatchmakingQueues;
        this.allCards = allCards;
        this.allCollectionCards = allCollectionCards;
        this.allGeneratedArts = allGeneratedArts;
        this.allGames = allGames;
        this.allGameUsers = allGameUsers;
    }

    /**
     * Exposes the root query type nested one level down. This is helpful for Relay 1
which can only query top level fields if they are in a particular form.
     */
    public Query getQuery() {
        return query;
    }
    /**
     * Exposes the root query type nested one level down. This is helpful for Relay 1
which can only query top level fields if they are in a particular form.
     */
    public void setQuery(Query query) {
        this.query = query;
    }

    /**
     * The root query type must be a `Node` to work well with Relay 1 mutations. This just resolves to `query`.
     */
    public String getNodeId() {
        return nodeId;
    }
    /**
     * The root query type must be a `Node` to work well with Relay 1 mutations. This just resolves to `query`.
     */
    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    /**
     * Fetches an object given its globally unique `ID`.
     */
    public Node getNode() {
        return node;
    }
    /**
     * Fetches an object given its globally unique `ID`.
     */
    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * Get a single `BannedDraftCard`.
     */
    public BannedDraftCard getBannedDraftCardByCardId() {
        return bannedDraftCardByCardId;
    }
    /**
     * Get a single `BannedDraftCard`.
     */
    public void setBannedDraftCardByCardId(BannedDraftCard bannedDraftCardByCardId) {
        this.bannedDraftCardByCardId = bannedDraftCardByCardId;
    }

    /**
     * Get a single `BotUser`.
     */
    public BotUser getBotUserById() {
        return botUserById;
    }
    /**
     * Get a single `BotUser`.
     */
    public void setBotUserById(BotUser botUserById) {
        this.botUserById = botUserById;
    }

    /**
     * Get a single `HardRemovalCard`.
     */
    public HardRemovalCard getHardRemovalCardByCardId() {
        return hardRemovalCardByCardId;
    }
    /**
     * Get a single `HardRemovalCard`.
     */
    public void setHardRemovalCardByCardId(HardRemovalCard hardRemovalCardByCardId) {
        this.hardRemovalCardByCardId = hardRemovalCardByCardId;
    }

    /**
     * Get a single `PublishedCard`.
     */
    public PublishedCard getPublishedCardById() {
        return publishedCardById;
    }
    /**
     * Get a single `PublishedCard`.
     */
    public void setPublishedCardById(PublishedCard publishedCardById) {
        this.publishedCardById = publishedCardById;
    }

    /**
     * Get a single `Guest`.
     */
    public Guest getGuestById() {
        return guestById;
    }
    /**
     * Get a single `Guest`.
     */
    public void setGuestById(Guest guestById) {
        this.guestById = guestById;
    }

    /**
     * Get a single `DeckShare`.
     */
    public DeckShare getDeckShareByDeckIdAndShareRecipientId() {
        return deckShareByDeckIdAndShareRecipientId;
    }
    /**
     * Get a single `DeckShare`.
     */
    public void setDeckShareByDeckIdAndShareRecipientId(DeckShare deckShareByDeckIdAndShareRecipientId) {
        this.deckShareByDeckIdAndShareRecipientId = deckShareByDeckIdAndShareRecipientId;
    }

    /**
     * Get a single `CardsInDeck`.
     */
    public CardsInDeck getCardsInDeckById() {
        return cardsInDeckById;
    }
    /**
     * Get a single `CardsInDeck`.
     */
    public void setCardsInDeckById(CardsInDeck cardsInDeckById) {
        this.cardsInDeckById = cardsInDeckById;
    }

    /**
     * Get a single `DeckPlayerAttributeTuple`.
     */
    public DeckPlayerAttributeTuple getDeckPlayerAttributeTupleById() {
        return deckPlayerAttributeTupleById;
    }
    /**
     * Get a single `DeckPlayerAttributeTuple`.
     */
    public void setDeckPlayerAttributeTupleById(DeckPlayerAttributeTuple deckPlayerAttributeTupleById) {
        this.deckPlayerAttributeTupleById = deckPlayerAttributeTupleById;
    }

    /**
     * Get a single `Friend`.
     */
    public Friend getFriendByIdAndFriend() {
        return friendByIdAndFriend;
    }
    /**
     * Get a single `Friend`.
     */
    public void setFriendByIdAndFriend(Friend friendByIdAndFriend) {
        this.friendByIdAndFriend = friendByIdAndFriend;
    }

    /**
     * Get a single `MatchmakingTicket`.
     */
    public MatchmakingTicket getMatchmakingTicketByUserId() {
        return matchmakingTicketByUserId;
    }
    /**
     * Get a single `MatchmakingTicket`.
     */
    public void setMatchmakingTicketByUserId(MatchmakingTicket matchmakingTicketByUserId) {
        this.matchmakingTicketByUserId = matchmakingTicketByUserId;
    }

    /**
     * Get a single `Deck`.
     */
    public Deck getDeckById() {
        return deckById;
    }
    /**
     * Get a single `Deck`.
     */
    public void setDeckById(Deck deckById) {
        this.deckById = deckById;
    }

    /**
     * Get a single `MatchmakingQueue`.
     */
    public MatchmakingQueue getMatchmakingQueueById() {
        return matchmakingQueueById;
    }
    /**
     * Get a single `MatchmakingQueue`.
     */
    public void setMatchmakingQueueById(MatchmakingQueue matchmakingQueueById) {
        this.matchmakingQueueById = matchmakingQueueById;
    }

    /**
     * Get a single `Card`.
     */
    public Card getCardBySuccession() {
        return cardBySuccession;
    }
    /**
     * Get a single `Card`.
     */
    public void setCardBySuccession(Card cardBySuccession) {
        this.cardBySuccession = cardBySuccession;
    }

    /**
     * Get a single `GeneratedArt`.
     */
    public GeneratedArt getGeneratedArtByHashAndOwner() {
        return generatedArtByHashAndOwner;
    }
    /**
     * Get a single `GeneratedArt`.
     */
    public void setGeneratedArtByHashAndOwner(GeneratedArt generatedArtByHashAndOwner) {
        this.generatedArtByHashAndOwner = generatedArtByHashAndOwner;
    }

    /**
     * Get a single `Game`.
     */
    public Game getGameById() {
        return gameById;
    }
    /**
     * Get a single `Game`.
     */
    public void setGameById(Game gameById) {
        this.gameById = gameById;
    }

    /**
     * Get a single `GameUser`.
     */
    public GameUser getGameUserByGameIdAndUserId() {
        return gameUserByGameIdAndUserId;
    }
    /**
     * Get a single `GameUser`.
     */
    public void setGameUserByGameIdAndUserId(GameUser gameUserByGameIdAndUserId) {
        this.gameUserByGameIdAndUserId = gameUserByGameIdAndUserId;
    }

    public String getGetUserId() {
        return getUserId;
    }
    public void setGetUserId(String getUserId) {
        this.getUserId = getUserId;
    }

    public Boolean getCanSeeDeck() {
        return canSeeDeck;
    }
    public void setCanSeeDeck(Boolean canSeeDeck) {
        this.canSeeDeck = canSeeDeck;
    }

    public Card getGetLatestCard() {
        return getLatestCard;
    }
    public void setGetLatestCard(Card getLatestCard) {
        this.getLatestCard = getLatestCard;
    }

    /**
     * Reads a single `BannedDraftCard` using its globally unique `ID`.
     */
    public BannedDraftCard getBannedDraftCard() {
        return bannedDraftCard;
    }
    /**
     * Reads a single `BannedDraftCard` using its globally unique `ID`.
     */
    public void setBannedDraftCard(BannedDraftCard bannedDraftCard) {
        this.bannedDraftCard = bannedDraftCard;
    }

    /**
     * Reads a single `BotUser` using its globally unique `ID`.
     */
    public BotUser getBotUser() {
        return botUser;
    }
    /**
     * Reads a single `BotUser` using its globally unique `ID`.
     */
    public void setBotUser(BotUser botUser) {
        this.botUser = botUser;
    }

    /**
     * Reads a single `HardRemovalCard` using its globally unique `ID`.
     */
    public HardRemovalCard getHardRemovalCard() {
        return hardRemovalCard;
    }
    /**
     * Reads a single `HardRemovalCard` using its globally unique `ID`.
     */
    public void setHardRemovalCard(HardRemovalCard hardRemovalCard) {
        this.hardRemovalCard = hardRemovalCard;
    }

    /**
     * Reads a single `PublishedCard` using its globally unique `ID`.
     */
    public PublishedCard getPublishedCard() {
        return publishedCard;
    }
    /**
     * Reads a single `PublishedCard` using its globally unique `ID`.
     */
    public void setPublishedCard(PublishedCard publishedCard) {
        this.publishedCard = publishedCard;
    }

    /**
     * Reads a single `Guest` using its globally unique `ID`.
     */
    public Guest getGuest() {
        return guest;
    }
    /**
     * Reads a single `Guest` using its globally unique `ID`.
     */
    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    /**
     * Reads a single `DeckShare` using its globally unique `ID`.
     */
    public DeckShare getDeckShare() {
        return deckShare;
    }
    /**
     * Reads a single `DeckShare` using its globally unique `ID`.
     */
    public void setDeckShare(DeckShare deckShare) {
        this.deckShare = deckShare;
    }

    /**
     * Reads a single `CardsInDeck` using its globally unique `ID`.
     */
    public CardsInDeck getCardsInDeck() {
        return cardsInDeck;
    }
    /**
     * Reads a single `CardsInDeck` using its globally unique `ID`.
     */
    public void setCardsInDeck(CardsInDeck cardsInDeck) {
        this.cardsInDeck = cardsInDeck;
    }

    /**
     * Reads a single `DeckPlayerAttributeTuple` using its globally unique `ID`.
     */
    public DeckPlayerAttributeTuple getDeckPlayerAttributeTuple() {
        return deckPlayerAttributeTuple;
    }
    /**
     * Reads a single `DeckPlayerAttributeTuple` using its globally unique `ID`.
     */
    public void setDeckPlayerAttributeTuple(DeckPlayerAttributeTuple deckPlayerAttributeTuple) {
        this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
    }

    /**
     * Reads a single `Friend` using its globally unique `ID`.
     */
    public Friend getFriend() {
        return friend;
    }
    /**
     * Reads a single `Friend` using its globally unique `ID`.
     */
    public void setFriend(Friend friend) {
        this.friend = friend;
    }

    /**
     * Reads a single `MatchmakingTicket` using its globally unique `ID`.
     */
    public MatchmakingTicket getMatchmakingTicket() {
        return matchmakingTicket;
    }
    /**
     * Reads a single `MatchmakingTicket` using its globally unique `ID`.
     */
    public void setMatchmakingTicket(MatchmakingTicket matchmakingTicket) {
        this.matchmakingTicket = matchmakingTicket;
    }

    /**
     * Reads a single `Deck` using its globally unique `ID`.
     */
    public Deck getDeck() {
        return deck;
    }
    /**
     * Reads a single `Deck` using its globally unique `ID`.
     */
    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    /**
     * Reads a single `MatchmakingQueue` using its globally unique `ID`.
     */
    public MatchmakingQueue getMatchmakingQueue() {
        return matchmakingQueue;
    }
    /**
     * Reads a single `MatchmakingQueue` using its globally unique `ID`.
     */
    public void setMatchmakingQueue(MatchmakingQueue matchmakingQueue) {
        this.matchmakingQueue = matchmakingQueue;
    }

    /**
     * Reads a single `Card` using its globally unique `ID`.
     */
    public Card getCard() {
        return card;
    }
    /**
     * Reads a single `Card` using its globally unique `ID`.
     */
    public void setCard(Card card) {
        this.card = card;
    }

    /**
     * Reads a single `Game` using its globally unique `ID`.
     */
    public Game getGame() {
        return game;
    }
    /**
     * Reads a single `Game` using its globally unique `ID`.
     */
    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Reads a single `GameUser` using its globally unique `ID`.
     */
    public GameUser getGameUser() {
        return gameUser;
    }
    /**
     * Reads a single `GameUser` using its globally unique `ID`.
     */
    public void setGameUser(GameUser gameUser) {
        this.gameUser = gameUser;
    }

    /**
     * Reads and enables pagination through a set of `BannedDraftCard`.
     */
    public BannedDraftCardsConnection getAllBannedDraftCards() {
        return allBannedDraftCards;
    }
    /**
     * Reads and enables pagination through a set of `BannedDraftCard`.
     */
    public void setAllBannedDraftCards(BannedDraftCardsConnection allBannedDraftCards) {
        this.allBannedDraftCards = allBannedDraftCards;
    }

    /**
     * Reads and enables pagination through a set of `BotUser`.
     */
    public BotUsersConnection getAllBotUsers() {
        return allBotUsers;
    }
    /**
     * Reads and enables pagination through a set of `BotUser`.
     */
    public void setAllBotUsers(BotUsersConnection allBotUsers) {
        this.allBotUsers = allBotUsers;
    }

    /**
     * Reads and enables pagination through a set of `HardRemovalCard`.
     */
    public HardRemovalCardsConnection getAllHardRemovalCards() {
        return allHardRemovalCards;
    }
    /**
     * Reads and enables pagination through a set of `HardRemovalCard`.
     */
    public void setAllHardRemovalCards(HardRemovalCardsConnection allHardRemovalCards) {
        this.allHardRemovalCards = allHardRemovalCards;
    }

    /**
     * Reads and enables pagination through a set of `PublishedCard`.
     */
    public PublishedCardsConnection getAllPublishedCards() {
        return allPublishedCards;
    }
    /**
     * Reads and enables pagination through a set of `PublishedCard`.
     */
    public void setAllPublishedCards(PublishedCardsConnection allPublishedCards) {
        this.allPublishedCards = allPublishedCards;
    }

    /**
     * Reads and enables pagination through a set of `Guest`.
     */
    public GuestsConnection getAllGuests() {
        return allGuests;
    }
    /**
     * Reads and enables pagination through a set of `Guest`.
     */
    public void setAllGuests(GuestsConnection allGuests) {
        this.allGuests = allGuests;
    }

    /**
     * Reads and enables pagination through a set of `DeckShare`.
     */
    public DeckSharesConnection getAllDeckShares() {
        return allDeckShares;
    }
    /**
     * Reads and enables pagination through a set of `DeckShare`.
     */
    public void setAllDeckShares(DeckSharesConnection allDeckShares) {
        this.allDeckShares = allDeckShares;
    }

    /**
     * Reads and enables pagination through a set of `CardsInDeck`.
     */
    public CardsInDecksConnection getAllCardsInDecks() {
        return allCardsInDecks;
    }
    /**
     * Reads and enables pagination through a set of `CardsInDeck`.
     */
    public void setAllCardsInDecks(CardsInDecksConnection allCardsInDecks) {
        this.allCardsInDecks = allCardsInDecks;
    }

    /**
     * Reads and enables pagination through a set of `DeckPlayerAttributeTuple`.
     */
    public DeckPlayerAttributeTuplesConnection getAllDeckPlayerAttributeTuples() {
        return allDeckPlayerAttributeTuples;
    }
    /**
     * Reads and enables pagination through a set of `DeckPlayerAttributeTuple`.
     */
    public void setAllDeckPlayerAttributeTuples(DeckPlayerAttributeTuplesConnection allDeckPlayerAttributeTuples) {
        this.allDeckPlayerAttributeTuples = allDeckPlayerAttributeTuples;
    }

    /**
     * Reads and enables pagination through a set of `Class`.
     */
    public ClassesConnection getAllClasses() {
        return allClasses;
    }
    /**
     * Reads and enables pagination through a set of `Class`.
     */
    public void setAllClasses(ClassesConnection allClasses) {
        this.allClasses = allClasses;
    }

    /**
     * Reads and enables pagination through a set of `Friend`.
     */
    public FriendsConnection getAllFriends() {
        return allFriends;
    }
    /**
     * Reads and enables pagination through a set of `Friend`.
     */
    public void setAllFriends(FriendsConnection allFriends) {
        this.allFriends = allFriends;
    }

    /**
     * Reads and enables pagination through a set of `MatchmakingTicket`.
     */
    public MatchmakingTicketsConnection getAllMatchmakingTickets() {
        return allMatchmakingTickets;
    }
    /**
     * Reads and enables pagination through a set of `MatchmakingTicket`.
     */
    public void setAllMatchmakingTickets(MatchmakingTicketsConnection allMatchmakingTickets) {
        this.allMatchmakingTickets = allMatchmakingTickets;
    }

    /**
     * Reads and enables pagination through a set of `Deck`.
     */
    public DecksConnection getAllDecks() {
        return allDecks;
    }
    /**
     * Reads and enables pagination through a set of `Deck`.
     */
    public void setAllDecks(DecksConnection allDecks) {
        this.allDecks = allDecks;
    }

    /**
     * Reads and enables pagination through a set of `MatchmakingQueue`.
     */
    public MatchmakingQueuesConnection getAllMatchmakingQueues() {
        return allMatchmakingQueues;
    }
    /**
     * Reads and enables pagination through a set of `MatchmakingQueue`.
     */
    public void setAllMatchmakingQueues(MatchmakingQueuesConnection allMatchmakingQueues) {
        this.allMatchmakingQueues = allMatchmakingQueues;
    }

    /**
     * Reads and enables pagination through a set of `Card`.
     */
    public CardsConnection getAllCards() {
        return allCards;
    }
    /**
     * Reads and enables pagination through a set of `Card`.
     */
    public void setAllCards(CardsConnection allCards) {
        this.allCards = allCards;
    }

    /**
     * Reads and enables pagination through a set of `CollectionCard`.
     */
    public CollectionCardsConnection getAllCollectionCards() {
        return allCollectionCards;
    }
    /**
     * Reads and enables pagination through a set of `CollectionCard`.
     */
    public void setAllCollectionCards(CollectionCardsConnection allCollectionCards) {
        this.allCollectionCards = allCollectionCards;
    }

    /**
     * Reads and enables pagination through a set of `GeneratedArt`.
     */
    public GeneratedArtsConnection getAllGeneratedArts() {
        return allGeneratedArts;
    }
    /**
     * Reads and enables pagination through a set of `GeneratedArt`.
     */
    public void setAllGeneratedArts(GeneratedArtsConnection allGeneratedArts) {
        this.allGeneratedArts = allGeneratedArts;
    }

    /**
     * Reads and enables pagination through a set of `Game`.
     */
    public GamesConnection getAllGames() {
        return allGames;
    }
    /**
     * Reads and enables pagination through a set of `Game`.
     */
    public void setAllGames(GamesConnection allGames) {
        this.allGames = allGames;
    }

    /**
     * Reads and enables pagination through a set of `GameUser`.
     */
    public GameUsersConnection getAllGameUsers() {
        return allGameUsers;
    }
    /**
     * Reads and enables pagination through a set of `GameUser`.
     */
    public void setAllGameUsers(GameUsersConnection allGameUsers) {
        this.allGameUsers = allGameUsers;
    }



    public static Query.Builder builder() {
        return new Query.Builder();
    }

    public static class Builder {

        private Query query;
        private String nodeId;
        private Node node;
        private BannedDraftCard bannedDraftCardByCardId;
        private BotUser botUserById;
        private HardRemovalCard hardRemovalCardByCardId;
        private PublishedCard publishedCardById;
        private Guest guestById;
        private DeckShare deckShareByDeckIdAndShareRecipientId;
        private CardsInDeck cardsInDeckById;
        private DeckPlayerAttributeTuple deckPlayerAttributeTupleById;
        private Friend friendByIdAndFriend;
        private MatchmakingTicket matchmakingTicketByUserId;
        private Deck deckById;
        private MatchmakingQueue matchmakingQueueById;
        private Card cardBySuccession;
        private GeneratedArt generatedArtByHashAndOwner;
        private Game gameById;
        private GameUser gameUserByGameIdAndUserId;
        private String getUserId;
        private Boolean canSeeDeck;
        private Card getLatestCard;
        private BannedDraftCard bannedDraftCard;
        private BotUser botUser;
        private HardRemovalCard hardRemovalCard;
        private PublishedCard publishedCard;
        private Guest guest;
        private DeckShare deckShare;
        private CardsInDeck cardsInDeck;
        private DeckPlayerAttributeTuple deckPlayerAttributeTuple;
        private Friend friend;
        private MatchmakingTicket matchmakingTicket;
        private Deck deck;
        private MatchmakingQueue matchmakingQueue;
        private Card card;
        private Game game;
        private GameUser gameUser;
        private BannedDraftCardsConnection allBannedDraftCards;
        private BotUsersConnection allBotUsers;
        private HardRemovalCardsConnection allHardRemovalCards;
        private PublishedCardsConnection allPublishedCards;
        private GuestsConnection allGuests;
        private DeckSharesConnection allDeckShares;
        private CardsInDecksConnection allCardsInDecks;
        private DeckPlayerAttributeTuplesConnection allDeckPlayerAttributeTuples;
        private ClassesConnection allClasses;
        private FriendsConnection allFriends;
        private MatchmakingTicketsConnection allMatchmakingTickets;
        private DecksConnection allDecks;
        private MatchmakingQueuesConnection allMatchmakingQueues;
        private CardsConnection allCards;
        private CollectionCardsConnection allCollectionCards;
        private GeneratedArtsConnection allGeneratedArts;
        private GamesConnection allGames;
        private GameUsersConnection allGameUsers;

        public Builder() {
        }

        /**
         * Exposes the root query type nested one level down. This is helpful for Relay 1
which can only query top level fields if they are in a particular form.
         */
        public Builder setQuery(Query query) {
            this.query = query;
            return this;
        }

        /**
         * The root query type must be a `Node` to work well with Relay 1 mutations. This just resolves to `query`.
         */
        public Builder setNodeId(String nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        /**
         * Fetches an object given its globally unique `ID`.
         */
        public Builder setNode(Node node) {
            this.node = node;
            return this;
        }

        /**
         * Get a single `BannedDraftCard`.
         */
        public Builder setBannedDraftCardByCardId(BannedDraftCard bannedDraftCardByCardId) {
            this.bannedDraftCardByCardId = bannedDraftCardByCardId;
            return this;
        }

        /**
         * Get a single `BotUser`.
         */
        public Builder setBotUserById(BotUser botUserById) {
            this.botUserById = botUserById;
            return this;
        }

        /**
         * Get a single `HardRemovalCard`.
         */
        public Builder setHardRemovalCardByCardId(HardRemovalCard hardRemovalCardByCardId) {
            this.hardRemovalCardByCardId = hardRemovalCardByCardId;
            return this;
        }

        /**
         * Get a single `PublishedCard`.
         */
        public Builder setPublishedCardById(PublishedCard publishedCardById) {
            this.publishedCardById = publishedCardById;
            return this;
        }

        /**
         * Get a single `Guest`.
         */
        public Builder setGuestById(Guest guestById) {
            this.guestById = guestById;
            return this;
        }

        /**
         * Get a single `DeckShare`.
         */
        public Builder setDeckShareByDeckIdAndShareRecipientId(DeckShare deckShareByDeckIdAndShareRecipientId) {
            this.deckShareByDeckIdAndShareRecipientId = deckShareByDeckIdAndShareRecipientId;
            return this;
        }

        /**
         * Get a single `CardsInDeck`.
         */
        public Builder setCardsInDeckById(CardsInDeck cardsInDeckById) {
            this.cardsInDeckById = cardsInDeckById;
            return this;
        }

        /**
         * Get a single `DeckPlayerAttributeTuple`.
         */
        public Builder setDeckPlayerAttributeTupleById(DeckPlayerAttributeTuple deckPlayerAttributeTupleById) {
            this.deckPlayerAttributeTupleById = deckPlayerAttributeTupleById;
            return this;
        }

        /**
         * Get a single `Friend`.
         */
        public Builder setFriendByIdAndFriend(Friend friendByIdAndFriend) {
            this.friendByIdAndFriend = friendByIdAndFriend;
            return this;
        }

        /**
         * Get a single `MatchmakingTicket`.
         */
        public Builder setMatchmakingTicketByUserId(MatchmakingTicket matchmakingTicketByUserId) {
            this.matchmakingTicketByUserId = matchmakingTicketByUserId;
            return this;
        }

        /**
         * Get a single `Deck`.
         */
        public Builder setDeckById(Deck deckById) {
            this.deckById = deckById;
            return this;
        }

        /**
         * Get a single `MatchmakingQueue`.
         */
        public Builder setMatchmakingQueueById(MatchmakingQueue matchmakingQueueById) {
            this.matchmakingQueueById = matchmakingQueueById;
            return this;
        }

        /**
         * Get a single `Card`.
         */
        public Builder setCardBySuccession(Card cardBySuccession) {
            this.cardBySuccession = cardBySuccession;
            return this;
        }

        /**
         * Get a single `GeneratedArt`.
         */
        public Builder setGeneratedArtByHashAndOwner(GeneratedArt generatedArtByHashAndOwner) {
            this.generatedArtByHashAndOwner = generatedArtByHashAndOwner;
            return this;
        }

        /**
         * Get a single `Game`.
         */
        public Builder setGameById(Game gameById) {
            this.gameById = gameById;
            return this;
        }

        /**
         * Get a single `GameUser`.
         */
        public Builder setGameUserByGameIdAndUserId(GameUser gameUserByGameIdAndUserId) {
            this.gameUserByGameIdAndUserId = gameUserByGameIdAndUserId;
            return this;
        }

        public Builder setGetUserId(String getUserId) {
            this.getUserId = getUserId;
            return this;
        }

        public Builder setCanSeeDeck(Boolean canSeeDeck) {
            this.canSeeDeck = canSeeDeck;
            return this;
        }

        public Builder setGetLatestCard(Card getLatestCard) {
            this.getLatestCard = getLatestCard;
            return this;
        }

        /**
         * Reads a single `BannedDraftCard` using its globally unique `ID`.
         */
        public Builder setBannedDraftCard(BannedDraftCard bannedDraftCard) {
            this.bannedDraftCard = bannedDraftCard;
            return this;
        }

        /**
         * Reads a single `BotUser` using its globally unique `ID`.
         */
        public Builder setBotUser(BotUser botUser) {
            this.botUser = botUser;
            return this;
        }

        /**
         * Reads a single `HardRemovalCard` using its globally unique `ID`.
         */
        public Builder setHardRemovalCard(HardRemovalCard hardRemovalCard) {
            this.hardRemovalCard = hardRemovalCard;
            return this;
        }

        /**
         * Reads a single `PublishedCard` using its globally unique `ID`.
         */
        public Builder setPublishedCard(PublishedCard publishedCard) {
            this.publishedCard = publishedCard;
            return this;
        }

        /**
         * Reads a single `Guest` using its globally unique `ID`.
         */
        public Builder setGuest(Guest guest) {
            this.guest = guest;
            return this;
        }

        /**
         * Reads a single `DeckShare` using its globally unique `ID`.
         */
        public Builder setDeckShare(DeckShare deckShare) {
            this.deckShare = deckShare;
            return this;
        }

        /**
         * Reads a single `CardsInDeck` using its globally unique `ID`.
         */
        public Builder setCardsInDeck(CardsInDeck cardsInDeck) {
            this.cardsInDeck = cardsInDeck;
            return this;
        }

        /**
         * Reads a single `DeckPlayerAttributeTuple` using its globally unique `ID`.
         */
        public Builder setDeckPlayerAttributeTuple(DeckPlayerAttributeTuple deckPlayerAttributeTuple) {
            this.deckPlayerAttributeTuple = deckPlayerAttributeTuple;
            return this;
        }

        /**
         * Reads a single `Friend` using its globally unique `ID`.
         */
        public Builder setFriend(Friend friend) {
            this.friend = friend;
            return this;
        }

        /**
         * Reads a single `MatchmakingTicket` using its globally unique `ID`.
         */
        public Builder setMatchmakingTicket(MatchmakingTicket matchmakingTicket) {
            this.matchmakingTicket = matchmakingTicket;
            return this;
        }

        /**
         * Reads a single `Deck` using its globally unique `ID`.
         */
        public Builder setDeck(Deck deck) {
            this.deck = deck;
            return this;
        }

        /**
         * Reads a single `MatchmakingQueue` using its globally unique `ID`.
         */
        public Builder setMatchmakingQueue(MatchmakingQueue matchmakingQueue) {
            this.matchmakingQueue = matchmakingQueue;
            return this;
        }

        /**
         * Reads a single `Card` using its globally unique `ID`.
         */
        public Builder setCard(Card card) {
            this.card = card;
            return this;
        }

        /**
         * Reads a single `Game` using its globally unique `ID`.
         */
        public Builder setGame(Game game) {
            this.game = game;
            return this;
        }

        /**
         * Reads a single `GameUser` using its globally unique `ID`.
         */
        public Builder setGameUser(GameUser gameUser) {
            this.gameUser = gameUser;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `BannedDraftCard`.
         */
        public Builder setAllBannedDraftCards(BannedDraftCardsConnection allBannedDraftCards) {
            this.allBannedDraftCards = allBannedDraftCards;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `BotUser`.
         */
        public Builder setAllBotUsers(BotUsersConnection allBotUsers) {
            this.allBotUsers = allBotUsers;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `HardRemovalCard`.
         */
        public Builder setAllHardRemovalCards(HardRemovalCardsConnection allHardRemovalCards) {
            this.allHardRemovalCards = allHardRemovalCards;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `PublishedCard`.
         */
        public Builder setAllPublishedCards(PublishedCardsConnection allPublishedCards) {
            this.allPublishedCards = allPublishedCards;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `Guest`.
         */
        public Builder setAllGuests(GuestsConnection allGuests) {
            this.allGuests = allGuests;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `DeckShare`.
         */
        public Builder setAllDeckShares(DeckSharesConnection allDeckShares) {
            this.allDeckShares = allDeckShares;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `CardsInDeck`.
         */
        public Builder setAllCardsInDecks(CardsInDecksConnection allCardsInDecks) {
            this.allCardsInDecks = allCardsInDecks;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `DeckPlayerAttributeTuple`.
         */
        public Builder setAllDeckPlayerAttributeTuples(DeckPlayerAttributeTuplesConnection allDeckPlayerAttributeTuples) {
            this.allDeckPlayerAttributeTuples = allDeckPlayerAttributeTuples;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `Class`.
         */
        public Builder setAllClasses(ClassesConnection allClasses) {
            this.allClasses = allClasses;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `Friend`.
         */
        public Builder setAllFriends(FriendsConnection allFriends) {
            this.allFriends = allFriends;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `MatchmakingTicket`.
         */
        public Builder setAllMatchmakingTickets(MatchmakingTicketsConnection allMatchmakingTickets) {
            this.allMatchmakingTickets = allMatchmakingTickets;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `Deck`.
         */
        public Builder setAllDecks(DecksConnection allDecks) {
            this.allDecks = allDecks;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `MatchmakingQueue`.
         */
        public Builder setAllMatchmakingQueues(MatchmakingQueuesConnection allMatchmakingQueues) {
            this.allMatchmakingQueues = allMatchmakingQueues;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `Card`.
         */
        public Builder setAllCards(CardsConnection allCards) {
            this.allCards = allCards;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `CollectionCard`.
         */
        public Builder setAllCollectionCards(CollectionCardsConnection allCollectionCards) {
            this.allCollectionCards = allCollectionCards;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `GeneratedArt`.
         */
        public Builder setAllGeneratedArts(GeneratedArtsConnection allGeneratedArts) {
            this.allGeneratedArts = allGeneratedArts;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `Game`.
         */
        public Builder setAllGames(GamesConnection allGames) {
            this.allGames = allGames;
            return this;
        }

        /**
         * Reads and enables pagination through a set of `GameUser`.
         */
        public Builder setAllGameUsers(GameUsersConnection allGameUsers) {
            this.allGameUsers = allGameUsers;
            return this;
        }


        public Query build() {
            return new Query(query, nodeId, node, bannedDraftCardByCardId, botUserById, hardRemovalCardByCardId, publishedCardById, guestById, deckShareByDeckIdAndShareRecipientId, cardsInDeckById, deckPlayerAttributeTupleById, friendByIdAndFriend, matchmakingTicketByUserId, deckById, matchmakingQueueById, cardBySuccession, generatedArtByHashAndOwner, gameById, gameUserByGameIdAndUserId, getUserId, canSeeDeck, getLatestCard, bannedDraftCard, botUser, hardRemovalCard, publishedCard, guest, deckShare, cardsInDeck, deckPlayerAttributeTuple, friend, matchmakingTicket, deck, matchmakingQueue, card, game, gameUser, allBannedDraftCards, allBotUsers, allHardRemovalCards, allPublishedCards, allGuests, allDeckShares, allCardsInDecks, allDeckPlayerAttributeTuples, allClasses, allFriends, allMatchmakingTickets, allDecks, allMatchmakingQueues, allCards, allCollectionCards, allGeneratedArts, allGames, allGameUsers);
        }

    }
}
