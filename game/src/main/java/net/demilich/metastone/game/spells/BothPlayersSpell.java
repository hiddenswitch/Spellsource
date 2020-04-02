package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.spells.aura.HoardingWhelpAura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

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
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		// We'll cast once for the target acquisition that was processed here.
		super.cast(context, player, desc, source, targets);

		// We'll perform another target acquisition for the opposing player
		if (SpellUtils.getAuras(context, HoardingWhelpAura.class, source).isEmpty()) {
			player = context.getOpponent(player);
			var spellTarget = (EntityReference) context.getEnvironment().getOrDefault(Environment.TARGET, null);
			// We're going to use a null source action because the opposing player is not actually performing an action. This
			// prevents the opponent's target override effects from affecting how their point of view is cast.
			targets = context.getLogic().resolveTarget(player, source, spellTarget, desc, null).getTargets();
			super.cast(context, context.getOpponent(player), desc, source, targets);
		}
	}
}
