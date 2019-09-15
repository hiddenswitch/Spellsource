package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.entities.Entity;

/**
 * A filter that matches an entity if the base card ID is matched by the {@link EntityFilterArg#CARD} property.
 * <p>
 * {@link net.demilich.metastone.game.spells.aura.CardAura} auras will change the card ID of a card, making it difficult
 * to apply a card aura to a specific card; as soon as the card changes, the aura no longer applies to it.
 * <p>
 * Use this filter in the {@code "filter"} property of a card aura if you are implementing text like, "All Lunstones are
 * Fireballs instead."
 */
public class CardAuraCardFilter extends SpecificCardFilter {

	public CardAuraCardFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		if (entity == null) {
			return false;
		}
		Card sourceCard = entity.getSourceCard();
		if (sourceCard == null) {
			return false;
		}
		CardDesc desc = sourceCard.getNonOverriddenDesc();
		if (desc == null) {
			return false;
		}
		return getDesc().getCardOrCards().contains(desc.getId());
	}
}
