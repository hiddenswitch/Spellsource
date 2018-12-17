package net.demilich.metastone.game.spells;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class SetMaxManaSpell extends Spell {
	private static final long serialVersionUID = 5064742607983235485L;

	@Override
    protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
        player.setMaxMana(desc.getValue(SpellArg.VALUE, context, player, target, source, 10));
    }
}
