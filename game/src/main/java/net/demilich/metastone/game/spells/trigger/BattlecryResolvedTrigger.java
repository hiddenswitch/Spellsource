package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.events.SummonEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

import java.util.Objects;

public final class BattlecryResolvedTrigger extends MinionPlayedTrigger {

	public BattlecryResolvedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean fire(GameEvent event, Entity host) {
		SummonEvent summonEvent = (SummonEvent) event;
		if (!summonEvent.isResolvedBattlecry()
				|| Objects.equals(summonEvent.getBattlecryAction(), BattlecryAction.NONE)) {
			return false;
		}
		return super.fire(event, host);
	}

}
