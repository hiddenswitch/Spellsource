package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.aura.HoardingWhelpAura;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Indicates that the effects should occur for both players, <b>without</b> using the {@link TargetPlayer#BOTH} value in
 * {@link net.demilich.metastone.game.spells.desc.SpellArg#TARGET_PLAYER}.
 * <p>
 * The target is resolved from the point of view of each player.
 * <p>
 * This can be overriden by the {@link net.demilich.metastone.game.spells.aura.HoardingWhelpAura}.
 */
public final class BothPlayersSpell extends MetaSpell {

	@Override
	@Suspendable
	protected void each(GameContext context, Player player, Entity source, Entity target, SpellDesc spell) {
		// Casts the spell for both players, unless otherwise specified.
		super.each(context, player, source, target, spell);
		if (SpellUtils.getAuras(context, HoardingWhelpAura.class, source).isEmpty()) {
			super.each(context, context.getOpponent(player), source, target, spell);
		}
	}
}
