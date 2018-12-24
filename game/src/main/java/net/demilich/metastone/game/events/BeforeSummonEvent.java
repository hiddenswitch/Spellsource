package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.BattlecryAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Actor;

public final class BeforeSummonEvent extends SummonEvent {

	public BeforeSummonEvent(GameContext context, Actor minion, Card source, boolean didResolveBattlecry, BattlecryAction battlecryAction) {
		super(context, minion, source, didResolveBattlecry, battlecryAction);
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.BEFORE_SUMMON;
	}
}
