package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Casts {@link SpellArg#SPELL1} if {@link SpellArg#CONDITION} is fulfilled, otherwise, casts {@link SpellArg#SPELL2}.
 * <p>
 * The condition is evaluated with respect to the {@code target}.
 * <p>
 * For example, to implement the text, "Deal 8 damage to a minion. If it's a friendly Demon, give it +8/+8 instead.":
 * <pre>
 *   {
 *     "class": "EitherOrSpell",
 *     "condition": {
 *       "class": "AndCondition",
 *       "conditions": [
 *         {
 *           "class": "OwnedByPlayerCondition",
 *           "targetPlayer": "SELF"
 *         },
 *         {
 *           "class": "RaceCondition",
 *           "race": "DEMON"
 *         }
 *       ]
 *     },
 *     "spell1": {
 *       "class": "BuffSpell",
 *       "attackBonus": 8,
 *       "hpBonus": 8
 *     },
 *     "spell2": {
 *       "class": "DamageSpell",
 *       "value": 8
 *     }
 *   }
 * </pre>
 */
public class EitherOrSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(EitherOrSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.CONDITION, SpellArg.SPELL1, SpellArg.SPELL2);
		Condition condition = (Condition) desc.get(SpellArg.CONDITION);
		SpellDesc either = (SpellDesc) desc.get(SpellArg.SPELL1);
		SpellDesc or = (SpellDesc) desc.get(SpellArg.SPELL2);
		if (desc.containsKey(SpellArg.CARD)) {
			either.put(SpellArg.CARD, desc.get(SpellArg.CARD));
			or.put(SpellArg.CARD, desc.get(SpellArg.CARD));
		}

		SpellDesc spellToCast = condition.isFulfilled(context, player, source, target) ? either : or;
		SpellUtils.castChildSpell(context, player, spellToCast, source, target);
	}

}
