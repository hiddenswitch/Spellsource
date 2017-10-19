package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

import java.util.Collections;
import java.util.List;

public class BattlecryAction extends GameAction {
	private static final String BATTLECRY_NAME = "Call to Power";

	public static BattlecryAction createBattlecry(SpellDesc spell) {
		return createBattlecry(spell, TargetSelection.NONE);
	}

	public static BattlecryAction createBattlecry(SpellDesc spell, TargetSelection targetSelection) {
		BattlecryAction battlecry = new BattlecryAction(spell);
		battlecry.setTargetRequirement(targetSelection);
		return battlecry;
	}

	private final SpellDesc spell;
	private Condition condition;

	protected BattlecryAction(SpellDesc spell) {
		this.spell = spell;
		setActionType(ActionType.BATTLECRY);
	}

	public boolean canBeExecuted(GameContext context, Player player) {
		if (getCondition() == null) {
			return true;
		}
		return getCondition().isFulfilled(context, player, null, null);
	}

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

	@Override
	public BattlecryAction clone() {
		BattlecryAction clone = BattlecryAction.createBattlecry(getSpell(), getTargetRequirement());
		clone.setSource(getSourceReference());
		return clone;
	}

	@Override
	@Suspendable
	public void execute(GameContext context, int playerId) {
		EntityReference target = getPredefinedSpellTargetOrUserTarget();
		context.getLogic().castSpell(playerId, getSpell(), getSourceReference(), target, getTargetRequirement(), false);
	}

	public EntityReference getPredefinedSpellTargetOrUserTarget() {
		return getSpell().hasPredefinedTarget() ? getSpell().getTarget() : getTargetReference();
	}

	private Condition getCondition() {
		return condition;
	}

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
	public String toString() {
		return String.format("[%s '%s']", getActionType(), getSpell().getSpellClass().getSimpleName());
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
			return String.format("%s's %s occurred.", BATTLECRY_NAME, source.getName());
		} else {
			return String.format("A %s occurred.", BATTLECRY_NAME);
		}
	}
}
