package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;

public abstract class AbstractQuestEvent extends CardEvent {
	private final Quest quest;

	AbstractQuestEvent(GameEvent.EventTypeEnum eventType, GameContext context, int playerId, Card questCard, Quest quest) {
		super(eventType, context, playerId, -1, questCard);
		this.quest = quest;
	}


	public Quest getQuest() {
		return quest;
	}
}
