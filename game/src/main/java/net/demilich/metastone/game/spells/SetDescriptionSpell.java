package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.utils.Attribute;

public class SetDescriptionSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		if (desc.containsKey(SpellArg.DESCRIPTION)) {
			final String description = desc.getString(SpellArg.DESCRIPTION);
			target.getAttributes().put(Attribute.DESCRIPTION, description);
		} else {
			target.getAttributes().remove(Attribute.DESCRIPTION);
		}
	}
}
