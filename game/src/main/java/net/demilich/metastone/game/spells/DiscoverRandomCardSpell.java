package net.demilich.metastone.game.spells;

import java.util.Map;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCollection;
import net.demilich.metastone.game.cards.CardCollectionImpl;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.filter.FilterArg;
import net.demilich.metastone.game.targeting.EntityReference;

public class DiscoverRandomCardSpell extends Spell {

	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = SpellDesc.build(DiscoverRandomCardSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		HeroClass heroClass = (HeroClass) cardFilter.getArg(FilterArg.HERO_CLASS);
		if (heroClass == null) {
			heroClass = player.getHero().getHeroClass();
		}
		if (heroClass == HeroClass.OPPONENT) {
			heroClass = context.getOpponent(player).getHero().getHeroClass();
		}
		if (heroClass != HeroClass.ANY && !heroClass.isBaseClass()) {
			Card card = context.getPendingCard();
			if (card != null) {
				heroClass = card.getHeroClass();
				if (heroClass == HeroClass.ANY) {
					heroClass = SpellUtils.getRandomHeroClass();
				}
			} else {
				// see if the source is a minion and use its class
				if (source instanceof Minion) {
					heroClass = ((Minion) source).getHeroClass();
				} else {
					heroClass = SpellUtils.getRandomHeroClass();
				}
			}

		}
		CardCollection cards = new CardCollectionImpl();
		if (heroClass == HeroClass.ANY) {
			CardCollection classCards = CardCatalogue.query(context.getDeckFormat());
			cards.addAll(classCards);
		} else {
			cards.addAll(CardCatalogue.query(context.getDeckFormat(), HeroClass.ANY));
			CardCollection classCards = CardCatalogue.query(context.getDeckFormat(), heroClass);
			for (int i = 0; i < 4; i++) {
				cards.addAll(classCards);
			}
		}

		CardCollection result = new CardCollectionImpl();
		for (Card card : cards) {
			if (cardFilter.matches(context, player, card)) {
				result.addCard(card);
			}
		}
		cards = new CardCollectionImpl();

		int count = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		for (int i = 0; i < count; i++) {
			if (!result.isEmpty()) {
				Card card = null;
				do {
					card = result.getRandom();
					result.remove(card);
				} while (cards.containsCard(card));
				if (card != null) {
					cards.addCard(card);
				}
			}
		}

		if (!cards.isEmpty()) {
			SpellUtils.castChildSpell(context, player, SpellUtils.getDiscover(context, player, desc, cards.getCopy()).getSpell(), source, target);
		}
	}

}
