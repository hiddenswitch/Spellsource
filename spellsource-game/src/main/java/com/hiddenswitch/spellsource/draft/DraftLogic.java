package com.hiddenswitch.spellsource.draft;

import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Implements a basic draft where the user is given a selection of champions from all {@link DeckFormat#spellsource()}
 * champions, and 30 rounds of 3-card choices pulled from a weighted selection of {@link DeckFormat#spellsource()}
 * catalogue cards.
 */
public class DraftLogic {
	private static final Logger logger = LoggerFactory.getLogger(DraftLogic.class);
	/**
	 * The number of cards per round the player will choose from.
	 */
	public static int CARDS_PER_ROUND = 3;
	/**
	 * Indicates the number of rounds of choices the user will make among {@link #CARDS_PER_ROUND} options to build a deck
	 * for a draft.
	 */
	public static int ROUNDS = 30;
	/**
	 * Indicates the additional odds the latest expansion gets in appearing in a draft.
	 */
	private static final float EXPANSION_ODDS_FACTOR = 4.0f;
	/**
	 * Indicates the percent chance a {@link Rarity#COMMON} card is chosen in the {@link #CARDS_PER_ROUND}-card pool of
	 * choices.
	 */
	private static final float COMMON_ROLL = 0.50f;
	/**
	 * Indicates the percent chance a {@link Rarity#RARE} card is shown in the {@link #CARDS_PER_ROUND}-card pool of
	 * choices.
	 */
	private static final float RARE_ROLL = 0.30f;
	/**
	 * Indicates the percent chance an {@link Rarity#EPIC} card is shown in the {@link #CARDS_PER_ROUND}-card pool of
	 * choices.
	 * <p>
	 * {@link Rarity#LEGENDARY} cards are shown with chance {@code 1 - EPIC_ROLL - RARE_ROLL - COMMON_ROLL}.
	 */
	private static final float EPIC_ROLL = 0.15f;
	private final WeakReference<DraftContext> context;


	/**
	 * Creates an instance associated weakly with the specified context.
	 * <p>
	 * As a side effect, this will initialize the context's {@link PrivateDraftState} if it is {@code null}.
	 *
	 * @param context
	 */
	public DraftLogic(DraftContext context) {
		this.context = new WeakReference<>(context);

		if (getContext().getPrivateState() == null) {
			getContext().setPrivateState(new PrivateDraftState());
		}
	}

	/**
	 * Begins a draft, initializing the {@link PublicDraftState} on the context and notifying the behaviour that the state
	 * has changed.
	 */
	public void initializeDraft() {
		if (getContext().getPublicState() == null) {
			getContext().setPublicState(new PublicDraftState());
		}

		requireDraftStatus(DraftStatus.NOT_STARTED);
		getContext().getPublicState().setHeroClassChoices(createHeroChoices());
		getContext().getPublicState().setStatus(DraftStatus.SELECT_HERO);
		notifyPublicStateChanged();
	}

	/**
	 * Starts the draft given the choice of the specified hero class.
	 *
	 * @param heroClass
	 */
	public void startDraft(String heroClass) {
		requireDraftStatus(DraftStatus.SELECT_HERO);
		// Determine the cards available to this player for the draft.
		// For now, do not make later parts of the draft dependent on earlier parts.
		getContext().getPublicState().setHeroClass(heroClass);
		getContext().getPrivateState().setCards(createDraftCards(heroClass));
		// Initialize the first card choices
		getContext().getPublicState().setCurrentCardChoices(getContext().getPrivateState().getCards().get(0));
		getContext().getPublicState().setStatus(DraftStatus.IN_PROGRESS);
		notifyPublicStateChanged();
	}

	protected void requireDraftStatus(DraftStatus requiredStatus) {
		if (getContext().getPublicState().getStatus() != requiredStatus) {
			throw new InvalidDraftStatusException(getContext().getPublicState().getStatus(), requiredStatus);
		}
	}

	private List<String> createHeroChoices() {
		return HeroClass.getBaseClasses(DeckFormat.spellsource());
	}

	/**
	 * Creates the list of cards that will appear in the draft.
	 * <p>
	 * Uses the {@link DeckFormat#spellsource()} sets and sets {@link CardSet#SPELLSOURCE_BASIC} to be {@link
	 * #EXPANSION_ODDS_FACTOR} more likely to appear.
	 *
	 * @param hero
	 * @return
	 */
	private List<List<String>> createDraftCards(String hero) {
		ArrayList<List<Card>> draftCards = new ArrayList<>(ROUNDS);

		List<String> equals = Arrays.asList(
				CardSet.CUSTOM
		);

		// Until we have enough mean streets cards, don't use it
		String latestExpansion = CardSet.SPELLSOURCE_BASIC;

		Set<CardType> validCardTypes = new HashSet<>(Arrays.asList(CardType.values()));
		Set<String> bannedCards = new HashSet<>(CardCatalogue.getBannedDraftCards());

		CardCatalogue.getAll().stream()
				.filter(Card::isQuest)
				.map(Card::getCardId)
				.forEach(bannedCards::add);

		for (int draft = 0; draft < ROUNDS; draft++) {
			// Select a rarity at the appropriate frequency
			float rarityRoll = roll();
			Rarity rarity;
			if (rarityRoll < COMMON_ROLL) {
				rarity = Rarity.COMMON;
			} else if (rarityRoll < COMMON_ROLL + RARE_ROLL) {
				rarity = Rarity.RARE;
			} else if (rarityRoll < COMMON_ROLL + RARE_ROLL + EPIC_ROLL) {
				rarity = Rarity.EPIC;
			} else {
				rarity = Rarity.LEGENDARY;
			}

			// Select the card set. The latest expansion gets a 50% bonus
			List<Card> draftChoices = new ArrayList<>(CARDS_PER_ROUND);

			while (draftChoices.stream().map(Card::getCardId).distinct().count() < CARDS_PER_ROUND) {
				float cardSetRoll = roll();
				DeckFormat format = new DeckFormat();
				float latestExpansionOdds = EXPANSION_ODDS_FACTOR / (equals.size() + EXPANSION_ODDS_FACTOR);
				if (cardSetRoll < latestExpansionOdds) {
					format.withCardSets(latestExpansion);
				} else {
					format.withCardSets(equals);
				}

				// Get neutral and hero cards
				CardList classCards = CardCatalogue.query(format, c -> {
					return c.hasHeroClass(hero)
							&& !bannedCards.contains(c.getCardId())
							&& c.getRarity() == rarity
							&& validCardTypes.contains(c.getCardType())
							&& c.isCollectible();
				});

				CardList neutralCards = CardCatalogue.query(format, c -> {
					return c.hasHeroClass(HeroClass.ANY)
							&& !bannedCards.contains(c.getCardId())
							&& c.getRarity() == rarity
							&& validCardTypes.contains(c.getCardType())
							&& c.isCollectible();
				});

				// Total five copies of the class cards and then the neutrals
				CardList cards = classCards.clone()
						.addAll(classCards)
						.addAll(classCards)
						.addAll(classCards)
						.addAll(classCards)
						.addAll(neutralCards);

				if (cards.getCount() == 0) {
					logger.info("Draft pulled no cards given parameters: draft={}, rarity={}, sets={}", draft, rarity, format.getCardSets());
					continue;
				}

				// Shuffle then choose until we're done
				cards.shuffle(getRandom());

				final Card nextCard = cards.removeFirst();

				if (draftChoices.stream().anyMatch(c -> Objects.equals(c.getCardId(), nextCard.getCardId()))) {
					continue;
				}

				draftChoices.add(nextCard);
			}

			draftCards.add(draftChoices);
		}
		return draftCards.stream().map(d -> d.stream().map(Card::getCardId).collect(toList())).collect(toList());
	}

	private float roll() {
		return getRandom().nextFloat();
	}

	private XORShiftRandom getRandom() {
		return getContext().getPrivateState().getRandom();
	}

	/**
	 * Selects a card from teh current choice index.
	 *
	 * @param choiceIndex
	 * @throws InvalidDraftCardSelectionException if the choice does not exist or the state is invalid.
	 */
	public void selectCard(int choiceIndex) {
		final PublicDraftState publicState = getContext().getPublicState();
		final List<String> selectedCards = publicState.getSelectedCards();
		final List<List<String>> choices = getContext().getPrivateState().getCards();

		requireDraftStatus(DraftStatus.IN_PROGRESS);

		// Are we making an invalid choice?
		int draftIndex = getContext().getPublicState().getDraftIndex();
		if (choiceIndex >= choices.get(draftIndex).size()
				|| choiceIndex < 0) {
			throw new InvalidDraftCardSelectionException(choiceIndex, getContext().getPrivateState());
		}

		String chosenCard = choices.get(draftIndex).get(choiceIndex);
		selectedCards.add(chosenCard);


		publicState.setCardsRemaining(publicState.getCardsRemaining() - 1);
		publicState.setDraftIndex(publicState.getDraftIndex() + 1);

		if (isDraftOver()) {
			publicState.setCurrentCardChoices(Collections.emptyList());
			publicState.setStatus(DraftStatus.COMPLETE);
		} else {
			publicState.setCurrentCardChoices(getContext().getPrivateState().getCards().get(publicState.getDraftIndex()));
		}

		notifyPublicStateChanged();
	}

	private void notifyPublicStateChanged() {
		getContext().notifyPublicStateChanged();
	}

	/**
	 * Is the draft over?
	 *
	 * @return {@code true} if the number of cards remaining is equal to 0.
	 */
	public boolean isDraftOver() {
		return getContext().getPublicState().getCardsRemaining() == 0;
	}

	/**
	 * Gets a reference to the draft context. Held weakly by this instance.
	 *
	 * @return
	 */
	public DraftContext getContext() {
		return context.get();
	}

	/**
	 * Gets a list of card choices currently available to the player.
	 *
	 * @return The choices, or {@link Collections#emptyList()} if there are none.
	 */
	public List<String> getCardChoices() {
		return getContext().getPublicState().getCurrentCardChoices();
	}
}
