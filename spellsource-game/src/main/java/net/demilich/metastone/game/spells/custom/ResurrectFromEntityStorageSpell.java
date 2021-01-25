package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Resurrects and clears the entities stored on the {@code source}.
 * <p>
 * Implements Frostmourne. However, Frostmourne's effect should really be adding deathrattles to it.
 *
 * @see CastOnCardsInStorageSpell for a more general way of performing actions on stored cards, including the base cards
 * of targeted minions.
 * @see CastOnEntitiesInStorageSpell for a more general way of performing actions on stored entities, which may be cards
 * or minions in the graveyard.
 */
public class ResurrectFromEntityStorageSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(ResurrectFromEntityStorageSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		// Snapshot the entity storage, do not use the transform
		var resurrect = new ArrayList<>(EnvironmentEntityList.getList(context).getCards(context, source));
		Collections.shuffle(resurrect, context.getLogic().getRandom());
		int i = 0;

		while (context.getLogic().canSummonMoreMinions(player)
				&& i < resurrect.size()) {
			Card card = resurrect.get(i).getCopy();
			card.setId(context.getLogic().generateId());
			card.setOwner(player.getId());
			card.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
			if (card.getCardType() == CardType.MINION) {
				context.getLogic().summon(player.getId(), card.minion(), source, -1, false);
			} else {
				logger.warn("onCast {} {}: Trying to resurrect {} from entity storage, which is not a minion", context.getGameId(), source, card);
			}
			card.moveOrAddTo(context, Zones.REMOVED_FROM_PLAY);
			context.getLogic().removeCard(card);
			i++;
		}
	}
}