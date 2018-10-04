package net.demilich.metastone.game.decks;

import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardSet;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.demilich.metastone.game.cards.CardSet.*;

public class DeckFormat implements Serializable, Cloneable {
	private String name = "";
	private Set<CardSet> sets;

	public static final DeckFormat STANDARD = new DeckFormat()
			.withName("Standard")
			.withCardSets(
					Collections.unmodifiableSet(EnumSet.of(
							BASIC,
							CLASSIC,
							JOURNEY_TO_UNGORO,
							KNIGHTS_OF_THE_FROZEN_THRONE,
							KOBOLDS_AND_CATACOMBS,
							WITCHWOOD,
							BOOMSDAY_PROJECT
					)));

	public static final DeckFormat WILD = new DeckFormat()
			.withName("Wild")
			.withCardSets(
					Collections.unmodifiableSet(EnumSet.of(
							BASIC,
							CLASSIC,
							REWARD,
							PROMO,
							NAXXRAMAS,
							GOBLINS_VS_GNOMES,
							BLACKROCK_MOUNTAIN,
							THE_GRAND_TOURNAMENT,
							LEAGUE_OF_EXPLORERS,
							THE_OLD_GODS,
							ONE_NIGHT_IN_KARAZHAN,
							MEAN_STREETS_OF_GADGETZAN,
							JOURNEY_TO_UNGORO,
							KNIGHTS_OF_THE_FROZEN_THRONE,
							KOBOLDS_AND_CATACOMBS,
							WITCHWOOD,
							BOOMSDAY_PROJECT,
							HALL_OF_FAME
					))
			);

	public static final DeckFormat PAST = new DeckFormat()
			.withName("Past")
			.withCardSets(
					Collections.unmodifiableSet(EnumSet.of(
							REWARD,
							PROMO,
							NAXXRAMAS,
							GOBLINS_VS_GNOMES,
							BLACKROCK_MOUNTAIN,
							THE_GRAND_TOURNAMENT,
							LEAGUE_OF_EXPLORERS,
							THE_OLD_GODS,
							ONE_NIGHT_IN_KARAZHAN,
							MEAN_STREETS_OF_GADGETZAN,
							HALL_OF_FAME
					))
			);

	public static final DeckFormat CUSTOM = new DeckFormat()
			.withName("Custom")
			.withCardSets(
					Collections.unmodifiableSet(EnumSet.of(
							BASIC,
							CLASSIC,
							REWARD,
							PROMO,
							NAXXRAMAS,
							GOBLINS_VS_GNOMES,
							BLACKROCK_MOUNTAIN,
							THE_GRAND_TOURNAMENT,
							LEAGUE_OF_EXPLORERS,
							THE_OLD_GODS,
							ONE_NIGHT_IN_KARAZHAN,
							MEAN_STREETS_OF_GADGETZAN,
							JOURNEY_TO_UNGORO,
							KNIGHTS_OF_THE_FROZEN_THRONE,
							KOBOLDS_AND_CATACOMBS,
							WITCHWOOD,
							BOOMSDAY_PROJECT,
							BATTLE_FOR_ASHENVALE,
							ALTERNATE,
							SANDS_OF_TIME,
							HALL_OF_FAME,
							CardSet.CUSTOM
					))
			);

	public static final DeckFormat SPELLSOURCE = new DeckFormat()
			.withName("Spellsource")
			.withCardSets(
					Collections.unmodifiableSet(EnumSet.of(
							BASIC,
							CLASSIC,
							REWARD,
							PROMO,
							NAXXRAMAS,
							GOBLINS_VS_GNOMES,
							BLACKROCK_MOUNTAIN,
							THE_GRAND_TOURNAMENT,
							LEAGUE_OF_EXPLORERS,
							THE_OLD_GODS,
							ONE_NIGHT_IN_KARAZHAN,
							MEAN_STREETS_OF_GADGETZAN,
							JOURNEY_TO_UNGORO,
							KNIGHTS_OF_THE_FROZEN_THRONE,
							KOBOLDS_AND_CATACOMBS,
							WITCHWOOD,
							BOOMSDAY_PROJECT,
							HALL_OF_FAME,
							CardSet.CUSTOM
					))
			);

	public static final DeckFormat ALL = new DeckFormat()
			.withName("All")
			.withCardSets(
					Collections.unmodifiableSet(new HashSet<>(Arrays.asList(CardSet.values())))
			);

	private static final Map<String, DeckFormat> FORMATS = Collections.unmodifiableMap(Stream.of(
			STANDARD,
			WILD,
			CUSTOM,
			PAST,
			SPELLSOURCE,
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
			case "custom":
			default:
				return CUSTOM;
		}
	}

	public static Map<String, DeckFormat> formats() {
		return FORMATS;
	}

	public static DeckFormat getSmallestSupersetFormat(Set<CardSet> requiredSets) {
		DeckFormat smallestFormat = DeckFormat.ALL;
		int minExcess = smallestFormat.sets.size();

		for (Map.Entry<String, DeckFormat> format : DeckFormat.formats().entrySet()) {
			Set<CardSet> formatSets = format.getValue().getCardSets();
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

	public DeckFormat() {
		sets = new HashSet<>();
	}

	public void addSet(CardSet cardSet) {
		sets.add(cardSet);
	}

	public boolean isInFormat(Card card) {
		if (sets.contains(card.getCardSet())) {
			return true;
		}
		return false;
	}

	public boolean isInFormat(CardSet set) {
		return set != null && sets.contains(set);
	}

	public Set<CardSet> getCardSets() {
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

	public DeckFormat withCardSets(CardSet... cardSets) {
		for (CardSet cardSet : cardSets) {
			addSet(cardSet);
		}
		return this;
	}

	public DeckFormat withCardSets(Iterable<CardSet> cardSets) {
		for (CardSet cardSet : cardSets) {
			addSet(cardSet);
		}
		return this;
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
