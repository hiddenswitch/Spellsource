package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class HeroPowerUsedEvent extends GameEvent {

	private final Card heroPower;

	public HeroPowerUsedEvent(GameContext context, int playerId, Card heroPower) {
		super(context, playerId, -1);
		this.heroPower = heroPower;
	}

	@Override
	public Entity getEventTarget() {
		return getHeroPower();
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.HERO_POWER_USED;
	}

	public Card getHeroPower() {
		return heroPower;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
