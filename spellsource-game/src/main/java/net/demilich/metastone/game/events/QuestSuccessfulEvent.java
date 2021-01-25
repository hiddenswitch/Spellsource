package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;

/**
 * A quest has processed.
 */
public final class QuestSuccessfulEvent extends AbstractQuestEvent {

	public QuestSuccessfulEvent(GameContext context, Quest quest, int playerId) {
		super(GameEventType.QUEST_SUCCESSFUL, context, playerId, quest.getSourceCard(), quest);
	}
}
