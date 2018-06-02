package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.List;

/**
 * A card or actor will pass this filter if its {@link Entity#getSourceCard()} {@link Card#getCardId()} matches the
 * {@link EntityFilterArg#CARD} argument.
 *
 * If a {@link EntityFilterArg#SECONDARY_TARGET} is specified, the card or actor will pass the filter if its card ID matches the card ID of the secondary target.
 */
public class SpecificCardFilter extends EntityFilter {

	public SpecificCardFilter(EntityFilterDesc desc) {
		super(desc);
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		String cardId = entity.getSourceCard().getCardId();
		String requiredCardId = getDesc().getString(EntityFilterArg.CARD);
		EntityReference comparedTo = (EntityReference) getDesc().get(EntityFilterArg.SECONDARY_TARGET);
		if (comparedTo != null
				&& !comparedTo.equals(EntityReference.NONE)) {
			List<Entity> entities = context.resolveTarget(player, host, comparedTo);
			if (entities != null &&
					!entities.isEmpty()) {
				requiredCardId = entities.get(0).getSourceCard().getCardId();
			}
		}

		return cardId.equalsIgnoreCase(requiredCardId);
	}

}

