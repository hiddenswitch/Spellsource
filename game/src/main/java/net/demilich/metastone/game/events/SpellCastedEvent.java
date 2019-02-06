package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

public class SpellCastedEvent extends GameEvent implements HasCard {

	private final Card sourceCard;
	private final Entity target;

	public SpellCastedEvent(GameContext context, int playerId, Card sourceCard, Entity target) {
		super(context, target != null ? target.getOwner() : -1, playerId);
		this.sourceCard = sourceCard;
		this.target = target;
	}

	@Override
	public Entity getEventSource() {
		return getSourceCard();
	}

	@Override
	public Entity getEventTarget() {
		return target;
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.SPELL_CASTED;
	}

	public Card getSourceCard() {
		return sourceCard;
	}

	@Override
	public Card getCard() {
		return sourceCard;
	}
}
