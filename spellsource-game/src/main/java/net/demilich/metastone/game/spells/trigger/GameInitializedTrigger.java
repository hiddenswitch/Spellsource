package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class GameInitializedTrigger extends EventTrigger {
	public GameInitializedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public Spellsource.GameEventTypeMessage.GameEventType interestedIn() {
		return Spellsource.GameEventTypeMessage.GameEventType.GAME_INITIALIZED;
	}
}
