package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardZone;

public final class DeckContainsFilter extends ZoneContainsFilter {

	private static final long serialVersionUID = 8473674909645399828L;

	public DeckContainsFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected CardZone getZone(Player player) {
		return player.getDeck();
	}
}
