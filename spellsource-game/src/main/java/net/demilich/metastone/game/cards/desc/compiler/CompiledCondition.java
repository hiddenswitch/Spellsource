package net.demilich.metastone.game.cards.desc.compiler;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

@FunctionalInterface
public interface CompiledCondition {
	boolean isFulfilled(GameContext context, Player player, Entity source, Entity target);
}
