package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.SummonEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

import java.util.Objects;

public final class BattlecryResolvedTrigger extends MinionPlayedTrigger {

	public BattlecryResolvedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		SummonEvent summonEvent = (SummonEvent) event;
		if (!summonEvent.isResolvedBattlecry()
				|| Objects.equals(summonEvent.getBattlecryActions(), BattlecryAction.NONE)) {
			return false;
		}
		return super.innerQueues(event, host);
	}

}
