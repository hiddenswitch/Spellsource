package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.FightSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Causes the {@link EntityReference#TRIGGER_HOST} to fight a random minion adjacent to it.
 * <p>
 * Implements Blood Thirst.
 */
public final class FightRandomAdjacentMinionSpell extends FightSpell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		desc = desc.clone();
		Entity triggerHost = context.resolveSingleTarget(player, source, EntityReference.TRIGGER_HOST);
		if (triggerHost == null) {
			return;
		}
		desc.put(SpellArg.SECONDARY_TARGET, triggerHost.getReference());
		target = context.getLogic().getRandom(context.getAdjacentMinions(triggerHost.getReference()));
		if (target != null) {
			super.onCast(context, player, desc, source, target);
		}
	}
}

