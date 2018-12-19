package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.BuffAura;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.valueprovider.ValueProvider;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;

import java.util.Map;

/**
 * @deprecated Use a {@link BuffAura} instead. For example, to give a minion +1 attack only if the friendly player has
 * 		more than 3 cards:
 * 		<pre>
 * 				  "aura": {
 * 				    "class": "BuffAura",
 * 				    "attackBonus": 1,
 * 				    "hpBonus": 0,
 * 				    "target": "SELF",
 * 				    "condition": {
 * 				      "class": "CardCountCondition",
 * 				      "targetPlayer": "SELF",
 * 				      "operation": "GREATER",
 * 				      "value": 3
 * 				    }
 * 				  }
 * 				</pre>
 * 		<p>
 * 		Gives a minion an attack bonus with a given condition.
 */
@Deprecated
public class ConditionalAttackBonusSpell extends Spell {

	public static SpellDesc create(EntityReference target, ValueProvider valueProvider) {
		Map<SpellArg, Object> arguments = new SpellDesc(ConditionalAttackBonusSpell.class);
		arguments.put(SpellArg.VALUE, valueProvider);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(ValueProvider valueProvider) {
		return create(null, valueProvider);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int attackBonus = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		target.setAttribute(Attribute.CONDITIONAL_ATTACK_BONUS, attackBonus);
	}
}
