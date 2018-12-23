package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;

public class SummonEvent extends GameEvent implements HasCard {

	private final boolean resolvedBattlecry;
	private final BattlecryAction battlecryAction;
	private final Actor minion;
	private final Card source;

	public SummonEvent(GameContext context, Actor minion, Card source, boolean resolvedBattlecry, BattlecryAction battlecryAction) {
		super(context, minion.getOwner(), source == null ? -1 : source.getOwner());
		this.minion = minion;
		this.source = source;
		this.resolvedBattlecry = resolvedBattlecry;
		this.battlecryAction = battlecryAction;
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
	public boolean isClientInterested() {
		return true;
	}

	@Override
	public Card getCard() {
		return source;
	}

	public boolean isResolvedBattlecry() {
		return resolvedBattlecry;
	}

	public BattlecryAction getBattlecryAction() {
		return battlecryAction;
	}
}
