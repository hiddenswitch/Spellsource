package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Restores {@link net.demilich.metastone.game.spells.desc.SpellArg#VALUE} health up to the {@code target} entity's
 * {@link net.demilich.metastone.game.cards.Attribute#MAX_HP} without triggering any enchantments.
 */
public final class RestoreHealthPeacefullySpell extends Spell {
	private static Logger LOGGER = LoggerFactory.getLogger(RestoreHealthPeacefullySpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (!(target instanceof Actor)) {
			LOGGER.error("onCast {} {}: Trying to restore health to non-actor target {}", context.getGameId(), source, target);
			return;
		}
		Actor actor = (Actor) target;
		actor.setHp(Math.min(actor.getHp() + desc.getValue(SpellArg.VALUE, context, player, target, source, 0), actor.getMaxHp()));
	}
}
