package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.WeaponEquippedEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.cards.Attribute;

public class WeaponEquippedTrigger extends EventTrigger {

	public WeaponEquippedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		WeaponEquippedEvent summonEvent = (WeaponEquippedEvent) event;
		Attribute requiredAttribute = (Attribute) getDesc().get(EventTriggerArg.REQUIRED_ATTRIBUTE);
		if (requiredAttribute != null && !summonEvent.getWeapon().hasAttribute(requiredAttribute)) {
			return false;
		}

		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.WEAPON_EQUIPPED;
	}

}
