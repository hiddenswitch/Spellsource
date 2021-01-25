package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.DuelSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.Collections;
import java.util.List;

/**
 * Like {@link DuelSpell}, except a random attacker that is not a defender is chosen from the {@link
 * net.demilich.metastone.game.spells.desc.SpellArg#SECONDARY_TARGET}. <b>Ignores its filter.</b>
 * <p>
 * Implements One on One.
 */
public final class DuelRandomSecondarySpell extends DuelSpell {

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		List<Entity> validDefenders = targets;
		if (validDefenders == null || validDefenders.isEmpty()) {
			return;
		}
		List<Entity> validAttackers = context.resolveTarget(player, source, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET));
		validAttackers.removeAll(validDefenders);
		if (validAttackers.isEmpty()) {
			return;
		}
		validAttackers = Collections.singletonList(context.getLogic().getRandom(validAttackers));
		duel(context, player, source, validAttackers, validDefenders);
	}
}
