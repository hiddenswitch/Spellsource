package com.hiddenswitch.proto3.net;

import com.hiddenswitch.proto3.net.models.LogicResponse;

import java.io.Serializable;

/**
 * Created by bberman on 6/6/17.
 */
public class PersistAttributeResponse implements Serializable {
	private LogicResponse logicResponse;

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
}
