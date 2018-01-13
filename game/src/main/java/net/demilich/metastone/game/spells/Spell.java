package net.demilich.metastone.game.spells;

import java.io.Serializable;
import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * The base class for "spells," or collections of effects in the engine.
 * <p>
 * Spell in this context means something broader than a spell card. It refers to anything that causes changes to the
 * game, like a deathrattle or a triggered effect.
 * <p>
 * To browse all the possible effects, visit the deriving classes of this class.
 */
public abstract class Spell implements Serializable {
	/**
	 * Casts a spell for the given arguments.
	 * <p>
	 * This spell casting code is responsible for interpreting the {@link SpellArg#FILTER} and {@link
	 * SpellArg#RANDOM_TARGET} attributes of a {@link SpellDesc}.
	 *
	 * @param context
	 * @param player
	 * @param desc
	 * @param source
	 * @param targets
	 * @see SpellUtils#getValidTargets(GameContext, Player, List, EntityFilter) for the logic which filters the targets
	 * argument.
	 */
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		// no target specified, cast the spell once with target NULL
		if (targets == null) {
			castForPlayer(context, player, desc, source, null);
			return;
		}

		EntityFilter targetFilter = desc.getEntityFilter();
		List<Entity> validTargets = SpellUtils.getValidTargets(context, player, targets, targetFilter);
		// there is at least one valid target and the RANDOM_TARGET flag is set,
		// pick one randomly
		if (validTargets.size() > 0 && desc.getBool(SpellArg.RANDOM_TARGET)) {
			Entity target = context.getLogic().getRandom(validTargets);
			castForPlayer(context, player, desc, source, target);
		} else {
			// there is at least one target and RANDOM_TARGET flag is not set,
			// cast in on all targets

			for (Entity target : validTargets) {
				final EntityReference reference = target == null ? EntityReference.NONE : target.getReference();
				context.getSpellTargetStack().push(reference);
				castForPlayer(context, player, desc, source, target);
				context.getSpellTargetStack().pop();
			}
		}
	}

	@Suspendable
	private void castForPlayer(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		TargetPlayer targetPlayer = desc.getTargetPlayer();
		if (targetPlayer == null) {
			targetPlayer = TargetPlayer.SELF;
		}
		Player opponent = context.getOpponent(player);
		switch (targetPlayer) {
			case BOTH:
				onCast(context, player, desc, source, target);
				onCast(context, opponent, desc, source, target);
				break;
			case OPPONENT:
				onCast(context, opponent, desc, source, target);
				break;
			case SELF:
				onCast(context, player, desc, source, target);
				break;
			case OWNER:
				onCast(context, context.getPlayer(target.getOwner()), desc, source, target);
				break;
			case ACTIVE:
				onCast(context, context.getActivePlayer(), desc, source, target);
				break;
			case INACTIVE:
				onCast(context, context.getOpponent(context.getActivePlayer()), desc, source, target);
				break;
			default:
				break;
		}
	}

	@Suspendable
	protected abstract void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target);

	@Override
	public String toString() {
		return "[SPELL " + getClass().getSimpleName() + "]";
	}
}
