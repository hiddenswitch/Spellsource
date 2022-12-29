package com.hiddenswitch.framework.impl;

/**
 * A delegate that needs to know when players have connected, and with which {@link Client}.
 */
public interface ClientConnectionHandler {
	void onPlayerReady(Client client);

	void onPlayerReconnected(Client client);
}
