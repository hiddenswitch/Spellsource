package net.demilich.metastone.game.actions;

import java.io.Serializable;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.gson.annotations.SerializedName;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
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
public abstract class GameAction implements Cloneable, Serializable {
	private int id;
	private TargetSelection targetRequirement = TargetSelection.NONE;
	private ActionType actionType = ActionType.SYSTEM;
	private EntityReference source;
	private EntityReference targetKey;
	private String actionSuffix;

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

	public String getActionSuffix() {
		return actionSuffix;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public abstract String getPromptText();

	public EntityReference getSource() {
		return source;
	}

	public EntityReference getTargetKey() {
		return targetKey;
	}

	public TargetSelection getTargetRequirement() {
		return targetRequirement;
	}

	public abstract boolean isSameActionGroup(GameAction anotherAction);

	public void setActionSuffix(String actionSuffix) {
		this.actionSuffix = actionSuffix;
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

	public void setTargetKey(EntityReference targetKey) {
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
					&& (this.getSource().equals(otherAction.getSource()))
					&& (this.getTargetKey().equals(otherAction.getTargetKey()))
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
				.append(actionSuffix)
				.toHashCode();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
