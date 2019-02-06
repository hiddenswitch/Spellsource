package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.trigger.Trigger;

import java.util.Map;

/**
 * Transforms the {@code source} (casting entity) of this spell into the {@code target} {@link Minion}.
 * <p>
 * The transformation includes all of the {@link Trigger} objects that are associated with (hosted by) the {@code
 * target}.
 * <p>
 * This spell can be used to interrupt summons.
 * <p>
 * The transformation does not count as a summon effect.
 * <p>
 * For <b>example</b>, to transform into a target minion in a battlecry:
 * <pre>
 *     {
 *         "targetSelection": "MINIONS",
 *         "spell": {
 *             "class": "CopyMinionSpell"
 *         }
 *     }
 * </pre>
 * To transform into a random minion on the battlefield:
 * <pre>
 *     {
 *         "spell": "CopyMinionSpell",
 *         "target": "ALL_OTHER_MINIONS",
 *         "randomTarget": true
 *     }
 * </pre>
 *
 * @see net.demilich.metastone.game.logic.GameLogic#transformMinion(Minion, Minion) for more about transformations.
 */
public final class CopyMinionSpell extends Spell {

	/**
	 * Creates this spell. The {@code target} will be passed down by a parent invocation.
	 *
	 * @return The spell
	 */
	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(CopyMinionSpell.class);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		Minion clone = ((Minion) target).getCopy();
		clone.clearEnchantments();
		clone.setCardCostModifier(null);
		context.getLogic().transformMinion((Minion) source, clone);

		boolean didTransform = context.getEnvironment().containsKey(Environment.TRANSFORM_REFERENCE);

		if (didTransform) {
			for (Trigger trigger : context.getTriggersAssociatedWith(target.getReference())) {
				Trigger triggerClone = trigger.clone();
				context.getLogic().addGameEventListener(player, triggerClone, clone);
			}
		}
	}
}