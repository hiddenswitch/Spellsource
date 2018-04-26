package net.demilich.metastone.game.spells;

import java.util.Map;
import java.util.function.Predicate;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Destroys the {@code target} {@link Actor}.
 * <p>
 * Actors that are destroyed in this way do not get their hitpoints reduced to zero and are not dealt any damage. They
 * receive the {@link net.demilich.metastone.game.utils.Attribute#DESTROYED} attribute, and during an {@link
 * GameLogic#endOfSequence()}, they are moved to the {@link net.demilich.metastone.game.targeting.Zones#GRAVEYARD} "not
 * peacefully" (i.e., deathrattles will trigger).
 *
 * @see GameLogic#markAsDestroyed(Actor) for the underlying effect that adds the {@link
 * net.demilich.metastone.game.utils.Attribute#DESTROYED} attribute.
 * @see GameLogic#endOfSequence() for more about how minions, heroes and weapons are removed from play.
 */
public class DestroySpell extends Spell {

	public static SpellDesc create() {
		return create(null);
	}

	public static SpellDesc create(EntityReference target) {
		return create(target, false);
	}

	public static SpellDesc create(EntityReference target, boolean randomTarget) {
		return create(target, null, randomTarget);
	}

	public static SpellDesc create(EntityReference target, Predicate<Entity> targetFilter, boolean randomTarget) {
		Map<SpellArg, Object> arguments = new SpellDesc(DestroySpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.RANDOM_TARGET, randomTarget);
		if (targetFilter != null) {
			arguments.put(SpellArg.FILTER, targetFilter);
		}
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		context.getLogic().markAsDestroyed((Actor) target);
	}

}

