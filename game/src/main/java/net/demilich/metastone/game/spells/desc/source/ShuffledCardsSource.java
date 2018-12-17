package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.custom.EnvironmentEntityList;

/**
 * Returns all the cards that the specified {@link CardSourceArg#TARGET_PLAYER} has shuffled into their deck.
 */
public class ShuffledCardsSource extends CardSource {
	private static final long serialVersionUID = -1094249547073461212L;

	public ShuffledCardsSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return EnvironmentEntityList.getList(context, Environment.SHUFFLED_CARDS_LIST).getCards(context, player);
	}
}
