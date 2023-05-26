package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.condition.CardPropertyCondition;
import net.demilich.metastone.game.spells.desc.condition.EntityEqualsCondition;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.targeting.EntityReference;

import static net.demilich.metastone.game.GameContext.PLAYER_1;

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

	/**
	 * Fires when a minion is summoned from this specific card
	 *
	 * @param player
	 * @param card
	 * @return
	 */
	public static EventTriggerDesc create(Player player, Card card) {
		var desc = new EventTriggerDesc(MinionSummonedTrigger.class);
		desc.put(EventTriggerArg.TARGET_PLAYER, player.getId() == PLAYER_1 ? TargetPlayer.PLAYER_1 : TargetPlayer.PLAYER_2);
		desc.put(EventTriggerArg.QUEUE_CONDITION, EntityEqualsCondition.create(EntityReference.EVENT_SOURCE, card.getReference()).create());
		return desc;
	}

	@Override
	public GameEventType interestedIn() {
		return com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.SUMMON;
	}

}


