package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Refreshes the {@code target} {@link Actor}'s attacks, accounting for {@link Attribute#WINDFURY}.
 * <p>
 * For <b>example,</b> to refresh all friendly minion's attacks:
 * <pre>
 *     {
 *         "class": "RefreshAttacksSpell",
 *         "target": "FRIENDLY_MINIONS"
 *     }
 * </pre>
 */
public class RefreshAttacksSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(RefreshAttacksSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (!(target instanceof Actor)) {
			logger.error("onCast {} {}: Cannot refresh attacks of {}, which is not an actor.", context.getGameId(), source, target);
		}

		((Actor)target).refreshAttacksPerRound();
	}

	public static SpellDesc create(EntityReference reference) {
		SpellDesc inst = new SpellDesc(RefreshAttacksSpell.class);
		inst.put(SpellArg.TARGET, reference);
		return inst;
	}
}
