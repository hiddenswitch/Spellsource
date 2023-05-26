package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.rpc.Spellsource.EntityTypeMessage.EntityType;
import net.demilich.metastone.game.events.DamageEvent;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

public class DamageReceivedTrigger extends EventTrigger {

	public DamageReceivedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTrigger create() {
		return new EventTriggerDesc(DamageReceivedTrigger.class).create();
	}

	public static EventTrigger create(TargetPlayer targetPlayer, EntityType targetEntityType) {
		EventTriggerDesc desc = new EventTriggerDesc(DamageReceivedTrigger.class);
		desc.put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
		desc.put(EventTriggerArg.TARGET_ENTITY_TYPE, targetEntityType);
		return desc.create();
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		DamageEvent damageEvent = (DamageEvent) event;

		EntityType targetEntityType = (EntityType) getDesc().get(EventTriggerArg.TARGET_ENTITY_TYPE);
		if (targetEntityType != null && damageEvent.getTarget().getEntityType() != targetEntityType) {
			return false;
		}

		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.DAMAGE;
	}

}

