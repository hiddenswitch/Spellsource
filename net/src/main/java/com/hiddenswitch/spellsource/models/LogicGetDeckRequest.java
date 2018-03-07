package com.hiddenswitch.spellsource.models;

import com.hiddenswitch.spellsource.impl.UserId;

import java.io.Serializable;

public final class LogicGetDeckRequest implements Serializable {
	private UserId userId;
	private String name;

	public LogicGetDeckRequest() {
	}

	public UserId getUserId() {
		return userId;
	}

	public void setUserId(UserId userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LogicGetDeckRequest withUserId(UserId userId) {
		this.userId = userId;
		return this;
	}

	public LogicGetDeckRequest withDeckName(String name) {
		this.name = name;
		return this;
	}
}
