package net.demilich.metastone.game.spells;

import java.util.ArrayList;
import java.util.List;

import com.github.fromage.quasi.fibers.Suspendable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;

/**
 * Destroys all minions except one. Respects {@link net.demilich.metastone.game.spells.desc.SpellArg#FILTER} if
 * specified.
 * <p>
 * Implements Brawl.
 */
public class DestroyAllExceptOneSpell extends DestroySpell {

	public static Logger logger = LoggerFactory.getLogger(DestroyAllExceptOneSpell.class);

	@Override
	@Suspendable
	public void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets) {
		// No additional args.
		checkArguments(logger, context, source, desc);
		if (targets == null || targets.isEmpty()) {
			return;
		}
		EntityFilter filter = desc.getEntityFilter();
		List<Entity> destroyedTargets = new ArrayList<Entity>(targets);
		List<Entity> potentialSurvivors = SpellUtils.getValidTargets(context, player, destroyedTargets, filter, source);
		if (!potentialSurvivors.isEmpty()) {
			Entity randomTarget = context.getLogic().getRandom(potentialSurvivors);
			destroyedTargets.remove(randomTarget);
		}

		for (Entity entity : destroyedTargets) {
			onCast(context, player, null, null, entity);
		}
	}
}
