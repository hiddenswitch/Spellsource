package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.NullSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Battlecry actions occur when {@link net.demilich.metastone.game.entities.Actor} entities are played from cards and
 * have battlecries. A battlecry is a possibly targeted effect.
 */
public final class BattlecryAction extends GameAction {

	public static final BattlecryAction NONE = new BattlecryAction(NullSpell.create());
	private static final String BATTLECRY_NAME = "Battlecry";
	private final SpellDesc spell;
	private Condition condition;

	/**
	 * Creates a battlecry that performs the specify spell. Does not ask for target selection.
	 *
	 * @param spell The spell to cast for this battlecry action.
	 * @return An instance
	 */
	public static BattlecryAction createBattlecry(SpellDesc spell) {
		return createBattlecry(spell, TargetSelection.NONE);
	}

	/**
	 * Creates a battlecry action that performs the specified spell and requests a target.
	 * <p>
	 * To filter the target, make sure to add a {@link net.demilich.metastone.game.spells.desc.SpellArg#FILTER} to the
	 * spell.
	 *
	 * @param spell           The spell to cast for this battlecry action.
	 * @param targetSelection The target selection to make.
	 * @return An instance
	 */
	public static BattlecryAction createBattlecry(SpellDesc spell, TargetSelection targetSelection) {
		BattlecryAction battlecry = new BattlecryAction(spell);
		battlecry.setTargetRequirement(targetSelection);
		return battlecry;
	}

	protected BattlecryAction(SpellDesc spell) {
		this.spell = spell;
		setActionType(ActionType.BATTLECRY);
	}

	@Override
	public BattlecryAction clone() {
		return (BattlecryAction) super.clone();
	}

	/**
	 * Computes whether the condition is fulfilled for a battlecry action to be executable. Conditions are <b>not</b>
	 * evaluated against targets.
	 *
	 * @param context The game context
	 * @param player  The casting player
	 * @return {@code true} if this battlecry can be executed <b>generally</b> (if it will even prompt the user for
	 * 		targeting).
	 */
	public boolean canBeExecuted(GameContext context, Player player) {
		if (getCondition() == null) {
			return true;
		}
		return getCondition().isFulfilled(context, player, getSource(context), null);
	}

	/**
	 * Computes whether the given target {@code entity} can be targeted by this battlecry.
	 *
	 * @param context The game context
	 * @param player  The casting player
	 * @param entity  The target entity
	 * @return {@code true} if the entity is a valid target for the battlecry.
	 */
	@Override
	public final boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		if (!super.canBeExecutedOn(context, player, entity)) {
			return false;
		}
		if (getSourceReference().getId() == entity.getId()) {
			return false;
		}
		if (getEntityFilter() == null) {
			return true;
		}
		return getEntityFilter().matches(context, player, entity, getSource(context));
	}

	/**
	 * Casts the {@link #getSpell()} on this action with the specified target.
	 *
	 * @param context  The game context
	 * @param playerId The casting player.
	 */
	@Override
	@Suspendable
	public void execute(GameContext context, int playerId) {
		EntityReference target = getPredefinedSpellTargetOrUserTarget();
		context.getLogic().castSpell(playerId, getSpell(), getSourceReference(), target, getTargetRequirement(), false, this);
	}

	/**
	 * Returns either the target chosen by the user or the predefined target (possible a group reference / {@link
	 * EntityReference#isTargetGroup()}) written on the {@link SpellDesc} of the spell.
	 *
	 * @return An entity reference
	 */
	public EntityReference getPredefinedSpellTargetOrUserTarget() {
		return getSpell().hasPredefinedTarget() ? getSpell().getTarget() : getTargetReference();
	}

	private Condition getCondition() {
		return condition;
	}

	/**
	 * Returns the {@link net.demilich.metastone.game.spells.desc.SpellArg#FILTER} of the spell.
	 *
	 * @return The filter.
	 */
	public EntityFilter getEntityFilter() {
		return spell.getEntityFilter();
	}

	public SpellDesc getSpell() {
		return spell;
	}

	public void setCondition(Condition condition) {
		this.condition = condition;
	}

	@Override
	public Entity getSource(GameContext context) {
		return context.resolveSingleTarget(getSourceReference());
	}

	@Override
	public List<Entity> getTargets(GameContext context, int player) {
		final List<Entity> entities = context.resolveTarget(context.getPlayer(getSource(context).getOwner()), getSource(context), getTargetReference());
		if (entities == null) {
			return Collections.emptyList();
		}
		return entities;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof BattlecryAction)) {
			return false;
		}
		BattlecryAction rhs = (BattlecryAction) other;
		return new EqualsBuilder()
				.appendSuper(super.equals(other))
				.append(spell, rhs.spell)
				.append(condition, rhs.condition)
				.build();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.appendSuper(super.hashCode())
				.append(spell)
				.append(condition)
				.build();
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		Entity source = context.resolveSingleTarget(getSourceReference());
		final EntityReference targetReference = getPredefinedSpellTargetOrUserTarget();

		if (targetReference == null || targetReference.isTargetGroup()) {
			return String.format("%s's %s occurred.", BATTLECRY_NAME, source.getName());
		}
		Entity target = context.resolveSingleTarget(targetReference);

		if (source != null && target != null
				&& source.getName() != null && target.getName() != null) {
			return String.format("%s's %s targeted %s.", source.getName(), BATTLECRY_NAME, target.getName());
		}
		if (source != null && target == null
				&& source.getName() != null) {
			return String.format("%s's %s occurred.", source.getName(), BATTLECRY_NAME);
		} else {
			return String.format("A %s occurred.", BATTLECRY_NAME);
		}
	}
}
