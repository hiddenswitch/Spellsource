package net.demilich.metastone.game.spells;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.DiscoverAction;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardArrayList;
import net.demilich.metastone.game.cards.CardList;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.spells.desc.SpellArg;
import net.demilich.metastone.game.spells.desc.SpellDesc;
import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CatalogueSource;
import net.demilich.metastone.game.spells.desc.source.HasCardCreationSideEffects;
import net.demilich.metastone.game.spells.desc.source.HasWeights;
import net.demilich.metastone.game.targeting.Zones;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.bag.HashBag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class DiscoverSpell extends Spell {

	@Override
	@Suspendable
	protected void onCast(GameContext context, Player player, SpellDesc desc, Entity source, Entity target) {
		List<Card> specificCards = Arrays.asList(SpellUtils.getCards(context, desc));
		boolean hasFilter = desc.containsKey(SpellArg.CARD_FILTER) || desc.containsKey(SpellArg.CARD_SOURCE);

		CardList filteredCards;
		if (hasFilter) {
			filteredCards = desc.getFilteredCards(context, player, source);
		} else {
			filteredCards = new CardArrayList();
		}

		int count = desc.getValue(SpellArg.HOW_MANY, context, player, target, source, 3);
		boolean cannotReceiveOwned = desc.getBool(SpellArg.CANNOT_RECEIVE_OWNED);
		// An exclusive of false indicates that the discovered card should be passed as a card ID to the spells. Otherwise,
		// the original cards should be passed to the spells. This should never be called with a CatalogueSource or from
		// cards originating in a cards variable and will throw an exception in that situation.
		boolean exclusive = desc.getBool(SpellArg.EXCLUSIVE);

		CardSource cardSource = desc.getCardSource();
		if (exclusive
				&& cardSource != null
				&& (cardSource instanceof HasCardCreationSideEffects)) {
			throw new UnsupportedOperationException("Cannot specify exclusive (use original copies) with cards that have" +
					" card creation side effects. The original copies came from the catalogue, and thus have entity " +
					"references that correspond to -1 (NONE)");
		}

		// SPELL and SPELL_1 are cast on the chosen cards
		SpellDesc chosenSpellTemplate = SpellDesc.join((SpellDesc) desc.get(SpellArg.SPELL), (SpellDesc) desc.get(SpellArg.SPELL_1));
		if (chosenSpellTemplate == null) {
			chosenSpellTemplate = ReceiveCardSpell.create();
		}

		// SPELL_2 is cast on the cards that aren't chosen
		SpellDesc otherSpell = (SpellDesc) desc.getOrDefault(SpellArg.SPELL_2, NullSpell.create());
		CardList allCards = new CardArrayList();
		allCards.addAll(specificCards);
		allCards.addAll(filteredCards);

		if (cannotReceiveOwned) {
			allCards = new CardArrayList(allCards.stream().filter(c -> !context.getLogic().hasCard(player, c)).collect(toList()));
		}

		CardList choices = new CardArrayList();
		// Apply the weights
		final boolean isWeighted = (cardSource != null
				&& cardSource instanceof HasWeights)
				|| (specificCards.size() == 0 && cardSource == null && hasFilter);

		// Compute weights if weighting is implied
		if (isWeighted) {
			if (cardSource == null) {
				cardSource = CatalogueSource.create();
			}

			final HasWeights weightedSource = (HasWeights) cardSource;
			final Bag<Card> weightedOptions = new HashBag<>();
			final TargetPlayer targetPlayer = cardSource.getTargetPlayer();

			allCards.forEach((final Card card) -> {
				final int weight;
				switch (targetPlayer) {
					case SELF:
						weight = weightedSource.getWeight(player, card);
						break;
					case OPPONENT:
						weight = weightedSource.getWeight(context.getOpponent(player), card);
						break;
					case ACTIVE:
						weight = weightedSource.getWeight(context.getActivePlayer(), card);
						break;
					case INACTIVE:
						weight = weightedSource.getWeight(context.getOpponent(context.getActivePlayer()), card);
						break;
					case OWNER:
						weight = weightedSource.getWeight(context.getPlayer(source.getOwner()), card);
						break;
					case BOTH:
					default:
						weight = 1;
						break;
				}
				if (weight > 0) {
					weightedOptions.add(card, weight);
				}
			});

			for (int i = 0; i < count; i++) {
				choices.add(context.getLogic().removeRandom(weightedOptions));
			}
		} else {
			for (int i = 0; i < count; i++) {
				choices.add(context.getLogic().removeRandom(allCards));
			}
		}

		choices.removeIf(Objects::isNull);

		if (choices.isEmpty()) {
			return;
		}

		// Always copy the choices.
		choices = choices.getCopy();

		List<GameAction> discoverActions = new ArrayList<>();
		for (int i = 0; i < choices.size(); i++) {
			Card card = choices.get(i);
			card.setId(context.getLogic().getIdFactory().generateId());
			card.setOwner(player.getId());
			card.moveOrAddTo(context, Zones.DISCOVER);

			// For each discover, it calls the chosenSpell on its card and notChosenSpell on the other cards
			List<SpellDesc> notChosenSpells;
			SpellDesc chosenSpell;
			final Stream<Card> otherCards = Stream.concat(choices.subList(0, i).stream(), choices.subList(i + 1, choices.size()).stream());
			if (exclusive) {
				chosenSpell = chosenSpellTemplate.addArg(SpellArg.TARGET, card.getCopySource());
				notChosenSpells = otherCards
						.map(Card::getCopySource)
						.map(cid -> otherSpell.addArg(SpellArg.TARGET, cid)).collect(toList());
			} else {
				chosenSpell = chosenSpellTemplate.addArg(SpellArg.CARD, card.getCardId());
				notChosenSpells = otherCards
						.map(Card::getCardId)
						.map(cid -> otherSpell.addArg(SpellArg.CARD, cid)).collect(toList());
			}

			// Construct the spell. Usually we include the parent
			final SpellDesc[] notChosenSpellsArray = new SpellDesc[notChosenSpells.size()];
			notChosenSpells.toArray(notChosenSpellsArray);
			final SpellDesc spell = SpellDesc.join(chosenSpell, notChosenSpellsArray);

			DiscoverAction discover = DiscoverAction.createDiscover(spell);
			discover.setCard(card);
			discover.setId(i);
			discoverActions.add(discover);
		}

		// Execute the discovery
		final DiscoverAction chosenAction = SpellUtils.postDiscover(context, player, choices, discoverActions);
		SpellUtils.castChildSpell(context, player, chosenAction.getSpell(), source, target);
	}
}
