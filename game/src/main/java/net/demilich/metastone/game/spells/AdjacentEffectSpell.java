package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;
import java.util.Map;

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
 *
 * @see RelativeToTargetEffectSpell for an abstract base class that can be used to cast spells relative to a {@code
 * 		target}.
 * @see EntityReference#ADJACENT_MINIONS to directly target minions adjacent to the {@code source} (i.e., the result of
 * 		{@link EntityReference#SELF}).
 * @see EntityReference#ADJACENT_TO_TARGET to direct target minions adjacent to the resolution of {@link
 * 		EntityReference#TARGET}, the currently selected player target.
 */
public class AdjacentEffectSpell extends RelativeToTargetEffectSpell {

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
	protected List<Actor> getActors(GameContext context, SpellDesc desc, Entity source, Entity target) {
		return context.getAdjacentMinions(target.getReference());
	}
}