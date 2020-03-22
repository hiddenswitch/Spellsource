package com.hiddenswitch.spellsource.micro;

import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
public class RequestTraceMulligan {
	private int playerId;
	private List<Integer> entityIds;

	public int getPlayerId() {
		return playerId;
	}

	public RequestTraceMulligan setPlayerId(int playerId) {
		this.playerId = playerId;
		return this;
	}

	public List<Integer> getEntityIds() {
		return entityIds;
	}

	public RequestTraceMulligan setEntityIds(List<Integer> entityIds) {
		this.entityIds = entityIds;
		return this;
	}

	public RequestTraceMulligan() {
	}
}
