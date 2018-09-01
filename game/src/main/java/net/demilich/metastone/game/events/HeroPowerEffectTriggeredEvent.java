package net.demilich.metastone.game.events;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;

public class HeroPowerEffectTriggeredEvent extends GameEvent {
	private final Card heroPower;

	public HeroPowerEffectTriggeredEvent(GameContext context, int playerId, Card heroPower) {
		super(context, playerId, -1);
		this.heroPower = heroPower;
	}

	@Override
	public Entity getEventTarget() {
		return getHeroPower();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.HERO_POWER_EFFECT_TRIGGERED;
	}

	public Card getHeroPower() {
		return heroPower;
	}

	@Override
	public boolean isClientInterested() {
		return false;
	}
}