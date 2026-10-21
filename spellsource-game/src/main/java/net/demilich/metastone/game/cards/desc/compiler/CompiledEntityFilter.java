package net.demilich.metastone.game.cards.desc.compiler;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;

@FunctionalInterface
public interface CompiledEntityFilter {
	boolean matches(GameContext context, Player player, Entity entity, Entity host);
}
