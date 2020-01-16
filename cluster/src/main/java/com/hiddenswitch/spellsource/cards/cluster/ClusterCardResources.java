package com.hiddenswitch.spellsource.cards.cluster;

import com.hiddenswitch.spellsource.core.AbstractCardResources;

public final class ClusterCardResources extends AbstractCardResources<ClusterCardResources> {

	public ClusterCardResources() {
		super(ClusterCardResources.class);
	}

	@Override
	public String getDirectoryPrefix() {
		return "cards/deckgencards";
	}
}

