package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.BoardChangedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.Zones;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swaps the minion specified by {@link SpellArg#SECONDARY_TARGET} with the {@code target}.
 * <p>
 * For <b>example,</b> for a minion to swap itself with a random enemy minion:
 * <pre>
 *     {
 *         "class": "SwapMinionSpell",
 *         "target": "ENEMY_MINIONS",
 *         "secondaryTarget": "SELF",
 *         "randomTarget": true
 *     }
 * </pre>
 */
public class SwapMinionSpell extends Spell {
	private static Logger logger = LoggerFactory.getLogger(SwapMinionSpell.class);

	@Override
	@Suspendable
	@SuppressWarnings("unchecked")
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.SECONDARY_TARGET);
		Minion swap = (Minion) context.resolveTarget(player, source, (EntityReference) desc.get(SpellArg.SECONDARY_TARGET)).get(0);
		if (swap.equals(target)) {
			logger.warn("onCast {} {}: Target {} was asked to swap with itself. SECONDARY_TARGET={}", context.getGameId(), source, target, desc.get(SpellArg.SECONDARY_TARGET));
			return;
		}
		boolean ownershipChangeRequired = swap.getOwner() != target.getOwner();
		EntityLocation swapLocation = swap.getEntityLocation();
		EntityLocation targetLocation = target.getEntityLocation();
		swap.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
		target.moveOrAddTo(context, Zones.SET_ASIDE_ZONE);
		context.getPlayer(targetLocation.getPlayer()).getZone(targetLocation.getZone()).add(targetLocation.getIndex(), swap);
		context.getPlayer(swapLocation.getPlayer()).getZone(swapLocation.getZone()).add(swapLocation.getIndex(), target);
		if (ownershipChangeRequired) {
			context.getLogic().innerChangeOwner(target, swapLocation.getPlayer());
			context.getLogic().innerChangeOwner(swap, targetLocation.getPlayer());
		}
		context.fireGameEvent(new BoardChangedEvent(context));
	}
}
