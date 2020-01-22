package net.demilich.metastone.game.spells.aura;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.spells.SetCardSpell;
import net.demilich.metastone.game.spells.desc.aura.AuraArg;
import net.demilich.metastone.game.spells.desc.aura.AuraDesc;
import net.demilich.metastone.game.spells.trigger.CardReceivedTrigger;
import net.demilich.metastone.game.targeting.Zones;

import java.util.List;
import java.util.Objects;

/**
 * Changes the targeted card's card ID to match the specified {@link AuraArg#CARD}, allowing its behaviour to change.
 * <p>
 * When a {@link AuraArg#FILTER} is specified, the filter will be evaluated against the base card instead of the actual
 * target. Otherwise, the aura would immediately stop applying after the card was changed.
 * <p>
 * For example, to change your hero power to DIE, INSECT while the aura's host actor is in play:
 * <pre>
 *   {
 *     "class": "CardAura",
 *     "target": "FRIENDLY_HERO_POWER",
 *     "card": "hero_power_die_insect"
 *   }
 * </pre>
 */
public class CardAura extends AbstractFriendlyCardAura {

	public CardAura(AuraDesc desc) {
		super(desc);
		applyAuraEffect = SetCardSpell.create((String) desc.get(AuraArg.CARD), true);
		removeAuraEffect = SetCardSpell.revert(true);
		triggers.add(CardReceivedTrigger.create());
	}

	@Override
	protected boolean affects(GameContext context, Player player, Entity target, List<Entity> resolvedTargets) {
		if (target.getZone() == Zones.REMOVED_FROM_PLAY) {
			return false;
		}

		if (!target.getEntityType().hasEntityType(EntityType.CARD)) {
			return false;
		}

		Entity source = context.resolveSingleTarget(getHostReference());

		Card originalCard = context.getCardById(((Card) target).getOriginalCardId());
		if (getEntityFilter() != null && !getEntityFilter().matches(context, player, originalCard, source)) {
			return false;
		}

		boolean conditionFulfilled = getCondition() == null || getCondition().isFulfilled(context, player, originalCard, source);

		return conditionFulfilled && resolvedTargets.contains(target);
	}

	@Override
	protected boolean notApplied(Entity target) {
		return !Objects.equals(target.getSourceCard().getCardId(), getDesc().get(AuraArg.CARD));
	}
}

