package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.heroes.Hero;

public final class FatigueEvent extends GameEvent implements HasValue {

	private Hero target;
	private int amount;

	public FatigueEvent(GameContext context, int targetPlayerId, int amount) {
		super(context, targetPlayerId, -1);
		Player player = context.getPlayer(targetPlayerId);
		target = player.getHero();
		this.amount = amount;
	}

	@Override
	public Hero getEventTarget() {
		return target;
	}

	@Override
	public Hero getTarget() {
		return target;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum getEventType() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.FATIGUE;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	@Override
	public int getValue() {
		return amount;
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s took %s fatigue damage", context.getPlayer(playerId).getName(), amount);
	}
}
