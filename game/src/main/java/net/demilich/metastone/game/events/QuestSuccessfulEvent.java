package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.HasCard;

public class QuestSuccessfulEvent extends GameEvent implements HasCard {

	private final Card quest;

	public QuestSuccessfulEvent(GameContext context, Card quest, int playerId) {
		super(context, playerId, -1);
		this.quest = quest;
	}

	@Override
	public Entity getEventTarget() {
		return getSourceCard();
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.SECRET_REVEALED;
	}

	@Override
	public Card getSourceCard() {
		return quest;
	}
}
