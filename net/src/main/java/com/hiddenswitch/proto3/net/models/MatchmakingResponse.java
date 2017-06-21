package com.hiddenswitch.proto3.net.models;

import com.hiddenswitch.proto3.net.client.models.MatchmakingQueuePutResponseUnityConnection;
import com.hiddenswitch.proto3.net.common.ClientConnectionConfiguration;

import java.io.Serializable;

public class MatchmakingResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	private ClientConnectionConfiguration connection;

	private MatchmakingRequest retry;

	public ClientConnectionConfiguration getConnection() {
		return connection;
	}

	public void setConnection(ClientConnectionConfiguration connection) {
		this.connection = connection;
	}

	public MatchmakingRequest getRetry() {
		return retry;
	}

	public void setRetry(MatchmakingRequest retry) {
		this.retry = retry;
	}

	public MatchmakingQueuePutResponseUnityConnection getUnityConnection() {
		return getConnection().toUnityConnection();
	}
}
