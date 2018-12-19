package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Refreshes the player's mana by gaining an amount of mana equal to the difference in their max mana and current mana.
 * <p>
 * If a {@link SpellArg#VALUE} is provided, only refreshes that many mana crystals.
 * <p>
 * To use this, simply put {@code "RefreshManaSpell"} into the {@link SpellArg#CLASS} argument:
 * <pre>
 *     "spell": {
 *         "class": "RefreshManaSpell"
 *     }
 * </pre>
 */
public class RefreshManaSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		int max = player.getMaxMana() - player.getMana();
		int amountToRestore = desc.getValue(SpellArg.VALUE, context, player, target, source, max);
		SpellDesc gainMana = GainManaSpell.create(Math.max(Math.min(max, amountToRestore), 0));
		SpellUtils.castChildSpell(context, player, gainMana, source, target);
	}
}
