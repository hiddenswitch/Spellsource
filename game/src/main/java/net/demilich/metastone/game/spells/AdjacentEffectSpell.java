package net.demilich.metastone.game.spells;

import java.util.List;
import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Casts {@link SpellArg#SPELL1} on the {@code target} minion, and {@link SpellArg#SPELL2} on the minions adjacent to
 * the target.
 * <p>
 * Used to implement text like, "Also damages the minions next to whomever this attacks":
 * <pre>
 *   "trigger": {
 *     "eventTrigger": {
 *       "class": "AfterPhysicalAttackTrigger",
 *       "hostTargetType": "IGNORE_OTHER_SOURCES",
 *       "targetEntityType": "MINION"
 *     },
 *     "spell": {
 *       "class": "AdjacentEffectSpell",
 *       "target": "EVENT_TARGET",
 *       "spell2": {
 *         "class": "DamageSpell",
 *         "value": {
 *           "class": "AttributeValueProvider",
 *           "target": "SELF",
 *           "attribute": "ATTACK"
 *         }
 *       }
 *     }
 *   }
 * </pre>
 * Observe that "damaging next to" is implemented by querying this minion's attack and dealing damage to adjacent
 * minions like a damage spell.
 */
public class AdjacentEffectSpell extends Spell {

	public static SpellDesc create(EntityReference target, SpellDesc primarySpell, SpellDesc secondarySpell) {
		Map<SpellArg, Object> arguments = new SpellDesc(AdjacentEffectSpell.class);
		if (primarySpell != null) {
			arguments.put(SpellArg.SPELL1, primarySpell);
		}
		if (secondarySpell != null) {
			arguments.put(SpellArg.SPELL2, secondarySpell);
		}
		if (primarySpell == null && secondarySpell == null) {
			throw new IllegalArgumentException("Both primary- and secondary spell are NULL; at least one of them must be set");
		}
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(SpellDesc primarySpell, SpellDesc secondarySpell) {
		return create(null, primarySpell, secondarySpell);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityReference sourceReference = source != null ? source.getReference() : null;
		List<Actor> adjacentMinions = context.getAdjacentMinions(target.getReference());

		SpellDesc primary = (SpellDesc) desc.get(SpellArg.SPELL1);
		if (primary != null) {
			context.getLogic().castSpell(player.getId(), primary, sourceReference, target.getReference(), true);
		}

		SpellDesc secondary = (SpellDesc) desc.get(SpellArg.SPELL2);
		if (secondary == null) {
			secondary = primary;
		}
		for (Entity adjacent : adjacentMinions) {
			context.getLogic().castSpell(player.getId(), secondary, sourceReference, adjacent.getReference(), true);
		}
	}

}