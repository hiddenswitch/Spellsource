package net.demilich.metastone.game.spells.custom;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.spells.ReturnTargetToHandSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Records the {@code target} minion's deathrattles. Returns the minion to hand. Triggers those deathrattles from the
 * target's old location.
 */
public final class SleightOfHandSpell extends ReturnTargetToHandSpell {

	private static Logger LOGGER = LoggerFactory.getLogger(SleightOfHandSpell.class);

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (target.getEntityType() != EntityType.MINION) {
			LOGGER.error("onCast {} {}: Tried to return {} which is not a minion", context.getGameId(), source, target);
			return;
		}

		Actor actor = (Actor) target;
		int boardPosition = target.getEntityLocation().getIndex();

		List<SpellDesc> aftermaths = context.getLogic().getAftermathSpells(actor);
		super.onCast(context, player, desc, source, target);
		context.getLogic().resolveAftermaths(player.getId(), target.getReference(), aftermaths, target.getOwner(), boardPosition);
	}
}
