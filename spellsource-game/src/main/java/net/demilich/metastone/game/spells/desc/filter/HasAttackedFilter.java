package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.cards.Attribute;

/**
 * Filters for actors that have attacked this turn.
 */
public class HasAttackedFilter extends EntityFilter {

	public HasAttackedFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		return entity.getMaxNumberOfAttacks() > entity.getAttributeValue(Attribute.NUMBER_OF_ATTACKS);
	}
}
