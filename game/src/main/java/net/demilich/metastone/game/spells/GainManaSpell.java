package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Gives the player a temporary amount of {@link SpellArg#VALUE} mana.
 * <p>
 * To give the player 1 mana, like the coin:
 * <pre>
 *     {
 *         "class": "GainManaSpell",
 *         "value": 1
 *     }
 * </pre>
 *
 * @see RefreshManaSpell to refresh existing, empty mana crystals.
 */
public class GainManaSpell extends Spell {

	private static Logger logger = LoggerFactory.getLogger(GainManaSpell.class);

	/**
	 * Gain a fixed amount of mana.
	 *
	 * @param mana The amount of mana the player should gain this turn.
	 * @return The spell
	 */
	public static SpellDesc create(int mana) {
		Map<SpellArg, Object> arguments = new SpellDesc(GainManaSpell.class);
		arguments.put(SpellArg.VALUE, mana);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		checkArguments(logger, context, source, desc, SpellArg.VALUE);
		int mana = desc.getValue(SpellArg.VALUE, context, player, target, source, 0);
		if (mana <= 0) {
			logger.debug("onCast {} {}: Player loses mana ({}) in this spell.", context.getGameId(), source, mana);
		}
		if (mana != 0) {
			context.getLogic().modifyCurrentMana(player.getId(), mana, mana < 0);
		}
	}
}
