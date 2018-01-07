package com.hiddenswitch.spellsource.impl;

import org.apache.commons.lang3.RandomUtils;

public final class InvocationId extends NumberEx {
	@Deprecated
	public InvocationId() {
		super();
	}

	@SuppressWarnings("deprecation")
	public static InvocationId create() {
		final InvocationId invocationId = new InvocationId();
		invocationId.id = System.nanoTime();
		return invocationId;
	}
}
