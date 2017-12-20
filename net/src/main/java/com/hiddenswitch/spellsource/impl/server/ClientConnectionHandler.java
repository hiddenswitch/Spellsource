package com.hiddenswitch.spellsource.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.Writer;

/**
 * A delegate that needs to know when players have connected, and with which {@link Writer}.
 */
public interface ClientConnectionHandler {
	@Suspendable
	void onPlayerConnected(int playerId, Writer writer);

	@Suspendable
	void onPlayerReconnected(int playerId, Writer writer);
}
