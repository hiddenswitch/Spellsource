package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires when the player gets dealt damage from {@link net.demilich.metastone.game.cards.Attribute#FATIGUE}.
 */
public class FatigueTrigger extends EventTrigger {

	public FatigueTrigger() {
		this(new EventTriggerDesc(FatigueTrigger.class));
	}

	public FatigueTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTriggerDesc create(TargetPlayer targetPlayer) {
		EventTriggerDesc desc = new EventTriggerDesc(FatigueTrigger.class);
		desc.put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
		return desc;
	}

	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	protected boolean fire(GameEvent event, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.FATIGUE;
	}
}
