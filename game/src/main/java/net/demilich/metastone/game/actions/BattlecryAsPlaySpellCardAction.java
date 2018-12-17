package net.demilich.metastone.game.actions;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.targeting.TargetSelection;

/**
 * Allows a battlecry to be encapsulated as a spell card.
 * <p>
 * Implements Shudderwock.
 */
public class BattlecryAsPlaySpellCardAction extends PlaySpellCardAction {

	private static final long serialVersionUID = 7634084466766601461L;
	private final EntityReference sourceMinion;

	public BattlecryAsPlaySpellCardAction(EntityReference sourceMinion, SpellDesc battlecrySpell, Card card, TargetSelection targetSelection) {
		super(battlecrySpell, card, targetSelection);
		this.sourceMinion = sourceMinion;
	}

	@Override
	public boolean canBeExecutedOn(GameContext context, Player player, Entity entity) {
		EntityFilter filter = getSpell().getEntityFilter();
		if (filter == null) {
			return true;
		}
		return filter.matches(context, player, entity, context.resolveSingleTarget(sourceMinion));
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
