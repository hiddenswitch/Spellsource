package net.demilich.metastone.game.actions;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * An action a player can take in the game.
 * <p>
 * This class both represents the piece of data that a player should consider from a list from {@link
 * net.demilich.metastone.game.logic.GameLogic#getValidActions(int)}. It also has the code that executes the action in
 * the {@link net.demilich.metastone.game.logic.GameLogic}.
 *
 * @see net.demilich.metastone.game.logic.GameLogic#performGameAction(int, GameAction) for more about game actions.
 */
public abstract class GameAction implements Cloneable, Serializable, Notification {
	private int id;
	private TargetSelection targetRequirement = TargetSelection.NONE;
	private ActionType actionType = ActionType.SYSTEM;
	private EntityReference source;
	private EntityReference targetKey;

	public boolean canBeExecutedOn(GameContext gameContext, Player player, Entity entity) {
		return true;
	}

	@Override
	public GameAction clone() {
		try {
			final GameAction clone = (GameAction) super.clone();
			clone.setId(getId());
			return clone;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Suspendable
	public abstract void execute(GameContext context, int playerId);

	public ActionType getActionType() {
		return actionType;
	}

	public EntityReference getSourceReference() {
		return source;
	}

	public EntityReference getTargetReference() {
		return targetKey;
	}

	public TargetSelection getTargetRequirement() {
		return targetRequirement;
	}

	protected void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public void setSource(EntityReference source) {
		this.source = source;
	}

	public void setTarget(Entity target) {
		this.targetKey = EntityReference.pointTo(target);
	}

	public void setTargetReference(EntityReference targetKey) {
		this.targetKey = targetKey;
	}

	public void setTargetRequirement(TargetSelection targetRequirement) {
		this.targetRequirement = targetRequirement;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof GameAction) {
			GameAction otherAction = (GameAction) other;
			return (this.actionType == otherAction.actionType)
					&& (this.targetRequirement == otherAction.targetRequirement)
					&& (this.getSourceReference().equals(otherAction.getSourceReference()))
					&& (this.getTargetReference().equals(otherAction.getTargetReference()))
					&& (this.getId() == otherAction.getId());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(id)
				.append(targetRequirement)
				.append(actionType)
				.append(source)
				.append(targetKey)
				.toHashCode();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public boolean isPowerHistory() {
		return true;
	}

	/**
	 * A user-renderable description of what occurred in this notification.
	 * @return
	 * @param context
	 * @param playerId
	 */
	public String getDescription(GameContext context, int playerId) {
		return getClass().getSimpleName();
	}

	@Override
	public Entity getSource(GameContext context) {
		return context.resolveSingleTarget(getSourceReference());
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		final Entity target = context.resolveSingleTarget(getTargetReference());
		return target == null ? Collections.emptyList() : Collections.singletonList(target);
	}
}
