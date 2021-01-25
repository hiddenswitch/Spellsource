package com.hiddenswitch.framework.impl;

import co.paralleluniverse.fibers.Suspendable;

/**
 * A delegate that needs to know when players have connected, and with which {@link Client}.
 */
public interface ClientConnectionHandler {
	@Suspendable
	void onPlayerReady(Client client);

	@Suspendable
	void onPlayerReconnected(Client client);
}
