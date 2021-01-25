package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

import java.util.stream.Collectors;

/**
 * Returns the list of source cards for the minions on the battlefield of the {@link CardSourceArg#TARGET_PLAYER}.
 */
public final class MinionsSource extends CardSource implements HasCardCreationSideEffects {

	public MinionsSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return player.getMinions()
				.stream()
				.map(Entity::getSourceCard)
				.map(Card::getCopy)
				.collect(Collectors.toCollection(CardArrayList::new));
	}
}
