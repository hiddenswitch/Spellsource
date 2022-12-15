package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.SummonSpell;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * For each {@code target}, destroy it. Once all the targets have been destroyed, end the sequence and trigger any
 * pending deathrattles. Then, for each destroyed target, call the summon spell specified by this {@link SpellDesc} with
 * a {@link SpellArg#BOARD_POSITION_ABSOLUTE} of the destroyed minion's prior location.
 * <p>
 * If {@link SpellArg#HOW_MANY} is specified, up to that many random targets (after filtering) are destroyed.
 */
public final class DestroyAndSummonInPlaceSpell extends SummonSpell {

	@Override
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		Map<Integer, Integer> targetLocations = targets.stream()
				.collect(toMap(Entity::getId, e -> e.getEntityLocation().getIndex()));

		// Filter the targets
		targets = SpellUtils.getValidTargets(context, player, targets, desc.getEntityFilter(), source);

		// Select the number of random choices, if HOW_MANY is specified
		if (desc.containsKey(SpellArg.HOW_MANY)) {
			int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, null, source, 0);
			List<Entity> newTargets = new ArrayList<>();
			for (int i = 0; i < howMany && !targets.isEmpty(); i++) {
				newTargets.add(context.getLogic().removeRandom(targets));
			}
			targets = newTargets;
		}

		// First do all the destroys
		SpellDesc destroySpell = DestroySpell.create();
		destroySpell.create().cast(context, player, destroySpell, source, targets);

		// Clear the board
		context.getLogic().endOfSequence();
		// Then do all summons
		for (Entity target : targets) {
			SpellDesc singleSummon = desc.clone();
			singleSummon.remove(SpellArg.HOW_MANY);
			singleSummon.put(SpellArg.BOARD_POSITION_ABSOLUTE, targetLocations.get(target.getId()));
			super.cast(context, player, singleSummon, source, Collections.singletonList(target));
		}
	}
}
