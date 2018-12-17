package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.entities.Entity;

public class TargetAcquisitionEvent extends GameEvent {

	private static final long serialVersionUID = -4544144586682682981L;
	private final Entity target;
	private final Entity source;
	private final ActionType actionType;
	private final GameAction action;

	public TargetAcquisitionEvent(GameContext context, GameAction sourceAction, Entity source, Entity target) {
		super(context, target.getOwner(), source.getOwner());
		this.action = sourceAction;
		this.actionType = sourceAction == null ? null : sourceAction.getActionType();
		this.source = source;
		this.target = target;
	}

	public ActionType getActionType() {
		return actionType;
	}

	@Override
	public Entity getEventSource() {
		return getSource();
	}

	@Override
	public Entity getEventTarget() {
		return getTarget();
	}

	@Override
	public GameEventType getEventType() {
		return GameEventType.TARGET_ACQUISITION;
	}

	public Entity getSource() {
		return source;
	}

	public Entity getTarget() {
		return target;
	}

	public GameAction getAction() {
		return action;
	}
}
