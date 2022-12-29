package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Receives a copy of all the cards stored on {@code source} by {@link StoreEntitySpell}.
 * <p>
 * Implements Primalfin Champion.
 *
 * @see CastOnCardsInStorageSpell for a more general way of performing actions on stored cards, including the base cards
 * of targeted minions.
 * @see CastOnEntitiesInStorageSpell for a more general way of performing actions on stored entities, which may be cards
 * or minions in the graveyard.
 */
public final class ReceiveCardsInStorageSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		CardList cards = new CardArrayList(EnvironmentEntityList.getList(context).getCards(context, source)).shuffle(context.getLogic().getRandom());
		cards.forEach(c -> context.getLogic().receiveCard(player.getId(), c.getCopy(), source));
		if (desc.containsKey(SpellArg.EXCLUSIVE)) {
			EnvironmentEntityList.getList(context).clear(source);
		}
	}
}