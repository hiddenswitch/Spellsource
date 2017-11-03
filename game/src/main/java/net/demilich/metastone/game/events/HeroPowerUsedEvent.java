package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.heroes.powers.HeroPowerCard;

public class HeroPowerUsedEvent extends GameEvent {

	private final HeroPowerCard heroPower;

	public HeroPowerUsedEvent(GameContext context, int playerId, HeroPowerCard heroPower) {
		super(context, playerId, -1);
		this.heroPower = heroPower;
	}
	
	@Override
	public Entity getEventTarget() {
		return getHeroPower();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.HERO_POWER_USED;
	}

	public HeroPowerCard getHeroPower() {
		return heroPower;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}
}
