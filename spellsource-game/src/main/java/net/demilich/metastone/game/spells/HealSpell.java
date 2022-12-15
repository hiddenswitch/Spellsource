package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Heals the specified {@code target} for {@link SpellArg#VALUE} amount.
 * <p>
 * Negative healing does not cause damage.
 * <p>
 * Healing raises a {@link net.demilich.metastone.game.events.HealEvent} and can be reacted to with the {@link
 * net.demilich.metastone.game.spells.trigger.HealingTrigger}.
 * <p>
 * For <b>example</b>, to fully heal a minion, heal up to its maximum hitpoints:
 * <pre>
 *   {
 *     "class": "HealSpell",
 *     "value": {
 *       "class": "AttributeValueProvider",
 *       "target": "TARGET",
 *       "attribute": "MAX_HP"
 *     }
 *   }
 * </pre>
 *
 * @see HealingMissilesSpell for the missiles version of this spell.
 * @see net.demilich.metastone.game.logic.GameLogic#heal(Player, Actor, int, Entity, boolean) for the full healing
 * 		rules.
 */
public class HealSpell extends Spell {

	public static SpellDesc create(EntityReference target, int healing) {
		return create(target, healing, false);
	}

	public static SpellDesc create(EntityReference target, int healing, boolean randomTarget) {
		Map<SpellArg, Object> arguments = new SpellDesc(HealSpell.class);
		arguments.put(SpellArg.VALUE, healing);
		arguments.put(SpellArg.TARGET, target);
		if (randomTarget) {
			arguments.put(SpellArg.RANDOM_TARGET, true);
			arguments.put(SpellArg.FILTER, (Predicate<Entity>) entity -> ((Actor) entity).isWounded());
		}
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(int healing) {
		return create(null, healing);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int healing = getHealing(context, player, desc, source, target);
		context.getLogic().heal(player, (Actor) target, Math.max(0, healing), source);
	}

	protected int getHealing(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		return desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
	}
}

