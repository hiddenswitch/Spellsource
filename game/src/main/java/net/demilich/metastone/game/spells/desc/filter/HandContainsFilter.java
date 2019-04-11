package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardZone;

public final class HandContainsFilter extends ZoneContainsFilter {

	public HandContainsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected CardZone getZone(Player player) {
		return player.getHand();
	}
}
