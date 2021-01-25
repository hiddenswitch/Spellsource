package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.rpc.Spellsource.ActionTypeMessage.ActionType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
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
	private EntityReference sourceReference;
	private EntityReference targetReference;
	private boolean overrideChild;

	@Suspendable
	public boolean canBeExecutedOn(GameContext gameContext, Player player, Entity entity) {
		return true;
	}

	@Override
	public GameAction clone() {
		try {
			return (GameAction) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new UnsupportedOperationException(e);
		}
	}

	/**
	 * The implementation of this method actually represents the game effects of this action.
	 * <p>
	 * For example, the {@link PlayMinionCardAction} eventually calls {@link net.demilich.metastone.game.logic.GameLogic#summon(int,
	 * Minion, Entity, int, boolean)}.
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
		return sourceReference;
	}

	public EntityReference getTargetReference() {
		return targetReference;
	}

	public TargetSelection getTargetRequirement() {
		return targetRequirement;
	}

	protected void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public void setSourceReference(EntityReference sourceReference) {
		this.sourceReference = sourceReference;
	}

	public void setTarget(Entity target) {
		this.targetReference = EntityReference.pointTo(target);
	}

	public void setTargetReference(EntityReference targetKey) {
		this.targetReference = targetKey;
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

	public String getDescription(GameContext context, int playerId) {
		return getClass().getSimpleName();
	}

	@Override
	public Entity getSource() {
		throw new UnsupportedOperationException("use sourceReference");
	}

	@Override
	public Entity getSource(GameContext context) {
		return context.resolveSingleTarget(sourceReference);
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
				.append("sourceReference", sourceReference)
				.append("targetReference", targetReference)
				.toString();
	}

	public boolean isOverrideChild() {
		return overrideChild;
	}

	public GameAction setOverrideChild(boolean overrideChild) {
		this.overrideChild = overrideChild;
		return this;
	}
}

