package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

/**
 * Created by bberman on 1/18/17.
 */
public class ForeverPostDocSpell extends Spell {
	@Override
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		SpellDesc spellDesc = DamageSpell.create(source.getReference(), 1);
		DamageSpell damageSpell = new DamageSpell();
		damageSpell.onCast(context, player, spellDesc, source, source);
	}
}
