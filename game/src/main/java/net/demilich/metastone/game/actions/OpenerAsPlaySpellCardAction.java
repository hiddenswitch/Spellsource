package net.demilich.metastone.game.actions;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.condition.Condition;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

/**
 * Allows an opener to be encapsulated as a spell card.
 */
public final class OpenerAsPlaySpellCardAction extends PlaySpellCardAction {

	private final EntityReference sourceMinion;
	private final Condition condition;

	public OpenerAsPlaySpellCardAction(EntityReference sourceMinion, SpellDesc battlecrySpell, Card card, TargetSelection targetSelection, Condition condition) {
		super(battlecrySpell, card, targetSelection);
		this.sourceMinion = sourceMinion;
		this.condition = condition;
	}

	@Override
	@Suspendable
	public boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		EntityFilter filter = getSpell().getEntityFilter();
		Entity source = context.resolveSingleTarget(sourceMinion);
		boolean conditionPasses = condition == null || condition.isFulfilled(context, player, source, entity);
		boolean matches = filter == null || filter.matches(context, player, entity, source);
		return conditionPasses && matches;
	}

	@Override
	public EntityReference getSourceReference() {
		return sourceMinion;
	}

	@Override
	public OpenerAsPlaySpellCardAction clone() {
		return (OpenerAsPlaySpellCardAction) super.clone();
	}
}
