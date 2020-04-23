package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;

/**
 * Fires after a quest is played.
 *
 * @see net.demilich.metastone.game.spells.AddQuestSpell for an effect that can put quests into play.
 * @see net.demilich.metastone.game.logic.GameLogic#playQuest(Player, Quest, boolean) for the method that puts quests into play.
 */
public class QuestPlayedTrigger extends EventTrigger {

	public QuestPlayedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.QUEST_PLAYED;
	}

}