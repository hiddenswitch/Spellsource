package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Removes "bonus" attributes, or attributes that didn't start on the text of the minion.
 * <p>
 * This is distinct from a silence, which removes all text.
 * <p>
 * Implements Felfire Glutton.
 */
public class RemoveBonusAttributes extends Spell {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		context.getLogic().removeBonusAttributes(target);
	}
}
