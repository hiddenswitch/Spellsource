package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.ReturnTargetToHandSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Records the {@code target} minion's deathrattles. Returns the minion to hand. Triggers those deathrattles from the
 * target's old location.
 */
public final class SleightOfHandSpell extends ReturnTargetToHandSpell {

	private static Logger LOGGER = LoggerFactory.getLogger(SleightOfHandSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target.getEntityType() != EntityType.MINION) {
			LOGGER.error("onCast {} {}: Tried to return {} which is not a minion", context.getGameId(), source, target);
			return;
		}

		Minion minion = (Minion) target;
		int boardPosition = target.getEntityLocation().getIndex();

		List<SpellDesc> deathrattles = new ArrayList<>(minion.getDeathrattles());
		super.onCast(context, player, desc, source, target);
		context.getLogic().resolveAftermaths(player.getId(), target.getReference(), deathrattles, target.getOwner(), boardPosition);
	}
}
