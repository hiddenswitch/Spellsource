package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * This spell implements the new Overkill mechanic introduced in Rastakhan's Rumble. It's meant to happen when something
 * kills a minion by dealing more damage than it has health.
 * <p>
 * The spell can serve both as a {@link DamageSpell} if it has a {@link SpellArg#VALUE} in its spelldesc, or just simply
 * as a way to simply check the state of a minion and fire the spell appropriately
 * <p>
 *
 * <b>Example:</b> on a minion with "Overkill: Draw 2 Cards":
 * <pre>
 *   "trigger": {
 *     "eventTrigger": {
 *     	  "class": "DamageCausedTrigger",
 *       "hostTargetType": "IGNORE_OTHER_SOURCES"
 *     },
 *     "spell": {
 *       "class": "OverkillSpell",
 *       "spell": {
 *         "class": "DrawCardSpell",
 *         "value": 2
 *       }
 *     }
 *   }
 * </pre>
 * <b>Example:</b> on a Spell with "Deal 3 damage. Overkill: Summon a 5/5 Devilsaur."
 * <pre>
 *   "spell": {
 *     "class": "OverkillSpell",
 *     "value": 3,
 *     "spell": {
 *       "class": "SummonSpell",
 *       "card": "token_devilsaur"
 *     }
 *   }
 * </pre>
 * <p>
 * If the desc has the {@link SpellArg#EXCLUSIVE} arg set to true, the excess damage will be passed into the spell's
 * {@link SpellArg#VALUE}
 */

public class OverkillSpell extends DamageSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (desc.containsKey(SpellArg.VALUE)) {
			// Allow the OverkillSpell to serve as a damage spell as well
			super.onCast(context, player, desc, source, target);
		}
		if (target == null) {
			target = context.resolveSingleTarget(player, source, EntityReference.EVENT_TARGET);
		}
		if (target instanceof Minion) {
			Minion minion = (Minion) target;
			if (minion.getHp() < 0 && minion.isDestroyed()) {
				//fire an overkill event?
				SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
				if (desc.getBool(SpellArg.EXCLUSIVE)) {
					spell.put(SpellArg.VALUE, Math.abs(minion.getHp()));
				}
				SpellUtils.castChildSpell(context, player, spell, source, target);
			}
		}
	}
}
