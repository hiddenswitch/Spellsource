package net.demilich.metastone.game.actions;

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
 * Allows a battlecry to be encapsulated as a spell card.
 * <p>
 * Implements Shudderwock.
 */
public class BattlecryAsPlaySpellCardAction extends PlaySpellCardAction {

	private final EntityReference sourceMinion;
	private final Condition condition;

	public BattlecryAsPlaySpellCardAction(EntityReference sourceMinion, SpellDesc battlecrySpell, Card card, TargetSelection targetSelection, Condition condition) {
		super(battlecrySpell, card, targetSelection);
		this.sourceMinion = sourceMinion;
		this.condition = condition;
	}

	@Override
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
	public BattlecryAsPlaySpellCardAction clone() {
		return (BattlecryAsPlaySpellCardAction) super.clone();
	}
}
