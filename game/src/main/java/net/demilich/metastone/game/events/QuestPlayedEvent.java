package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;
import net.demilich.metastone.game.spells.trigger.secrets.Quest;

public class QuestPlayedEvent extends GameEvent implements HasCard {

	private final Card questCard;
	private final Quest quest;

	public QuestPlayedEvent(GameContext context, int playerId, Card questCard, Quest quest) {
		super(context, playerId, -1);
		this.questCard = questCard;
		this.quest = quest;
	}

	@Override
	public Entity getEventTarget() {
		return getQuestCard();
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.QUEST_PLAYED;
	}

	public Card getQuestCard() {
		return questCard;
	}

	@Override
	public Card getSourceCard() {
		return questCard;
	}

	public Quest getQuest() {
		return quest;
	}
}
