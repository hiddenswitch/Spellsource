package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires when a tap action is activated on an entity (including Forge effects on hand cards).
 * <p>
 * The event target is the card associated with the tapped entity.
 */
public class TapTrigger extends AbstractCardTrigger {

	public TapTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.TAP_ACTIVATED;
	}
}
