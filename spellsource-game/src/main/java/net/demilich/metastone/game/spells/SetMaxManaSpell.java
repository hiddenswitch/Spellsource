package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Sets a player's max mana to the given {@link SpellArg#VALUE}
 */
public class SetMaxManaSpell extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		player.setMaxMana(desc.getValue(SpellArg.VALUE, context, player, target, source, 10));
	}
}
