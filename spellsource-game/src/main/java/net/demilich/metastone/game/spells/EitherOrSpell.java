package net.demilich.metastone.game.spells;

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

	protected void onCast(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			Condition condition,
			SpellDesc spell1,
			SpellDesc spell2,
			String card) {
		if (card != null) {
			spell1 = spell1.addArg(SpellArg.CARD, card);
			spell2 = spell2.addArg(SpellArg.CARD, card);
		}

		SpellDesc spellToCast = condition.isFulfilled(context, player, source, target) ? spell1 : spell2;
		SpellUtils.castChildSpell(context, player, spellToCast, source, target);
	}

}
