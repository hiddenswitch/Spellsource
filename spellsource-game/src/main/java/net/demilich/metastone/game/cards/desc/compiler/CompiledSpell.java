package net.demilich.metastone.game.cards.desc.compiler;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;

import java.util.List;

/**
 * Functional interface implemented by hidden JVM classes compiled from {@link SpellDesc} instances.
 */
@FunctionalInterface
public interface CompiledSpell {
	void cast(GameContext context, Player player, SpellDesc desc, Entity source, List<Entity> targets);
}
