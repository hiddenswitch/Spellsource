package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires when the host deals exactly lethal damage to a target (the damage equals the target's remaining HP).
 */
public class HonorableKillTrigger extends EventTrigger {

	public HonorableKillTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return event.getSource() == host;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.HONORABLE_KILL;
	}
}
