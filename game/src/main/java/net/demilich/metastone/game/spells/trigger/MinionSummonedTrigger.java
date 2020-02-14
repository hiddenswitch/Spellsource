package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.condition.CardPropertyCondition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;

/**
 * Fires whenever a minion is summoned. This occurs after the minion's enchantments have been put into play, but before
 * its battlecry has been evaluated.
 */
public class MinionSummonedTrigger extends AbstractSummonTrigger {

	public MinionSummonedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	public static EventTriggerDesc create(TargetPlayer player, String minionCardId) {
		EventTriggerDesc desc = new EventTriggerDesc(MinionSummonedTrigger.class);
		desc.put(EventTriggerArg.TARGET_PLAYER, player);
		desc.put(EventTriggerArg.FIRE_CONDITION, CardPropertyCondition.create(EntityReference.EVENT_TARGET, minionCardId));
		return desc;
	}

	@Override
	public GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.SUMMON;
	}

}


