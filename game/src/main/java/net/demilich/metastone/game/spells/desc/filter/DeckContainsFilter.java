package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardZone;

public final class DeckContainsFilter extends ZoneContainsFilter {

	public DeckContainsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected CardZone getZone(Player player) {
		return player.getDeck();
	}
}
