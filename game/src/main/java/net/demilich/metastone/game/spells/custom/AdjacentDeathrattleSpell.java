package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.logic.TargetLogic;
import net.demilich.metastone.game.spells.AdjacentEffectSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.List;

/**
 * Casts {@link net.demilich.metastone.game.spells.desc.SpellArg#SPELL2} on the minions adjacent to the minion whose
 * deathrattle is currently being processed.
 */
public final class AdjacentDeathrattleSpell extends AdjacentEffectSpell {

	@Override
	protected List<Actor> getActors(GameContext context, SpellDesc desc, Entity source, Entity target) {
		if (desc.containsKey(SpellArg.BOARD_POSITION_ABSOLUTE)) {
			List<Actor> adjacentMinions = new ArrayList<>();
			List<Minion> minions = new ArrayList<>(context.getPlayer(target.getOwner()).getMinions());
			int index = desc.getInt(SpellArg.BOARD_POSITION_ABSOLUTE, -1);
			if (index == -1) {
				return TargetLogic.withoutPermanents(adjacentMinions);
			}
			minions.add(index, null);
			int left = index - 1;
			int right = index + 1;
			if (left > -1 && left < minions.size()) {
				adjacentMinions.add(minions.get(left));
			}
			if (right > -1 && right < minions.size()) {
				adjacentMinions.add(minions.get(right));
			}
			return TargetLogic.withoutPermanents(adjacentMinions);

		} else {
			return super.getActors(context, desc, source, target);
		}
	}
}
