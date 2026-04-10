package com.hiddenswitch.framework.graphql;


/**
 * ─── Mutation ─────────────────────────────────────────────────
 */
public interface MutationResolver {

    /**
     * auth
     */
    io.vertx.core.Future<LoginOrCreateReply> createAccount(CreateAccountInput input) throws Exception;

    io.vertx.core.Future<LoginOrCreateReply> login(LoginInput input) throws Exception;

    io.vertx.core.Future<LoginOrCreateReply> changePassword(String newPassword) throws Exception;

    io.vertx.core.Future<Boolean> requestPasswordResetEmail() throws Exception;

    /**
     * decks
     */
    io.vertx.core.Future<DecksPutResponse> createDeck(DecksPutInput input) throws Exception;

    io.vertx.core.Future<DecksGetResponse> updateDeck(DecksUpdateInput input) throws Exception;

    io.vertx.core.Future<Boolean> deleteDeck(String deckId) throws Exception;

    io.vertx.core.Future<DecksGetResponse> duplicateDeck(String deckId) throws Exception;

    /**
     * drafts
     */
    io.vertx.core.Future<DraftState> startOrModifyDraft(DraftsPostInput input) throws Exception;

    io.vertx.core.Future<DraftState> draftsChooseHero(int heroIndex) throws Exception;

    io.vertx.core.Future<DraftState> draftsChooseCard(int cardIndex) throws Exception;

    /**
     * friends
     */
    io.vertx.core.Future<Friend> addFriend(String friendId, String usernameWithToken) throws Exception;

    io.vertx.core.Future<Boolean> removeFriend(String friendId) throws Exception;

    /**
     * invites
     */
    io.vertx.core.Future<InviteResponse> sendInvite(InvitePostInput input) throws Exception;

    io.vertx.core.Future<InviteResponse> acceptInvite(AcceptInviteInput input) throws Exception;

    io.vertx.core.Future<InviteResponse> deleteInvite(String inviteId) throws Exception;

    /**
     * matchmaking
     */
    io.vertx.core.Future<Boolean> enqueueMatchmaking(MatchmakingEnqueueInput input) throws Exception;

    io.vertx.core.Future<Boolean> cancelMatchmaking() throws Exception;

    /**
     * Connect to an active game. Must be called before gameMessages subscription will emit.
     */
    io.vertx.core.Future<Boolean> connectToGame(String playerKey, String playerSecret) throws Exception;

    /**
     * Send a game action in response to an ON_REQUEST_ACTION message.
     */
    io.vertx.core.Future<Boolean> sendGameAction(int actionIndex, String repliesTo) throws Exception;

    /**
     * Send a mulligan response in response to an ON_MULLIGAN message.
     */
    io.vertx.core.Future<Boolean> sendMulligan(java.util.List<Integer> discardedCardIndices, String repliesTo) throws Exception;

    /**
     * Send an emote during a game.
     */
    io.vertx.core.Future<Boolean> sendEmote(int entityId, EmoteType message) throws Exception;

    /**
     * Concede the current game.
     */
    io.vertx.core.Future<Boolean> concedeGame() throws Exception;

    /**
     * Notify the opponent that a local entity is being hovered.
     */
    io.vertx.core.Future<Boolean> touchEntity(int entityId) throws Exception;

    /**
     * Notify the opponent that a local entity is no longer being hovered.
     */
    io.vertx.core.Future<Boolean> untouchEntity(int entityId) throws Exception;

    /**
     * cards (editable)
     */
    io.vertx.core.Future<PutCardResult> putCard(PutCardInput input) throws Exception;

    io.vertx.core.Future<Boolean> deleteCard(String editableCardId) throws Exception;

    /**
     * rogue (existing)
     */
    io.vertx.core.Future<RogueRun> startRogueRun(String heroClass, java.lang.Long seed) throws Exception;

    io.vertx.core.Future<RogueRun> makeRogueChoice(java.lang.Long choiceId, java.util.List<Integer> choices) throws Exception;

    io.vertx.core.Future<RogueRun> reroll(java.lang.Long choiceId) throws Exception;

    io.vertx.core.Future<RogueRun> trashCard(java.lang.Long rogueId, String cardId) throws Exception;

    io.vertx.core.Future<RogueRun> skipBoss(java.lang.Long rogueId) throws Exception;

    io.vertx.core.Future<RogueRun> upgradeCard(java.lang.Long rogueId, String cardId) throws Exception;

}
