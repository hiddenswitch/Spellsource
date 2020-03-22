package com.hiddenswitch.spellsource.micro;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class Payload {
	private RequestTrace trace;
	private int playerId;

	public Payload() {
	}

	public RequestTrace getTrace() {
		return trace;
	}

	public Payload setTrace(RequestTrace trace) {
		this.trace = trace;
		return this;
	}

	public int getPlayerId() {
		return playerId;
	}

	public Payload setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}
}
