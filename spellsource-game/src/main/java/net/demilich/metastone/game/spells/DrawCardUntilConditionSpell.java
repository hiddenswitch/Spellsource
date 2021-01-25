package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Draws cards until a condition is met.
 * <p>
 * In other words, this spell implements {@link DrawCardSpell} with a {@link SpellArg#CONDITION}. If the condition is
 * met, drawing stops.
 * <p>
 * For example, to keep drawing cards until a dragon is drawn:
 * <pre>
 *   {
 *     "class": "DrawCardUntilConditionSpell",
 *     "condition": {
 *       "class": "RaceCondition",
 *       "race": "DRAGON"
 *     }
 *   }
 * </pre>
 */
public final class DrawCardUntilConditionSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(DrawCardUntilConditionSpell.class);

	@Override
	@Suspendable
	public void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.CONDITION, SpellArg.VALUE);
		int cardCount = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		Condition condition = (Condition) desc.get(SpellArg.CONDITION);
		for (int i = 0; i < cardCount; i++) {
			Card card = context.getLogic().drawCard(player.getId(), source);
			if (card == null || (condition != null && condition.isFulfilled(context, player, source, card))) {
				return;
			}
		}
	}
}
