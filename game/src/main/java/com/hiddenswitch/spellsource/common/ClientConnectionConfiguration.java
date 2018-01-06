package com.hiddenswitch.spellsource.common;

import java.io.Serializable;

/**
 * A class storing the information a client needs to connect to the correct game session server and game.
 */
public class ClientConnectionConfiguration implements Serializable {
	private final String userId;

	/**
	 * Create a new client connection configuration. This will be returned to the client from the matchmaking service
	 * and helps the client connect to the correct host and identify itself to the server.
	 */
	public ClientConnectionConfiguration(String url, String userId, String playerSecret) {
		this.userId = userId;
		String url1 = url;
	}

	public String getUserId() {
		return userId;
	}

}
