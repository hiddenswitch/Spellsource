package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Destroys the {@code target} {@link Actor}.
 * <p>
 * Actors that are destroyed in this way do not get their hitpoints reduced to zero and are not dealt any damage. They
 * receive the {@link Attribute#DESTROYED} attribute, and during an {@link GameLogic#endOfSequence()}, they are moved to
 * the {@link com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones#GRAVEYARD} "not peacefully" (i.e., deathrattles will
 * trigger).
 * <p>
 * For example, to destroy all frozen minions:
 * <pre>
 *   {
 *     "class": "DestroySpell",
 *     "target": "ALL_MINIONS",
 *     "filter": {
 *       "class": "AttributeFilter",
 *       "attribute": "FROZEN",
 *       "operation": "HAS"
 *     }
 *   }
 * </pre>
 *
 * @see GameLogic#markAsDestroyed(Actor, Entity) for the underlying effect that adds the {@link Attribute#DESTROYED} attribute.
 * @see GameLogic#endOfSequence() for more about how minions, heroes and weapons are removed from play.
 */
public class DestroySpell extends Spell {
	public static Logger logger = LoggerFactory.getLogger(DestroySpell.class);

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
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc);
		if (target == null) {
			throw new UnsupportedOperationException("must specify a target");
		}
		// Give the source a kill if the target isn't already destroyed
		if (!target.isDestroyed()) {
			source.modifyAttribute(Attribute.TOTAL_KILLS, 1);
		}
		context.getLogic().markAsDestroyed((Actor) target, source);
	}
}

