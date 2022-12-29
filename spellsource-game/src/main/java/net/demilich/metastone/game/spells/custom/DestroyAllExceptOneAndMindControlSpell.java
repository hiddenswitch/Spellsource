package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.DestroyAllExceptOneSpell;
import net.demilich.metastone.game.spells.DestroySpell;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Destroys all minions except one. Then mind controls it.
 */
public final class DestroyAllExceptOneAndMindControlSpell extends DestroySpell {

	public static Logger logger = LoggerFactory.getLogger(DestroyAllExceptOneSpell.class);

	@Override
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		if (targets == null || targets.isEmpty()) {
			return;
		}
		EntityFilter filter = desc.getEntityFilter();
		List<Entity> destroyedTargets = new ArrayList<>(targets);
		List<Entity> potentialSurvivors = SpellUtils.getValidTargets(context, player, destroyedTargets, filter, source);
		if (!potentialSurvivors.isEmpty()) {
			Entity randomTarget = context.getLogic().getRandom(potentialSurvivors);
			destroyedTargets.remove(randomTarget);
			Minion minion = (Minion) randomTarget;
			context.getLogic().mindControl(player, minion, source);
		}

		for (Entity entity : destroyedTargets) {
			onCast(context, player, null, null, entity);
		}
	}

}
