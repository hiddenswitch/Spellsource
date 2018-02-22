package net.demilich.metastone.game.spells.custom;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.cards.MinionCard;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.BuffSpell;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.*;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.HandSource;
import net.demilich.metastone.game.spells.desc.valueprovider.*;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.utils.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GainStatsOfHighestCostMinionSpell extends BuffSpell {
	private static Logger logger = LoggerFactory.getLogger(GainStatsOfHighestCostMinionSpell.class);

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		final ValueProvider highestCostProvider =
				ReduceValueProvider.create(
						EntityReference.FRIENDLY_HAND,
						ManaCostProvider.create(),
						CardFilter.create(CardType.MINION),
						AlgebraicOperation.MAXIMUM).createInstance();

		final int highestCost = highestCostProvider.getValue(context, player, target, source);

		final EntityFilter highestCostFilter = AndFilter.create(
				CardFilter.create(CardType.MINION),
				ManaCostFilter.create(highestCost, Operation.EQUAL)
		);

		final CardSource handSource = HandSource.create();
		final CardList eligibleMinions = handSource
				.getCards(context, source, player)
				.filtered(highestCostFilter.matcher(context, player, source));

		if (eligibleMinions.isEmpty()) {
			logger.debug("onCast {}: No minions found to gain stats from.", context.getGameId());
			return;
		}

		final MinionCard randomCard = (MinionCard) context.getLogic().getRandom(eligibleMinions);
		final int attackBonus = AttributeValueProvider.create(Attribute.ATTACK,
				randomCard.getReference()).createInstance().getValue(context, player, randomCard, source);
		final int hpBonus = AttributeValueProvider.create(Attribute.HP,
				randomCard.getReference()).createInstance().getValue(context, player, randomCard, source);

		super.onCast(context, player, BuffSpell.create(target.getReference(), attackBonus, hpBonus), source, target);
	}
}
