package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Implements "50% chance to target the wrong entity" when coupled with an appropriate trigger. The {@code target} is
 * interpreted as the intended target of the attack.
 * <p>
 * For example, to implement a minion that has a 50% chance to target the wrong enemy::
 * <pre>
 *   "trigger": {
 *     "eventTrigger": {
 *       "class": "TargetAcquisitionTrigger",
 *       "actionType": "PHYSICAL_ATTACK",
 *       "hostTargetType": "IGNORE_OTHER_SOURCES",
 *       "queueCondition": {
 *         "class": "RandomCondition"
 *       }
 *     },
 *     "spell": {
 *       "class": "FumbleSpell",
 *       "target": "EVENT_TARGET"
 *     }
 *   }
 * </pre>
 */
public class FumbleSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Actor attacker = (Actor) context.resolveSingleTarget(context.getAttackerReferenceStack().peek());
		Actor randomTarget = context.getLogic().getAnotherRandomTarget(context.getActivePlayer(), attacker, (Actor) target,
				EntityReference.ENEMY_CHARACTERS);
		if (randomTarget != target) {
			context.getEnvironment().put(Environment.TARGET_OVERRIDE, randomTarget.getReference());
		}
	}

}
