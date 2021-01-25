package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;

import static com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.QUEST_PLAYED;

/**
 * A quest was played.
 */
public class QuestPlayedEvent extends AbstractQuestEvent {

	public QuestPlayedEvent(GameContext context, int playerId, Card questCard, Quest quest) {
		super(QUEST_PLAYED, context, playerId, questCard, quest);
	}
}