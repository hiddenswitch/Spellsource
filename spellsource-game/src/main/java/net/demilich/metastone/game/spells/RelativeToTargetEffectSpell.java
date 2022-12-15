package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This abstract class provides a way to cast spell effects on targets.
 *
 * <ul>
 * <li>{@link SpellArg#SPELL1} is cast on {@code target}.</li>
 * <li>{@link SpellArg#SPELL2} is cast on the result of {@link #getActors(GameContext, SpellDesc, Entity, Entity)} of that {@code
 * target}.</li>
 * <li>{@link SpellArg#SPELL} is cast on both, only if neither of the above were specified.</li>
 * </ul>
 */
public abstract class RelativeToTargetEffectSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(AdjacentEffectSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.SPELL, SpellArg.SPELL1, SpellArg.SPELL2);
		EntityReference sourceReference = source != null ? source.getReference() : null;
		List<Actor> adjacentMinions = getActors(context, desc, source, target);
		SpellDesc primary;
		SpellDesc secondary;
		if (desc.containsKey(SpellArg.SPELL) &&
				!desc.containsKey(SpellArg.SPELL1)
				&& !desc.containsKey(SpellArg.SPELL2)) {
			primary = (SpellDesc) desc.get(SpellArg.SPELL);
			secondary = primary;
		} else {
			primary = (SpellDesc) desc.get(SpellArg.SPELL1);
			secondary = (SpellDesc) desc.get(SpellArg.SPELL2);
			if (secondary == null) {
				secondary = primary;
			}
		}

		if (primary != null) {
			SpellUtils.castChildSpell(context, player, primary, source, target);
		}

		for (Entity adjacent : adjacentMinions) {
			SpellUtils.castChildSpell(context, player, secondary, source, adjacent);
		}
	}

	protected abstract List<Actor> getActors(GameContext context, SpellDesc desc, Entity source, Entity target);
}
