package com.hiddenswitch.spellsource.net.impl;

import org.apache.commons.lang3.RandomStringUtils;

public final class GameId extends StringEx {

	@Deprecated
	public GameId() {
		super();
	}

	public GameId(String id) {
		super(id);
	}

	public static GameId create() {
		return new GameId(RandomStringUtils.randomAlphanumeric(12).toLowerCase());
	}
}