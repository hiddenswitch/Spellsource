package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires whenever a target is drained using {@link net.demilich.metastone.game.spells.DrainSpell}.
 *
 * @see net.demilich.metastone.game.events.DrainEvent
 */
public final class DrainTrigger extends EventTrigger {

	public DrainTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.DRAIN;
	}
}
