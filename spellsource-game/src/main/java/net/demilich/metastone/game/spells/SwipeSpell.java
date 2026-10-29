package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Deals {@link SpellArg#VALUE} damage to the {@code target} and {@link SpellArg#SECONDARY_VALUE} damage to the {@link
 * SpellArg#SECONDARY_TARGET} actors.
 * <p>
 * Implements Swipe.
 */
public class SwipeSpell extends Spell {

	protected void onCast(
			GameContext context,
			Player player,
			Entity source,
			Entity target,
			@Default(2) int value,
			@Default(1) int secondaryValue,
			@Default(string = "ENEMY_CHARACTERS") EntityReference secondaryTarget) {
		for (Entity entity : context.resolveTarget(player, target, secondaryTarget)) {
			if (!entity.equals(target)) {
				context.getLogic().damage(player, (Actor) entity, secondaryValue, source);
			}
		}

		context.getLogic().damage(player, (Actor) target, value, source);
	}

}
