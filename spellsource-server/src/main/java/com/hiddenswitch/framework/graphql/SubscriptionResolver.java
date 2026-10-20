package com.hiddenswitch.framework.graphql;


/**
 * ─── Subscription ─────────────────────────────────────────────
 */
public interface SubscriptionResolver {

    /**
     * Streams game messages for the authenticated user's active game.
Open this subscription before calling connectToGame. The mutation waits until
this subscription is ready before initiating the game connection.
Emits ServerGameMessage for each game state change, action request,
mulligan, event, and game over.
     */
    org.reactivestreams.Publisher<ServerGameMessage> gameMessages(String connectionId) throws Exception;

    /**
     * Emits when the current user is matched into a game.
     */
    org.reactivestreams.Publisher<MatchFound> matchFound() throws Exception;

    /**
     * Emits friend list changes (added, removed, presence updates).
     */
    org.reactivestreams.Publisher<Friend> friendUpdated() throws Exception;

    /**
     * Emits when an invite is received or its status changes.
     */
    org.reactivestreams.Publisher<Invite> inviteUpdated() throws Exception;

    /**
     * Emits when an editable card is added, changed, or removed.
     */
    org.reactivestreams.Publisher<EditableCard> editableCardUpdated() throws Exception;

}
