package net.demilich.metastone.game.spells;

import java.util.List;
import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.environment.Environment;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.targeting.EntityReference;

import static java.util.stream.Collectors.toList;

public class SummonNewAttackTargetSpell extends Spell {
	protected static MinionCard getRandomMatchingMinionCard(GameContext context, Player player, EntityFilter cardFilter, CardSource cardSource, Entity source) {
		CardList relevantMinions = null;
		if (cardSource != null) {
			CardList allCards = cardSource.getCards(context, player);
			relevantMinions = new CardArrayList();
			for (Card card : allCards) {
				if (card.getCardType().isCardType(CardType.MINION) && (cardFilter == null || cardFilter.matches(context, player, card, source))) {
					relevantMinions.addCard(card);
				}
			}
		} else {
			CardList allMinions = CardCatalogue.query(context.getDeckFormat(), CardType.MINION);
			relevantMinions = new CardArrayList();
			for (Card card : allMinions) {
				if (cardFilter == null || cardFilter.matches(context, player, card, source)) {
					relevantMinions.addCard(card);
				}
			}
		}

		return (MinionCard) relevantMinions.getRandom();
	}

	public static SpellDesc create(MinionCard minionCard) {
		Map<SpellArg, Object> arguments = SpellDesc.build(SummonNewAttackTargetSpell.class);
		arguments.put(SpellArg.CARD, minionCard);
		arguments.put(SpellArg.TARGET, EntityReference.NONE);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		MinionCard minionCard = (MinionCard) SpellUtils.getCard(context, desc);
		if (minionCard == null) {
			minionCard = getRandomMatchingMinionCard(context, player, cardFilter, cardSource, source);
		}

		Minion targetMinion = minionCard.summon();
		context.getLogic().summon(player.getId(), targetMinion, null, -1, false);
		List<SpellDesc> spells = desc.subSpells().collect(toList());
		if (targetMinion.getOwner() > -1) {
			context.getEnvironment().put(Environment.TARGET_OVERRIDE, targetMinion.getReference());
			// Implements Voraxx
			if (spells.size() > 0) {
				context.getEnvironment().remove(Environment.TARGET_OVERRIDE);
				for (SpellDesc spell : spells) {
					SpellUtils.castChildSpell(context, player, spell, source, targetMinion);
				}
			}
		}
	}
}
