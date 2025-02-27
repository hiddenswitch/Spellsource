package net.demilich.metastone.game.cards;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A place that stores {@link CardCatalogueRecordImpl} records that were generated from the "cards" Java package.
 */
public interface CardCatalogue {
	String FORMAT_NAME_ALL = "All";
	DeckFormat all = new DeckFormat()
			.withName(FORMAT_NAME_ALL);

	static String latestImplementedHearthstoneExpansion() {
		return "RISE_OF_SHADOWS";
	}


	default DeckFormat getSmallestSupersetFormat(GameDeck... decks) {
		return getSmallestSupersetFormat(Arrays.asList(decks));
	}

	default DeckFormat getSmallestSupersetFormat(List<GameDeck> deckPair) {
		if (deckPair.get(0).getFormat().equals(deckPair.get(1).getFormat())) {
			return deckPair.get(0).getFormat();
		}
		Set<String> requiredSets = deckPair.stream().flatMap(deck -> deck.getCards().stream())
				.map(Card::getCardSet).collect(Collectors.toSet());
		return getSmallestSupersetFormat(requiredSets);
	}

	default DeckFormat getSmallestSupersetFormat(Set<String> requiredSets) {
		DeckFormat smallestFormat = getFormat(FORMAT_NAME_ALL);
		int minExcess = smallestFormat.getSets().size();

		for (Map.Entry<String, DeckFormat> format : formats().entrySet()) {
			Set<String> formatSets = format.getValue().getSets();
			if (!formatSets.containsAll(requiredSets)) {
				continue;
			}

			int excess = formatSets.size() - requiredSets.size();
			if (excess < minExcess) {
				smallestFormat = format.getValue();
				minExcess = excess;
			}
		}

		smallestFormat = smallestFormat.clone();
		smallestFormat.getSets().remove(CardSet.TEST);
		return smallestFormat;
	}

	default DeckFormat all() {
		return getFormat(FORMAT_NAME_ALL);
	}

	/**
	 * The current {@code Spellsource} format containing all Spellsource sets.
	 *
	 * @return A format, or {@code null} if either Spellsource cards are not on your classpath or
	 * {@link ClasspathCardCatalogue#loadCardsFromPackage()} has not been called. OSGi-friendly.
	 */
	default DeckFormat spellsource() {
		var format = getFormat("Spellsource");
		if (format == null) {
			if (this instanceof ClasspathCardCatalogue classpathCardCatalogue) {
				classpathCardCatalogue.loadCardsFromPackage();
				format = getFormat("Spellsource");
				if (format == null) {
					throw new IllegalStateException("???");
				}
			}
			throw new NullPointerException("must load cards first with CardCatalogue.loadCardsFromPackage() (%d)".formatted(this.getCards().size()));
		}
		return format;
	}

	Map<String, DeckFormat> formats();

	@Nullable
	DeckFormat getFormat(String name);

	Set<String> getBannedDraftCards();

	Set<String> getHardRemovalCardIds();

	default String getOneOneNeutralMinionCardId() {
		return "minion_neutral_test_1";
	}

	default String getNeutralHero() {
		return "hero_neutral";
	}

	@NotNull CardList getAll();

	/**
	 * Returns a mutable reference to the cards loaded into this catalogue.
	 * <p>
	 * Changing the cards here changes all their references in all games. However, {@link #getCardById(String)} returns
	 * clones.
	 *
	 * @return
	 */
	@NotNull
	Map<String, Card> getCards();

	/**
	 * Gets a card from the card database by a specific ID. These IDs correspond to the names of the JSON files in the
	 * {@code cards/src/main/resources/cards} directory. Staging cards are never retrieved. The catalogue does not respect
	 * {@link DeckFormat} filters here, and will return any card with a valid ID.
	 * <p>
	 * Some effects, like {@link net.demilich.metastone.game.spells.CastFromGroupSpell}, create temporary cards that exist
	 * only in the game context. Generally, you should call
	 * {@link net.demilich.metastone.game.GameContext#getCardById(String)} in order to correctly retrieve those cards.
	 *
	 * @param id
	 * @return
	 * @throws NullPointerException if the card cannot be found or if the card's version exceeds the currently configured
	 *                              version. (Versions are only used for {@link net.demilich.metastone.game.logic.Trace}
	 *                              objects.)
	 */
	@NotNull Card getCardById(@NotNull String id);

	/**
	 * Gets all the {@link CardCatalogueRecordImpl} objects specified in the {@code cards} module.
	 *
	 * @return
	 */
	default
	@NotNull Map<String, CardCatalogueRecord> getRecords() {
		return Maps.transformValues(getCards(), card -> new CardCatalogueRecord() {
			@Override
			public String getId() {
				return card.getCardId();
			}

			@Override
			public CardDesc getDesc() {
				return card.getDesc();
			}
		});
	}

	@Nullable Card getCardByName(String name);

	@NotNull
	Card getCardByName(String name, String heroClass);

	default CardList query(DeckFormat deckFormat) {
		return query(deckFormat, null, null, null, null, true);
	}

	default CardList query(DeckFormat deckFormat, CardType cardType) {
		return query(deckFormat, cardType, null, null, null, true);
	}

	default CardList query(DeckFormat deckFormat, String heroClass) {
		return query(deckFormat, null, null, heroClass, null, true);
	}

	default CardList query(DeckFormat deckFormat, CardType cardType, Rarity rarity, String heroClass) {
		return query(deckFormat, cardType, rarity, heroClass, null, true);
	}

	/**
	 * Queries the card catalogue for cards that match the specified filters.
	 *
	 * @param deckFormat
	 * @param cardType
	 * @param rarity
	 * @param heroClass
	 * @param tag
	 * @param clone
	 * @return
	 */
	@NotNull CardList query(DeckFormat deckFormat, CardType cardType, Rarity rarity, String heroClass, Attribute tag, boolean clone);

	Card getFormatCard(String name);

	Card getHeroCard(String heroClass);

	/**
	 * Retrieves all the "class_" {@link CardType#CLASS} cards that specify a hero card, color, heroClass string, etc. for
	 * the specified class in the specified format.
	 *
	 * @param format
	 * @return
	 */
	CardList getClassCards(DeckFormat format);

	/**
	 * Retrieves the {@link net.demilich.metastone.game.entities.heroes.HeroClass} strings that correspond to the classes
	 * in the specified format.
	 *
	 * @param deckFormat
	 * @return
	 */
	Collection<String> getBaseClasses(DeckFormat deckFormat);

	Stream<Card> stream();

	CardList queryClassCards(DeckFormat format, String hero, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes);

	CardList queryNeutrals(DeckFormat format, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes);

	CardList queryUncollectible(DeckFormat deckFormat);

	default List<Card> queryClassCards(DeckFormat deckFormat, String heroClass) {
		var minions = query(deckFormat, CardType.MINION, null, heroClass);
		var spells = query(deckFormat, CardType.SPELL, null, heroClass);
		var weapons = query(deckFormat, CardType.WEAPON, null, heroClass);
		return Lists.newArrayList(Iterables.concat(minions, spells, weapons));
	}

	default List<Card> queryNeutrals(DeckFormat deckFormat) {
		var minions = query(deckFormat, CardType.MINION, null, HeroClass.ANY);
		var spells = query(deckFormat, CardType.SPELL, null, HeroClass.ANY);
		var weapons = query(deckFormat, CardType.WEAPON, null, HeroClass.ANY);
		return Lists.newArrayList(Iterables.concat(minions, spells, weapons));
	}

	default DeckFormat defaultFormat() {
		return spellsource();
	}
}
