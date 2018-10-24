package net.demilich.metastone.game.spells.custom;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * For each {@code target}, destroy it. Once all the targets have been destroyed, end the sequence and trigger any
 * pending deathrattles. Then, for each destroyed target, call the summon spell specified by this {@link SpellDesc} with
 * a {@link SpellArg#BOARD_POSITION_ABSOLUTE} of the destroyed minion's prior location.
 */
public final class DestroyAndSummonInPlaceSpell extends SummonSpell {

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		Map<Integer, Integer> targetLocations = targets.stream()
				.collect(toMap(Entity::getId, e -> e.getEntityLocation().getIndex()));

		// First do all the destroys
		SpellDesc destroySpell = DestroySpell.create();
		destroySpell.create().cast(context, player, destroySpell, source, targets);

		// Clear the board
		context.getLogic().endOfSequence();
		// Then do all summons
		for (Entity target : targets) {
			SpellDesc singleSummon = desc.clone();
			singleSummon.put(SpellArg.BOARD_POSITION_ABSOLUTE, targetLocations.get(target.getId()));
			super.cast(context, player, singleSummon, source, Collections.singletonList(target));
		}
	}
}
