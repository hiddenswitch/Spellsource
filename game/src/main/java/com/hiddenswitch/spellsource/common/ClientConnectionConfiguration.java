package com.hiddenswitch.spellsource.common;

import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.client.models.ClientToServerMessage;
import com.hiddenswitch.spellsource.client.models.MessageType;

import java.io.Serializable;

/**
 * A class storing the information a client needs to connect to the correct game session server and game.
 */
public class ClientConnectionConfiguration implements Serializable {
	private final String url;
	private final String playerKey;
	private final String playerSecret;

	/**
	 * Create a new client connection configuration. This will be returned to the client from the matchmaking service
	 * and helps the client connect to the correct host and identify itself to the server.
	 */
	public ClientConnectionConfiguration(String url, String playerKey, String playerSecret) {
		this.playerKey = playerKey;
		this.playerSecret = playerSecret;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public String getPlayerKey() {
		return playerKey;
	}

	public String getPlayerSecret() {
		return playerSecret;
	}

	public MatchmakingQueuePutResponseUnityConnection toUnityConnection() {
		return new MatchmakingQueuePutResponseUnityConnection()
				.url(getUrl())
				.firstMessage(new ClientToServerMessage()
						.messageType(MessageType.FIRST_MESSAGE)
						.firstMessage(new ClientToServerMessageFirstMessage()
								.playerKey(getPlayerKey())
								.playerSecret(getPlayerSecret())));
	}
}
