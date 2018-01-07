package com.hiddenswitch.spellsource.common;

import com.hiddenswitch.spellsource.util.DefaultClusterSerializable;

import java.io.Serializable;

/**
 * A class storing the information a client needs to connect to the correct game session server and game.
 */
public class ClientConnectionConfigurationImpl implements DefaultClusterSerializable, Serializable, ClientConnectionConfiguration {
	public String userId;

	/**
	 * Create a new client connection configuration. This will be returned to the client from the matchmaking service
	 * and helps the client connect to the correct host and identify itself to the server.
	 */
	public ClientConnectionConfigurationImpl(String url, String userId) {
		this.userId = userId;
	}

	@Deprecated
	public ClientConnectionConfigurationImpl()  {
	}

	@Override
	public String getUserId() {
		return userId;
	}

}
