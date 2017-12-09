package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.impl.BroadcasterImpl;
import io.vertx.core.Verticle;

/**
 * A service that broadcasts, in a simple UDP protocol, the server host to connect to.
 * <p>
 * The client listens for UDP broadcats from {@link #getMulticastAddress()}. Then, the client sends a UDP datagram
 * containing {@link #getClientCall()} to broadcast address {@link #getMulticastAddress()}. The server responds with
 * {@code getResponsePrefix() + "http://ipaddress:8080"}, a local area network-accessible address to connect to.
 */
public interface Broadcaster extends Verticle {
	/**
	 * Creates a new default Broadcaster.
	 *
	 * @return
	 */
	static Broadcaster create() {
		return new BroadcasterImpl();
	}

	/**
	 * Retrieves the configured address used for UDP multicasting.
	 *
	 * @return
	 */
	String getMulticastAddress();

	/**
	 * Retrieves the message expected from clients to respond to.
	 *
	 * @return
	 */
	String getClientCall();

	/**
	 * Retrieves the standard (otherwise unused) port for broadcasting.
	 *
	 * @return
	 */
	int getMulticastPort();

	/**
	 * Returns the prefix for the datagram containing the server URL.
	 *
	 * @return
	 */
	String getResponsePrefix();
}
