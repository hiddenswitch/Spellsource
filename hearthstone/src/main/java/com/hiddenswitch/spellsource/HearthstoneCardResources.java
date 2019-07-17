package com.hiddenswitch.spellsource;

public final class HearthstoneCardResources extends AbstractCardResources<HearthstoneCardResources> {
	public HearthstoneCardResources() {
		super(HearthstoneCardResources.class);
	}

	@Override
	public String getDirectoryPrefix() {
		return "hearthstone";
	}
}
