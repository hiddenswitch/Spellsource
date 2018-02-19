package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

@FunctionalInterface
public interface Spell {
	@Suspendable
	void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target);
}
