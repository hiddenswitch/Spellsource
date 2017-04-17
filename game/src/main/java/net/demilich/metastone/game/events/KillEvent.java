package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.Entity;

public class KillEvent extends GameEvent {

	private final Entity victim;
	private final int formerBoardPosition;

	public KillEvent(GameContext context, Entity victim, int formerBoardPosition) {
		super(context, victim.getOwner(), -1);
		this.victim = victim;
		this.formerBoardPosition = formerBoardPosition;
	}
	
	@Override
	public Entity getEventTarget() {
		return getVictim();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.KILL;
	}

	public Entity getVictim() {
		return victim;
	}

	public int getFormerBoardPosition() {
		return formerBoardPosition;
	}
}
