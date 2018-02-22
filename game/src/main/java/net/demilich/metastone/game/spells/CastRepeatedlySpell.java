package net.demilich.metastone.game.spells;

import java.util.List;
import java.util.Map;

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
 * Casts the specified {@link SpellArg#SPELL} for {@link SpellArg#HOW_MANY} times. If a {@link SpellArg#CONDITION} is
 * specified, the condition is evaluated after the first cast; if the condition is not {@link
 * Condition#isFulfilled(GameContext, Player, Entity, Entity)}, the casting stops.
 * <p>
 * If this spell's invocation has a non-null {@code target}, the sub spell will be cast with a random target in this
 * spell's {@link SpellArg#TARGET} property. This surprising behaviour reflects a consequence of legacy Metastone code.
 * <p>
 * This spell will <b>not</b> end the sequence after every repeat, while {@link RecastWhileSpell} does.
 *
 * @see RecastWhileSpell for a more appropriate way to cast a spell multiple times with a condition.
 * @see ForceDeathPhaseSpell to see how to cause the end of a sequence and clean dead minions off the battlefield.
 */
public class CastRepeatedlySpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(CastRepeatedlySpell.class);

	public static SpellDesc create(SpellDesc spell, int value) {
		Map<SpellArg, Object> arguments = SpellDesc.build(CastRandomSpellSpell.class);
		arguments.put(SpellArg.SPELL, spell);
		arguments.put(SpellArg.HOW_MANY, value);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.HOW_MANY, SpellArg.CONDITION);
		int iterations = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 1);
		SpellDesc spell = (SpellDesc) desc.get(SpellArg.SPELL);
		Condition condition = (Condition) desc.get(SpellArg.CONDITION);

		for (int i = 0; i < iterations; i++) {
			if (target == null) {
				logger.debug("onCast {} {}: A null target argument was provided, recasting with a null target.", context.getGameId(), source);
				SpellUtils.castChildSpell(context, player, spell, source, null);
				if (condition != null && condition.isFulfilled(context, player, source, null)) {
					return;
				}
			} else {
				List<Entity> targets = context.resolveTarget(player, source, desc.getTarget());
				if (targets.isEmpty()) {
					return;
				}
				Entity randomTarget = context.getLogic().getRandom(targets);
				SpellUtils.castChildSpell(context, player, spell, source, randomTarget);
				if (condition != null && condition.isFulfilled(context, player, source, randomTarget)) {
					return;
				}
			}

		}
	}

}
