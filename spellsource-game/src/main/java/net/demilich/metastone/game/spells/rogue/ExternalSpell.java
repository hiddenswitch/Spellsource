package net.demilich.metastone.game.spells.rogue;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.Spell;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public abstract class ExternalSpell extends Spell implements ExternalLogicComponent {

	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		context.getExternalLogicComponent(this).onCast(context, player, desc, source, target);
	}

}
