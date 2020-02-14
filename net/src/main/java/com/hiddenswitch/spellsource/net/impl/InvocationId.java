package com.hiddenswitch.spellsource.net.impl;

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
