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
import java.util.Collections;
import java.util.List;

/**
 * Casts {@link net.demilich.metastone.game.spells.desc.SpellArg#SPELL2} on the minions adjacent to the minion whose
 * deathrattle is currently being processed.
 * <p>
 * Required for implementing an effect that processes like {@link net.demilich.metastone.game.targeting.EntityReference#ADJACENT_MINIONS}
 * except from the point of view of a deathrattle, where the minion is typically in the graveyard.
 * <p>
 * For example, to implement "Deathrattle: Give adjacent minions Taunt.":
 * <pre>
 *   "deathrattle": {
 *     "class": "custom.AdjacentDeathrattleSpell",
 *     "target": "SELF",
 *     "spell2": {
 *       "class": "AddAttributeSpell",
 *       "attribute": "TAUNT"
 *     }
 *   }
 * </pre>
 */
public final class AdjacentDeathrattleSpell extends AdjacentEffectSpell {

	@Override
	protected List<Actor> getActors(GameContext context, SpellDesc desc, Entity source, Entity target) {
		if (desc.containsKey(SpellArg.BOARD_POSITION_ABSOLUTE)) {
			List<Actor> adjacentMinions = new ArrayList<>();
			List<Minion> minions = new ArrayList<>(context.getPlayer(target.getOwner()).getMinions());
			int index = desc.getInt(SpellArg.BOARD_POSITION_ABSOLUTE, -1);
			if (index == -1) {
				return Collections.emptyList();
			}
			if (index >= minions.size()) {
				return Collections.emptyList();
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
			// Exclude minions that are already destroyed.
			adjacentMinions.removeIf(Actor::isDestroyed);
			return TargetLogic.withoutPermanents(adjacentMinions);

		} else {
			return super.getActors(context, desc, source, target);
		}
	}
}
