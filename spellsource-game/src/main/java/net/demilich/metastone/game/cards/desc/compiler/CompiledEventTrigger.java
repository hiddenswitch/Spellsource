package net.demilich.metastone.game.cards.desc.compiler;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.trigger.Enchantment;

public interface CompiledEventTrigger {
	boolean queues(GameEvent event, Enchantment enchantment, Entity host, int playerId);

	boolean fires(GameEvent event, Entity host, int playerId);
}
