package com.hiddenswitch.framework.graphql;


/**
 * ─── Query ────────────────────────────────────────────────────
 */
public interface QueryResolver {

    /**
     * auth
     */
    io.vertx.core.Future<String> currentUserId() throws Exception;

    io.vertx.core.Future<ClientConfiguration> configuration() throws Exception;

    /**
     * cards
     */
    io.vertx.core.Future<GetCardsResponse> cards(String ifNoneMatch) throws Exception;

    io.vertx.core.Future<GetCardsResponse> cardsByUser(String ifNoneMatch) throws Exception;

    /**
     * account
     */
    io.vertx.core.Future<UserEntity> account() throws Exception;

    io.vertx.core.Future<java.util.List<UserEntity>> accounts(java.util.List<String> userIds) throws Exception;

    /**
     * decks
     */
    io.vertx.core.Future<DecksGetResponse> deck(String deckId) throws Exception;

    io.vertx.core.Future<java.util.List<DecksGetResponse>> decks() throws Exception;

    /**
     * drafts
     */
    io.vertx.core.Future<DraftState> draft() throws Exception;

    /**
     * friends (snapshot, use subscription for live updates)
     */
    io.vertx.core.Future<java.util.List<Friend>> friends() throws Exception;

    /**
     * invites
     */
    io.vertx.core.Future<InviteResponse> invite(String inviteId) throws Exception;

    io.vertx.core.Future<java.util.List<Invite>> invites() throws Exception;

    /**
     * matchmaking
     */
    io.vertx.core.Future<java.util.List<MatchmakingQueue>> matchmakingQueues() throws Exception;

    io.vertx.core.Future<String> isInMatch() throws Exception;

    /**
     * game records
     */
    io.vertx.core.Future<GameRecord> gameRecord(String gameId) throws Exception;

    io.vertx.core.Future<java.util.List<String>> gameRecordIds() throws Exception;

    /**
     * rogue (existing)
     */
    io.vertx.core.Future<java.util.List<String>> currentRogueClasses() throws Exception;

    io.vertx.core.Future<Integer> rerollCost(java.lang.Long rogueId) throws Exception;

    io.vertx.core.Future<Integer> trashCardCost(java.lang.Long rogueId, String cardId) throws Exception;

    io.vertx.core.Future<Integer> upgradeCardCost(java.lang.Long rogueId, String cardId) throws Exception;

}
