package com.hiddenswitch.spellsource;

public class BaseCardResources extends AbstractCardResources<BaseCardResources> {

	public BaseCardResources() {
		super(BaseCardResources.class);
	}

	@Override
	public String getDirectoryPrefix() {
		return "basecards";
	}
}
