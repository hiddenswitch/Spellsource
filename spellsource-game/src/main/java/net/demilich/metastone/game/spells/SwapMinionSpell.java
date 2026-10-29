package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityLocation;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.BoardChangedEvent;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
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

	@SuppressWarnings("unchecked")
	protected void onCast(GameContext context, Player player, Entity source, Entity target, EntityReference secondaryTarget) {
		if (target == null) {
			return;
		}
		var resolved = context.resolveTarget(player, source, secondaryTarget);
		if (resolved.isEmpty() || !(resolved.get(0) instanceof Minion swap)) {
			return;
		}
		if (swap.equals(target)) {
			logger.warn("onCast {} {}: Target {} was asked to swap with itself. SECONDARY_TARGET={}", context.getGameId(), source, target, secondaryTarget);
			return;
		}
		boolean ownershipChangeRequired = swap.getOwner() != target.getOwner();
		EntityLocation swapLocation = swap.getEntityLocation();
		EntityLocation targetLocation = target.getEntityLocation();
		context.getPlayer(swapLocation.getPlayer()).getZone(swapLocation.getZone()).remove(swap);
		context.getPlayer(targetLocation.getPlayer()).getZone(targetLocation.getZone()).remove(target);
		var targetZone = context.getPlayer(targetLocation.getPlayer()).getZone(targetLocation.getZone());
		targetZone.add(Math.min(targetLocation.getIndex(), targetZone.size()), swap);
		var swapZone = context.getPlayer(swapLocation.getPlayer()).getZone(swapLocation.getZone());
		swapZone.add(Math.min(swapLocation.getIndex(), swapZone.size()), target);
		if (ownershipChangeRequired) {
			context.getLogic().innerChangeOwner(target, swapLocation.getPlayer());
			context.getLogic().innerChangeOwner(swap, targetLocation.getPlayer());
		}
		context.getLogic().fireGameEvent(new BoardChangedEvent(context));
	}
}
