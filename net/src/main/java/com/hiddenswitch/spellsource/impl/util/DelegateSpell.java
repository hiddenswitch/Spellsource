package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Spellsource;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;

public class DelegateSpell extends net.demilich.metastone.game.spells.Spell {
	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		String name = desc.getString(SpellArg.NAME);
		Spell spell = Spellsource.spellsource().getSpells().get(name);

		if (spell != null) {
			spell.onCast(context, player, desc, source, target);
		}
	}
}
