package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

/**
 * Returns the list of source cards for the minions on the battlefield of the {@link CardSourceArg#TARGET_PLAYER}.
 */
public final class SummonWithoutReplacementCardSource extends CardSource {

	public SummonWithoutReplacementCardSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return new CardArrayList();
	}
}
