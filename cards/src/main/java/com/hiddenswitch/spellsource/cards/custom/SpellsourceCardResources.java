package com.hiddenswitch.spellsource.cards.custom;

import com.hiddenswitch.spellsource.core.AbstractCardResources;

public final class SpellsourceCardResources extends AbstractCardResources<SpellsourceCardResources> {

	public SpellsourceCardResources() {
		super(SpellsourceCardResources.class);
	}

	@Override
	public String getDirectoryPrefix() {
		return "cards";
	}
}

