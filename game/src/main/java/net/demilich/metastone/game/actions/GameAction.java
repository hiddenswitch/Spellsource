package net.demilich.metastone.game.actions;

import com.github.fromage.quasi.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.events.Notification;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

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
	protected Integer chooseOneOptionIndex = null;
	private int id = -1;
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

	/**
	 * The implementation of this method actually represents the game effects of this action.
	 * <p>
	 * For example, the {@link PlayMinionCardAction} eventually calls {@link net.demilich.metastone.game.logic.GameLogic#summon(int,
	 * Minion, Card, int, boolean)}.
	 *
	 * @param context  The game context
	 * @param playerId The invoking player
	 * @see PlayCardAction#execute(GameContext, int) for an important implementation for playing cards.
	 */
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
		if (!(other instanceof GameAction)) {
			return false;
		}

		GameAction rhs = (GameAction) other;

		return new EqualsBuilder()
				.append(chooseOneOptionIndex, rhs.chooseOneOptionIndex)
				.append(getTargetRequirement(), rhs.getTargetRequirement())
				.append(getActionType(), rhs.getActionType())
				.append(getSourceReference(), rhs.getSourceReference())
				.append(getTargetReference(), rhs.getTargetReference())
				.build();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(chooseOneOptionIndex)
				.append(getTargetRequirement())
				.append(getActionType())
				.append(getSourceReference())
				.append(getTargetReference())
				.build();
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
	 *
	 * @param context
	 * @param playerId
	 * @return
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

	public GameAction withTargetReference(EntityReference reference) {
		setTargetReference(reference);
		return this;
	}

	@Override
	public boolean isClientInterested() {
		return true;
	}

	public Integer getChooseOneOptionIndex() {
		return chooseOneOptionIndex;
	}

	public void setChooseOneOptionIndex(Integer chooseOneOptionIndex) {
		this.chooseOneOptionIndex = chooseOneOptionIndex;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("actionType", actionType)
				.append("source", source)
				.append("target", targetKey)
				.toString();
	}
}
