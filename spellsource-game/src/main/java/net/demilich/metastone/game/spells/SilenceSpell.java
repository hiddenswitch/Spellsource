package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Silences the specified {@link Actor}.
 * <p>
 * For example, to silence a random enemy minion:
 * <pre>
 *   {
 *     "class": "SilenceSpell",
 *     "target": "ENEMY_MINIONS",
 *     "randomTarget": true
 *   }
 * </pre>
 */
public class SilenceSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(SilenceSpell.class);

	public static SpellDesc create() {
		return create(null);
	}

	public static SpellDesc create(EntityReference target) {
		Map<SpellArg, Object> arguments = new SpellDesc(SilenceSpell.class);
		arguments.put(SpellArg.TARGET, target);
		return new SpellDesc(arguments);
	}

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (!(target instanceof Actor)) {
			logger.error("onCast {} {}: The specified target {} is not an actor, so it cannot be silenced", context.getGameId(), source, target);
			return;
		}

		context.getLogic().silence(player.getId(), (Actor) target);
	}
}
