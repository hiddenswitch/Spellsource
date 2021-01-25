package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.actions.OpenerAction;
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
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		SummonEvent summonEvent = (SummonEvent) event;
		if (!summonEvent.isResolvedOpener()
				|| Objects.equals(summonEvent.getOpenerActions(), OpenerAction.NONE)) {
			return false;
		}
		return super.innerQueues(event, enchantment, host);
	}

}
