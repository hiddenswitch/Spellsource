package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.net.common.ClientConnectionConfiguration;

import java.io.Serializable;

/**
 * Created by bberman on 4/2/17.
 */
public class BotsStartGameResponse implements Serializable {
	private ClientConnectionConfiguration playerConnection;

	public ClientConnectionConfiguration getPlayerConnection() {
		return playerConnection;
	}

	public void setPlayerConnection(ClientConnectionConfiguration playerConnection) {
		this.playerConnection = playerConnection;
	}
}
