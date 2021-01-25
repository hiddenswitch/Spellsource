package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.filter.AndFilter;
import net.demilich.metastone.game.spells.desc.filter.EntityFilter;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CatalogueSource;
import net.demilich.metastone.game.targeting.EntityReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

/**
 * This class is currently only used for tri-class card discoveries.
 */
public class DiscoverFilteredCardSpell extends DiscoverSpell {

	private static Logger LOGGER = LoggerFactory.getLogger(DiscoverFilteredCardSpell.class);

	public static SpellDesc create(EntityReference target, SpellDesc spell) {
		Map<SpellArg, Object> arguments = new SpellDesc(DiscoverFilteredCardSpell.class);
		arguments.put(SpellArg.TARGET, target);
		arguments.put(SpellArg.SPELL, spell);
		return new SpellDesc(arguments);
	}

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		EntityFilter cardFilter = (EntityFilter) desc.get(SpellArg.CARD_FILTER);
		EntityFilter[] cardFilters = (EntityFilter[]) desc.get(SpellArg.CARD_FILTERS);
		CardSource cardSource = (CardSource) desc.get(SpellArg.CARD_SOURCE);
		CardSource[] cardSources = (CardSource[]) desc.get(SpellArg.CARD_SOURCES);
		int howMany = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		CardList discoverCards = new CardArrayList();

		if (cardSources == null) {
			cardSources = new CardSource[howMany];
			if (cardSource == null) {
				cardSource = CatalogueSource.create();
			}
			Arrays.fill(cardSources, cardSource);
		}

		if (cardFilters == null) {
			cardFilters = new EntityFilter[howMany];
			if (cardFilter == null) {
				cardFilter = AndFilter.create();
			}
			Arrays.fill(cardFilters, cardFilter);
		}

		if (cardFilters.length != howMany) {
			LOGGER.error("onCast {} {}: Incorrect number of card filters. Expected {}, got {}", context.getGameId(), source, howMany, cardFilters.length);
			return;
		}

		if (cardSources.length != howMany) {
			LOGGER.error("onCast {} {}: Incorrect number of card sources. Expected {}, got {}", context.getGameId(), source, howMany, cardSources.length);
			return;
		}

		for (int i = 0; i < howMany; i++) {
			Card card = context.getLogic().getRandom(
					cardSources[i].getCards(context, source, player)
							.filtered(cardFilters[i].matcher(context, player, source)));
			if (card == null) {
				throw new NullPointerException("card");
			}
			discoverCards.add(card);
		}


		if (!discoverCards.isEmpty()) {
			SpellUtils.castChildSpell(context, player, SpellUtils.discoverCard(context, player, source, desc, discoverCards.getCopy()).getSpell(), source, target);
		}
	}

}
