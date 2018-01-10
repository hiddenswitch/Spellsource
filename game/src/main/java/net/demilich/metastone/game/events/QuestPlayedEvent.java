package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.QuestCard;
import net.demilich.metastone.game.entities.Entity;

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
	public GameEventType getEventType() {
		return GameEventType.QUEST_PLAYED;
	}

	public Card getQuestCard() {
		return questCard;
	}

	@Override
	public Card getCard() {
		return questCard;
	}
}
