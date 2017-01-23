package com.hiddenswitch.proto3.net.common;

import com.hiddenswitch.proto3.net.common.ClientConnectionConfiguration;
import com.hiddenswitch.proto3.net.common.MatchmakingRequest;

import java.io.Serializable;

public class MatchmakingResponse implements Serializable {
	private static final long serialVersionUID = 1L;
	private ClientConnectionConfiguration connection;

	private MatchmakingQueuePut retry;

	public ClientConnectionConfiguration getConnection() {
		return connection;
	}

	public void setConnection(ClientConnectionConfiguration connection) {
		this.connection = connection;
	}

	public MatchmakingQueuePut getRetry() {
		return retry;
	}

	public void setRetry(MatchmakingQueuePut retry) {
		this.retry = retry;
	}
}
