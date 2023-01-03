package com.hiddenswitch.framework.impl;

import io.vertx.core.Closeable;
import net.demilich.metastone.game.events.GameEvent;

/**
 * An interface that specifies the boundary between a {@link net.demilich.metastone.game.GameContext} and a networking
 * channel like a websocket or a plain TCP socket.
 */
public interface Client extends ActionListener, GameEventListener, HasElapsableTurns, Closeable {

	/**
	 * Send an emote to a client. By default, there's no implementation except for Unity clients.
	 *
	 * @param entityId The entity from which the emote should originate. Typically a hero entity.
	 * @param emote    The emote to send
	 */
	@Override
	void sendEmote(int entityId, String emote);

	/**
	 * Called when the last event in a stack of {@link GameEvent} has been evaluated. Typically, a client should be
	 * notified of an entire sequence of events, so that it has valid data at the end of each event, rather than as the
	 * events are processed.
	 */
	void lastEvent();

	int getPlayerId();

	String getUserId();

	void closeInboundMessages();

	/**
	 * Copies all the requests to the target client.
	 * <p>
	 * This allows the client to resume pending requests correctly.
	 *
	 * @param client
	 */
	void copyRequestsTo(Client client);
}