package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

/**
 * Forces the leftmost and rightmost enemy minions to attack their neighbors.
 * <p>
 * Implements Sow Distrust.
 */
public final class SowDistrustSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Entity> opposingMinions = context.resolveTarget(player, source, EntityReference.ENEMY_MINIONS);
		if (opposingMinions.size() < 2) {
			return;
		}

		context.getLogic().fight(player, (Actor) opposingMinions.get(0), (Actor) opposingMinions.get(1), null);
		context.getLogic().fight(player, (Actor) opposingMinions.get(opposingMinions.size() - 1), (Actor) opposingMinions.get(opposingMinions.size() - 2), null);
	}
}
