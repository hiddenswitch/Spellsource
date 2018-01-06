package com.hiddenswitch.spellsource.impl;

import org.apache.commons.lang3.RandomUtils;

public class InvocationId extends NumberEx {

	public InvocationId() {
		super(System.nanoTime());
	}
}
