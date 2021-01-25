package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;

/**
 * Returns a snapshot of all the cards that the specified {@link CardSourceArg#TARGET_PLAYER} has shuffled into their
 * deck.
 */
public class ShuffledCardsSource extends CardSource {
	public ShuffledCardsSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		// Snapshot
		return new CardArrayList(EnvironmentEntityList.getList(context, Environment.SHUFFLED_CARDS_LIST).getCards(context, player));
	}
}
