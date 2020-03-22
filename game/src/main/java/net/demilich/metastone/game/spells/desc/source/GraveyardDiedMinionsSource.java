package net.demilich.metastone.game.spells.desc.source;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;

import java.util.stream.Collectors;

/**
 * Returns a copy of the source cards of minions that {@link Entity#diedOnBattlefield()} belonging to the casting {@code
 * player}.
 *
 * @see Entity#diedOnBattlefield()
 */
public class GraveyardDiedMinionsSource extends CardSource {

	public GraveyardDiedMinionsSource(CardSourceDesc desc) {
		super(desc);
	}

	@Override
	protected CardList match(GameContext context, Entity source, Player player) {
		return player
				.getGraveyard()
				.stream()
				.filter(Entity::diedOnBattlefield)
				.map(Entity::getSourceCard)
				.map(Card::getCopy)
				.collect(Collectors.toCollection(CardArrayList::new));
	}
}
