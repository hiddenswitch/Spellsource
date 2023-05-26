package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class BoardChangedTrigger extends EventTrigger {

	public BoardChangedTrigger() {
		this(new EventTriggerDesc(BoardChangedTrigger.class));
	}

	public BoardChangedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTrigger create() {
		return new BoardChangedTrigger();
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.BOARD_CHANGED;
	}

}
