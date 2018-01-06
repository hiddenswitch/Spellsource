package com.hiddenswitch.spellsource.impl;

import org.apache.commons.lang3.RandomStringUtils;

public final class GameId extends StringEx {
	public final static GameId PENDING = new GameId("!!pending");

	public GameId() {
		super(RandomStringUtils.randomAlphanumeric(28).toLowerCase());
	}

	public GameId(String id) {
		super(id);
	}
}

