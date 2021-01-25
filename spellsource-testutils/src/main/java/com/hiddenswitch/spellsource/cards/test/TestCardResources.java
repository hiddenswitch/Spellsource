package com.hiddenswitch.spellsource.cards.test;

import com.hiddenswitch.spellsource.core.AbstractCardResources;

public class TestCardResources extends AbstractCardResources<TestCardResources> {

	public static final String TEST = "TEST";

	public TestCardResources() {
		super(TestCardResources.class);
	}

	@Override
	public String getDirectoryPrefix() {
		return "test";
	}
}
