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

	public SpecificCardFilter(EntityFilterDesc desc) {
		super(desc);
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

		List<Aura> filterAuras = context.getTriggerManager().getTriggers().stream()
				.filter(t -> t instanceof SpecificCardFilterOverrideAura)
				.map(t -> (Aura) t)
				.filter(((Predicate<Aura>) Aura::isExpired).negate())
				.filter(aura -> aura.getAffectedEntities().contains(entity.getId()))
				.filter(aura -> aura.getCondition() == null || aura.getCondition().isFulfilled(context,
						context.getPlayer(aura.getOwner()), context.resolveSingleTarget(aura.getHostReference()), null))
				.collect(Collectors.toList());
		if (filterAuras != null && !filterAuras.isEmpty()) {
			for (Aura aura : filterAuras) {
				String filterCardId = aura.getDesc().getString(AuraArg.CARD);
				if (filterCardId != null && filterCardId.equalsIgnoreCase(requiredCardId)) {
					return true;
				}
			}
		}


		if (!requiredCardId.contains("_")) {                              //functionality to search for cardIds containing a specific key word,
			return cardId.length() > cardId.replace(requiredCardId, "").length();  //known by the creator not including the usual underscore of a cardId
		} else return cardId.equalsIgnoreCase(requiredCardId);


	}

}

