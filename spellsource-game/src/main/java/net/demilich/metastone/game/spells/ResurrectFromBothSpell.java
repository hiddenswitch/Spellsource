package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Resurrects minions from both player's graveyards.
 *
 * If a {@link SpellArg#CARD_FILTER} is specified, only resurrect minions that satisfy the filter.
 * <p>
 * Does not resurrect unique minions.
 */
public class ResurrectFromBothSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Minion> deadMinions = new ArrayList<>();
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		List<Entity> bothGraveyards = new ArrayList<Entity>();
		bothGraveyards.addAll(player.getGraveyard());
		bothGraveyards.addAll(context.getOpponent(player).getGraveyard());
		for (Entity deadEntity : bothGraveyards) {
			if (deadEntity.diedOnBattlefield()) {
				if (cardFilter == null || cardFilter.matches(context, player, deadEntity, source)) {
					deadMinions.add((Minion) deadEntity);
				}
			}
		}
		int count = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < count; i++) {
			if (deadMinions.isEmpty()) {
				return;
			}
			Minion resurrectedMinion = context.getLogic().getRandom(deadMinions);
			Card card = resurrectedMinion.getSourceCard();
			context.getLogic().summon(player.getId(), card.minion(), source, -1, false);
			deadMinions.remove(resurrectedMinion);
		}
	}

}
