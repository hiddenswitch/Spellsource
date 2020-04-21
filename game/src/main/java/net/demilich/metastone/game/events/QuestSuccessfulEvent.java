package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;

/**
 * A quest has processed.
 */
public final class QuestSuccessfulEvent extends AbstractQuestEvent {

	public QuestSuccessfulEvent(GameContext context, Quest quest, int playerId) {
		super(GameEvent.EventTypeEnum.QUEST_SUCCESSFUL, context, playerId, quest.getSourceCard(), quest);
	}
}
