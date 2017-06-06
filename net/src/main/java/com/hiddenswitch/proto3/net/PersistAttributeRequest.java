package com.hiddenswitch.proto3.net;

import com.hiddenswitch.proto3.net.models.EventLogicRequest;

import java.io.Serializable;

/**
 * Created by bberman on 6/6/17.
 */
public class PersistAttributeRequest implements Serializable {
	private String id;
	private EventLogicRequest request;

	public PersistAttributeRequest withId(String id) {
		this.id = id;
		return this;
	}

	public PersistAttributeRequest withRequest(EventLogicRequest request) {
		this.request = request;
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public EventLogicRequest getRequest() {
		return request;
	}

	public void setRequest(EventLogicRequest request) {
		this.request = request;
	}
}
