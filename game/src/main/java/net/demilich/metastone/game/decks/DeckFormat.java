package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.spells.desc.condition.ConditionDesc;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The sets that are available to build decks from and generate cards from.
 * <p>
 *
 * @see GameContext#getDeckFormat() for the property on the game context where the deck format is set
 * @see #getSmallestSupersetFormat(GameDeck...) to determine the smallest format that can be used for the specified
 * decks
 */
public class DeckFormat implements Serializable {
	private String name = "";
	private Set<String> sets;
	private String[] secondPlayerBonusCards = new String[0];
	private ConditionDesc validDeckCondition;

	public static DeckFormat ALL = new DeckFormat()
			.withName("All");

	public static void populateAll(List<String> sets) {
		ALL.sets.clear();
		for (String set : sets) {
			ALL.addSet(set);
		}
	}

	private static Map<String, DeckFormat> FORMATS = new HashMap<>();

	public static void populateFormats(CardList formatCards) {
		FORMATS.put("All", ALL);
		for (Card formatCard : formatCards) {
			FORMATS.put(formatCard.getName(), new DeckFormat()
					.setSecondPlayerBonusCards(formatCard.getDesc().getSecondPlayerBonusCards())
					.setValidDeckCondition(formatCard.getDesc().getCondition())
					.withName(formatCard.getName())
					.withCardSets(formatCard.getCardSets()));
		}
	}

	public static DeckFormat getFormat(String name) {
		return FORMATS.get(name);
	}

	public static Map<String, DeckFormat> formats() {
		return FORMATS;
	}

	public static DeckFormat getSmallestSupersetFormat(Set<String> requiredSets) {
		DeckFormat smallestFormat = DeckFormat.getFormat("All");
		int minExcess = smallestFormat.sets.size();

		for (Map.Entry<String, DeckFormat> format : DeckFormat.formats().entrySet()) {
			Set<String> formatSets = format.getValue().getCardSets();
			if (!formatSets.containsAll(requiredSets)) {
				continue;
			}

			int excess = formatSets.size() - requiredSets.size();
			if (excess < minExcess) {
				smallestFormat = format.getValue();
				minExcess = excess;
			}
		}

		return smallestFormat;
	}

	public static DeckFormat getSmallestSupersetFormat(List<GameDeck> deckPair) {
		return deckPair.get(0).getFormat().equals(deckPair.get(1).getFormat())
				? deckPair.get(0).getFormat()
				: DeckFormat.getSmallestSupersetFormat(deckPair.stream().flatMap(deck -> deck.getCards().stream())
				.map(Card::getCardSet).collect(Collectors.toSet()));
	}

	public static DeckFormat getSmallestSupersetFormat(GameDeck... decks) {
		return getSmallestSupersetFormat(Arrays.asList(decks));
	}

	public DeckFormat() {
		sets = new HashSet<>();
	}

	/**
	 * The current {@code Spellsource} format containing all Spellsource sets.
	 *
	 * @return A format, or {@code null} if either Spellsource cards are not on your classpath or {@link
	 * CardCatalogue#loadCardsFromPackage()} has not been called. OSGi-friendly.
	 */
	public static DeckFormat spellsource() {
		var format = getFormat("Spellsource");
		if (format == null) {
			throw new NullPointerException("must load cards first with CardCatalogue.loadCardsFromPackage()");
		}
		return format;
	}

	public static DeckFormat all() {
		return getFormat("All");
	}

	public DeckFormat addSet(String cardSet) {
		sets.add(cardSet);
		return this;
	}

	public boolean isInFormat(Card card) {
		if (sets.contains(card.getCardSet())) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isInFormat(String set) {
		return set != null && sets.contains(set);
	}

	public Set<String> getCardSets() {
		return sets;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DeckFormat withName(String name) {
		this.name = name;
		return this;
	}

	public DeckFormat withCardSets(String... cardSets) {
		for (String cardSet : cardSets) {
			addSet(cardSet);
		}
		return this;
	}

	public DeckFormat withCardSets(Iterable<String> cardSets) {
		for (String cardSet : cardSets) {
			addSet(cardSet);
		}
		return this;
	}

	public static String latestHearthstoneExpansion() {
		return "RISE_OF_SHADOWS";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DeckFormat)) return false;
		DeckFormat that = (DeckFormat) o;
		return name.equals(that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	public String[] getSecondPlayerBonusCards() {
		return secondPlayerBonusCards;
	}

	public DeckFormat setSecondPlayerBonusCards(String[] secondPlayerBonusCards) {
		this.secondPlayerBonusCards = secondPlayerBonusCards;
		return this;
	}

	public ConditionDesc getValidDeckCondition() {
		return validDeckCondition;
	}

	public DeckFormat setValidDeckCondition(ConditionDesc validDeckCondition) {
		this.validDeckCondition = validDeckCondition;
		return this;
	}
}

