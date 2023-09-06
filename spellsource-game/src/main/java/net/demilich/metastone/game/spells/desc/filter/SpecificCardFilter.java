package net.demilich.metastone.game.spells.desc.filter;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.SpellUtils;
import net.demilich.metastone.game.spells.aura.Aura;
import net.demilich.metastone.game.spells.aura.SpecificCardFilterOverrideAura;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.targeting.EntityReference;

import java.util.*;

/**
 * A card or actor will pass this filter if its {@link Entity#getSourceCard()} {@link Card#getCardId()} matches the
 * {@link EntityFilterArg#CARD} argument.
 * <p>
 * If a {@link EntityFilterArg#CARDS} argument is specified, passes the filter if the {@code target}'s source card
 * matches any card in the list.
 * <p>
 * If a {@link EntityFilterArg#SECONDARY_TARGET} is specified, the card or actor will pass the filter if its card ID
 * matches the card ID of the secondary target.
 */
public class SpecificCardFilter extends EntityFilter {

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
		Set<String> requiredCardIds = new LinkedHashSet<>(getDesc().getCardOrCards());
		EntityReference comparedTo = (EntityReference) getDesc().get(EntityFilterArg.SECONDARY_TARGET);
		if (comparedTo != null && !comparedTo.equals(EntityReference.NONE)) {
			Entity target = context.resolveSingleTarget(player, host, comparedTo);
			if (target != null) {
				requiredCardIds = Collections.singleton(target.getSourceCard().getCardId());
			}
		}

		List<SpecificCardFilterOverrideAura> filterAuras = SpellUtils.getAuras(context, player.getId(), SpecificCardFilterOverrideAura.class);
		if (!filterAuras.isEmpty()) {
			for (Aura aura : filterAuras) {
				String overrideCardId = aura.getDesc().getString(AuraArg.CARD);
				if (aura.getAffectedEntities().contains(entity.getId()) && overrideCardId != null && requiredCardIds.contains(overrideCardId)) {
					return true;
				}
			}
		}

		return requiredCardIds.contains(cardId);
	}
}

