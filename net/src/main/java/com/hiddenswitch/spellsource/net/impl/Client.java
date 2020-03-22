package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.Emote;
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
	@Suspendable
	void sendEmote(int entityId, Emote.MessageEnum emote);

	/**
	 * Called when the last event in a stack of {@link GameEvent} has been evaluated. Typically, a client should be
	 * notified of an entire sequence of events, so that it has valid data at the end of each event, rather than as the
	 * events are processed.
	 */
	@Suspendable
	void lastEvent();

	int getPlayerId();

	String getUserId();

	void closeInboundMessages();
}