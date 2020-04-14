package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Entity;
import com.hiddenswitch.spellsource.client.models.EntityType;

/**
 * Returns entities that have been played by {@link EntityFilterArg#TARGET_PLAYER}. In other words, this lets you filter
 * a card that hasn't been played but whose copies have.
 */
public final class CopiesHaveBeenPlayedFilter extends EntityFilter {

	public CopiesHaveBeenPlayedFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		return context.getEntities()
				.anyMatch(e ->
						e.getOwner() == player.getOwner()
								&& e.getEntityType() == EntityType.CARD
								&& e.hasAttribute(Attribute.PLAYED_FROM_HAND_OR_DECK)
								&& e.getSourceCard().getCardId().equals(entity.getSourceCard().getCardId()));
	}
}
