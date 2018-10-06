package com.hiddenswitch.spellsource.impl.server;

import com.github.fromage.quasi.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.Client;

/**
 * A delegate that needs to know when players have connected, and with which {@link Client}.
 */
public interface ClientConnectionHandler {
	@Suspendable
	void onPlayerReady(Client client);

	@Suspendable
	void onPlayerReconnected(Client client);
}
