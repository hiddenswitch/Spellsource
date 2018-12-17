package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.aura.SpecificCardFilterOverrideAura;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A card or actor will pass this filter if its {@link Entity#getSourceCard()} {@link Card#getCardId()} matches the
 * {@link EntityFilterArg#CARD} argument.
 * <p>
 * If a {@link EntityFilterArg#SECONDARY_TARGET} is specified, the card or actor will pass the filter if its card ID
 * matches the card ID of the secondary target.
 */
public class SpecificCardFilter extends EntityFilter {

	private static final long serialVersionUID = -3576411127059638716L;

	public SpecificCardFilter(EntityFilterDesc desc) {
		super(desc);
	}

	public static EntityFilterDesc create(String cardId) {
		EntityFilterDesc desc = new EntityFilterDesc(SpecificCardFilter.class);
		desc.put(EntityFilterArg.CARD, cardId);
		return desc;
	}

	@Override
	protected boolean test(GameContext context, Player player, Entity entity, Entity host) {
		if (entity == null) {
			return false;
		}

		String cardId = entity.getSourceCard().getCardId();
		String requiredCardId = getDesc().getString(EntityFilterArg.CARD);
		EntityReference comparedTo = (EntityReference) getDesc().get(EntityFilterArg.SECONDARY_TARGET);
		if (comparedTo != null && !comparedTo.equals(EntityReference.NONE)) {
			Entity target = context.resolveSingleTarget(player, host, comparedTo);
			if (target != null) {
				requiredCardId = target.getSourceCard().getCardId();
			}
		}

		List<SpecificCardFilterOverrideAura> filterAuras = SpellUtils.getAuras(context, player.getId(), SpecificCardFilterOverrideAura.class);
		if (!filterAuras.isEmpty()) {
			for (Aura aura : filterAuras) {
				String overrideCardId = aura.getDesc().getString(AuraArg.CARD);
				if (aura.getAffectedEntities().contains(entity.getId()) && overrideCardId != null && overrideCardId.equalsIgnoreCase(requiredCardId)) {
					return true;
				}
			}
		}

		return cardId.equalsIgnoreCase(requiredCardId);
	}

}

