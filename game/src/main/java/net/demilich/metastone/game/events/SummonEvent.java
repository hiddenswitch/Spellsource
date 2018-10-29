package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

public class SummonEvent extends GameEvent implements HasCard {

	private final Actor minion;
	private final Card source;

	public SummonEvent(GameContext context, Actor minion, Card source) {
		super(context, minion.getOwner(), source == null ? -1 : source.getOwner());
		this.minion = minion;
		this.source = source;
	}

	@Override
	public Entity getEventTarget() {
		return getMinion();
	}

	@Override
	public Entity getEventSource() {
		return getSource();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.SUMMON;
	}

	public Actor getMinion() {
		return minion;
	}

	public Card getSource() {
		return source;
	}

	@Override
	public String toString() {
		return "[Summon Event MINION " + minion + " from SOURCE " + source + "]";
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	@Override
	public Card getCard() {
		return source;
	}
}
