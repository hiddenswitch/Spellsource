package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.condition.CardPropertyCondition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;

public class MinionPlayedTrigger extends MinionSummonedTrigger {

	public static EventTriggerDesc create(TargetPlayer player, String minionCardId) {
		EventTriggerDesc desc = new EventTriggerDesc(MinionPlayedTrigger.class);
		desc.put(EventTriggerArg.TARGET_PLAYER, player);
		desc.put(EventTriggerArg.FIRE_CONDITION, CardPropertyCondition.create(EntityReference.EVENT_TARGET, minionCardId));
		return desc;
	}

	public MinionPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean onlyPlayedFromHandOrDeck() {
		return true;
	}
}

