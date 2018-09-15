package net.demilich.metastone.game.spells;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Gives the {@code target} the {@link SpellArg#SPELL} as a deathrattle.
 * <p>
 * Resolves the {@link SpellArg#SECONDARY_TARGET} and puts it as the {@link SpellArg#TARGET} of the {@link
 * SpellArg#SPELL} (the deathrattle).l
 *
 * @see AddDeathrattleSpell for more about adding deathrattles.
 */
public final class AddDeathrattleSecondaryAsTargetSpell extends AddDeathrattleSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		desc = desc.clone();
		SpellDesc deathrattle = desc.getSpell();
		deathrattle.put(SpellArg.TARGET, context.resolveSingleTarget(player, source, desc.getSecondaryTarget()).getReference());
		super.onCast(context, player, desc, source, target);
	}
}
