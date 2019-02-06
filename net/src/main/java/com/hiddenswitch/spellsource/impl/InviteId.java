package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.impl.StringEx;
import org.apache.commons.lang3.RandomStringUtils;

public final class InviteId extends StringEx {

	@Deprecated
	public InviteId() {
		super();
	}

	public InviteId(String id) {
		super(id);
	}

	public static InviteId create() {
		return new InviteId(RandomStringUtils.randomAlphanumeric(12).toLowerCase());
	}
}