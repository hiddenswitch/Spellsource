package com.hiddenswitch.proto3.net.models;

import java.io.Serializable;

/**
 * Created by bberman on 6/6/17.
 */
public class PersistAttributeResponse implements Serializable {
	private LogicResponse logicResponse;
	private Long updated;

	public LogicResponse getLogicResponse() {
		return logicResponse;
	}

	public void setLogicResponse(LogicResponse logicResponse) {
		this.logicResponse = logicResponse;
	}

	public PersistAttributeResponse withResponse(LogicResponse logicResponse) {
		this.logicResponse = logicResponse;
		return this;
	}

	public Long getUpdated() {
		return updated;
	}

	public void setUpdated(Long updated) {
		this.updated = updated;
	}

	public PersistAttributeResponse withUpdated(final Long updated) {
		this.updated = updated;
		return this;
	}
}
