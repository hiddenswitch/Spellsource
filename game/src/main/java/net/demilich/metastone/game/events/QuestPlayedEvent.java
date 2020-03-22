package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

public class QuestPlayedEvent extends GameEvent implements HasCard {

	private final Card questCard;

	public QuestPlayedEvent(GameContext context, int playerId, Card questCard) {
		super(context, playerId, -1);
		this.questCard = questCard;
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
}
