package net.demilich.metastone.game.cards.costmodifier;

import net.demilich.metastone.game.spells.desc.manamodifier.CardCostModifierDesc;

/**
 * A card cost modifier that lasts only one turn.
 */
public final class OneTurnCostModifier extends CardCostModifier {
	public OneTurnCostModifier(CardCostModifierDesc desc) {
		super(desc);
		oneTurn = true;
	}

	@Override
	public boolean oneTurnOnly() {
		return true;
	}
}
