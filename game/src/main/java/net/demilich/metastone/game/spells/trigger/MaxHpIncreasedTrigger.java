package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires whenever the matching entity's {@link net.demilich.metastone.game.cards.Attribute#MAX_HP} is increased via the
 * {@link net.demilich.metastone.game.logic.GameLogic#setHpAndMaxHp(Actor, int)} effect.
 * <p>
 * Does not trigger off of {@link net.demilich.metastone.game.spells.ModifyAttributeSpell}.
 */
public final class MaxHpIncreasedTrigger extends EventTrigger {

	public MaxHpIncreasedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.MAX_HP_INCREASED;
	}
}
