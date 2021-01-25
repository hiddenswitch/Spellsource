package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Increments and deals fatigue damage to the {@code player} ({@link SpellArg#TARGET_PLAYER}).
 */
public final class FatigueSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int times = desc.getValue(SpellArg.VALUE, context, player, target, source, 1);
		for (int i = 0; i < times; i++) {
			context.getLogic().dealFatigueDamage(player);
		}
	}
}
