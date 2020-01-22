package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;

public final class HasAttackedLastTurnFilter extends HasAttackedFilter {

	public HasAttackedLastTurnFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		if (entity.getOwner() == context.getActivePlayerId()) {
			return entity.getAttributeValue(Attribute.ATTACKS_LAST_TURN) > 0;
		} else {
			return entity.getAttributeValue(Attribute.ATTACKS_THIS_TURN) > 0;
		}
	}
}
