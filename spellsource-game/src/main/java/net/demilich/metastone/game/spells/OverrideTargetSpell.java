package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Map;

/**
 * Overrides the target of the next {@link net.demilich.metastone.game.logic.GameLogic#targetAcquisition(Player, Entity, GameAction)}  call.
 * <p>
 * Overrides are always cleared at the end of evaluating a game action (not necessarily at the end of a sequence).
 * <p>
 * Use in combination with a {@link net.demilich.metastone.game.spells.trigger.TargetAcquisitionTrigger} to override
 * player-chosen targets. For <b>example</b>, this code will implement the text, "Whenever a Fireball is cast, cast it
 * instead on the enemy hero."
 * <pre>
 *   "trigger": {
 *     "eventTrigger": {
 *       "class": "TargetAcquisitionTrigger",
 *       "actionType": "SPELL",
 *       "fireCondition": {
 *         "class": "CardPropertyCondition",
 *         "target": "EVENT_SOURCE",
 *         "card": "spell_fireball"
 *       },
 *       "sourcePlayer": "BOTH",
 *       "targetPlayer": "BOTH"
 *     },
 *     "spell": {
 *       "class": "OverrideTargetSpell",
 *       "target": "ENEMY_HERO"
 *     }
 *   }
 * </pre>
 */
public class OverrideTargetSpell extends Spell {

	public static SpellDesc create() {
		Map<SpellArg, Object> arguments = new SpellDesc(OverrideTargetSpell.class);
		return new SpellDesc(arguments);
	}

	public static SpellDesc create(EntityReference override) {
		return new SpellDesc(OverrideTargetSpell.class, override, null, false);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		context.getEnvironment().putIfAbsent(Environment.TARGET_OVERRIDE, target.getReference());
	}
}
