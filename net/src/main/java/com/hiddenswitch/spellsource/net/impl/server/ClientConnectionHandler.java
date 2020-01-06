package com.hiddenswitch.spellsource.net.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.impl.Client;

/**
 * A delegate that needs to know when players have connected, and with which {@link Client}.
 */
public interface ClientConnectionHandler {
	@Suspendable
	void onPlayerReady(Client client);

	@Suspendable
	void onPlayerReconnected(Client client);
}
