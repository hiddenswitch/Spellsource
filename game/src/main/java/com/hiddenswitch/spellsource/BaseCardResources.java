package com.hiddenswitch.spellsource;

/**
 * Represents card resources with some base cards that are useful for testing and ensuring there is always at least one
 * champion / class, format and set available for the runtime.
 *
 * @see AbstractCardResources for more about how to create a cards package.
 */
public class BaseCardResources extends AbstractCardResources<BaseCardResources> {

	public BaseCardResources() {
		super(BaseCardResources.class);
	}

	@Override
	public String getDirectoryPrefix() {
		return "basecards";
	}
}
