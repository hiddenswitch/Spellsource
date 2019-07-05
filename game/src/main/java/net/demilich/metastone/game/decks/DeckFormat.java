package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The sets that are available to build decks from and generate cards from.
 * <p>
 * The list of formats currently are {@link #STANDARD}, {@link #WILD}, {@link #PAST}, {@link #CUSTOM}, {@link
 * #GREATER_CUSTOM}, {@link #SPELLSOURCE}. {@link #SPELLSOURCE} is the default format for community games, while {@link
 * #STANDARD} is the default format for testing bots.
 *
 * @see GameContext#getDeckFormat() for the property on the game context where the deck format is set
 * @see #getSmallestSupersetFormat(GameDeck...) to determine the smallest format that can be used for the specified
 * 		decks
 */
public class DeckFormat implements Serializable, Cloneable {
	private String name = "";
	private Set<String> sets;

	public static final DeckFormat STANDARD = new DeckFormat()
			.withName("Standard")
			.withCardSets(List.of(
					"BASIC",
					"CLASSIC",
					"WITCHWOOD",
					"BOOMSDAY_PROJECT",
					"RASTAKHANS_RUMBLE",
					"RISE_OF_SHADOWS"
			));

	public static final DeckFormat WILD = new DeckFormat()
			.withName("Wild")
			.withCardSets(List.of(
					"BASIC",
					"CLASSIC",
					"REWARD",
					"PROMO",
					"NAXXRAMAS",
					"GOBLINS_VS_GNOMES",
					"BLACKROCK_MOUNTAIN",
					"THE_GRAND_TOURNAMENT",
					"LEAGUE_OF_EXPLORERS",
					"THE_OLD_GODS",
					"ONE_NIGHT_IN_KARAZHAN",
					"MEAN_STREETS_OF_GADGETZAN",
					"JOURNEY_TO_UNGORO",
					"KNIGHTS_OF_THE_FROZEN_THRONE",
					"KOBOLDS_AND_CATACOMBS",
					"WITCHWOOD",
					"BOOMSDAY_PROJECT",
					"RASTAKHANS_RUMBLE",
					"RISE_OF_SHADOWS",
					"HALL_OF_FAME"
			));

	public static final DeckFormat PAST = new DeckFormat()
			.withName("Past")
			.withCardSets(List.of(
					"REWARD",
					"PROMO",
					"NAXXRAMAS",
					"GOBLINS_VS_GNOMES",
					"BLACKROCK_MOUNTAIN",
					"THE_GRAND_TOURNAMENT",
					"LEAGUE_OF_EXPLORERS",
					"THE_OLD_GODS",
					"ONE_NIGHT_IN_KARAZHAN",
					"MEAN_STREETS_OF_GADGETZAN",
					"HALL_OF_FAME"
			));

	public static final DeckFormat CUSTOM = new DeckFormat()
			.withName("Custom")
			.withCardSets(List.of(
					"BASIC",
					"CLASSIC",
					"REWARD",
					"PROMO",
					"NAXXRAMAS",
					"GOBLINS_VS_GNOMES",
					"BLACKROCK_MOUNTAIN",
					"THE_GRAND_TOURNAMENT",
					"LEAGUE_OF_EXPLORERS",
					"THE_OLD_GODS",
					"ONE_NIGHT_IN_KARAZHAN",
					"MEAN_STREETS_OF_GADGETZAN",
					"JOURNEY_TO_UNGORO",
					"KNIGHTS_OF_THE_FROZEN_THRONE",
					"KOBOLDS_AND_CATACOMBS",
					"WITCHWOOD",
					"BOOMSDAY_PROJECT",
					"RASTAKHANS_RUMBLE",
					"RISE_OF_SHADOWS",
					"BATTLE_FOR_ASHENVALE",
					"SANDS_OF_TIME",
					"VERDANT_DREAMS",
					"HALL_OF_FAME",
					"SPELLSOURCE_BASIC",
					"CUSTOM"
			));

	public static final DeckFormat GREATER_CUSTOM = new DeckFormat()
			.withName("Greater Custom")
			.withCardSets(List.of(
					"BASIC",
					"CLASSIC",
					"REWARD",
					"PROMO",
					"NAXXRAMAS",
					"GOBLINS_VS_GNOMES",
					"BLACKROCK_MOUNTAIN",
					"THE_GRAND_TOURNAMENT",
					"LEAGUE_OF_EXPLORERS",
					"THE_OLD_GODS",
					"ONE_NIGHT_IN_KARAZHAN",
					"MEAN_STREETS_OF_GADGETZAN",
					"JOURNEY_TO_UNGORO",
					"KNIGHTS_OF_THE_FROZEN_THRONE",
					"KOBOLDS_AND_CATACOMBS",
					"WITCHWOOD",
					"BOOMSDAY_PROJECT",
					"RASTAKHANS_RUMBLE",
					"RISE_OF_SHADOWS",
					"BATTLE_FOR_ASHENVALE",
					"SANDS_OF_TIME",
					"VERDANT_DREAMS",
					"HALL_OF_FAME",
					"ALTERNATIVE",
					"UNNERFED",
					"BLIZZARD_ADVENTURE",
					"SPELLSOURCE_BASIC",
					"CUSTOM"
			));

	public static final DeckFormat SPELLSOURCE = new DeckFormat()
			.withName("Spellsource")
			.withCardSets(List.of(
					"SPELLSOURCE_BASIC",
					"VERDANT_DREAMS",
					"SANDS_OF_TIME",
					"BATTLE_FOR_ASHENVALE",
					"CUSTOM"
			));

	public static DeckFormat ALL = new DeckFormat()
			.withName("All");

	public static void populateAll(List<String> sets) {
		ALL.sets.clear();
		for (String set : sets) {
			ALL.addSet(set);
		}
	}

	private static final Map<String, DeckFormat> FORMATS = Collections.unmodifiableMap(Stream.of(
			STANDARD,
			WILD,
			CUSTOM,
			PAST,
			SPELLSOURCE,
			GREATER_CUSTOM,
			ALL)
			.collect(Collectors.toMap(DeckFormat::getName, Function.identity())));

	public static DeckFormat getFormat(String name) {
		name = name.toLowerCase();
		switch (name) {
			case "standard":
				return STANDARD;
			case "wild":
				return WILD;
			case "all":
				return ALL;
			case "spellsource":
				return SPELLSOURCE;
			case "past":
				return PAST;
			case "greater custom":
				return GREATER_CUSTOM;
			case "custom":
			default:
				return CUSTOM;
		}
	}

	public static Map<String, DeckFormat> formats() {
		return FORMATS;
	}

	public static DeckFormat getSmallestSupersetFormat(Set<String> requiredSets) {
		DeckFormat smallestFormat = DeckFormat.ALL;
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

	public void addSet(String cardSet) {
		sets.add(cardSet);
	}

	public boolean isInFormat(Card card) {
		if (sets.contains(card.getCardSet())) {
			return true;
		} else return false;
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
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof DeckFormat)) {
			return false;
		}

		DeckFormat rhs = (DeckFormat) obj;
		return this.getCardSets().equals(rhs.getCardSets());
	}

	@Override
	public DeckFormat clone() throws CloneNotSupportedException {
		return (DeckFormat) super.clone();
	}
}

