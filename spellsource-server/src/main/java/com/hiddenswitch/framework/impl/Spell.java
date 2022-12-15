package com.hiddenswitch.framework.impl;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

@FunctionalInterface
public interface Spell {
	void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target);
}
