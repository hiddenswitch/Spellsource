package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.entities.Entity;

/**
 * A target will be acquired for the specified game action.
 * <p>
 * Gives an opportunity to override the target or cancel target acquisition.
 */
public class TargetAcquisitionEvent extends BasicGameEvent {

	private final ActionType actionType;
	private final GameAction action;

	public TargetAcquisitionEvent(GameContext context, GameAction sourceAction, Entity source, Entity target) {
		super(com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.TARGET_ACQUISITION, context, context.getPlayer(source.getOwner()), source, target);
		this.action = sourceAction;
		this.actionType = sourceAction == null ? null : sourceAction.getActionType();
	}

	public ActionType getActionType() {
		return actionType;
	}

	public GameAction getAction() {
		return action;
	}
}
