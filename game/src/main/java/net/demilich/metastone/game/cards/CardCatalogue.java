package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.utils.ResourceInputStream;
import net.demilich.metastone.game.utils.ResourceLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A place that stores {@link CardCatalogueRecord} records that were generated from the "cards" Java package.
 */
public class CardCatalogue {
	/**
	 * The directory/prefix of the location in the JAR resources of the {@code card} module, i.e., a directory inside of
	 * {@code cards/src/main/resources}.
	 */
	public static final String CARDS_FOLDER = "cards";
	private static Logger logger = LoggerFactory.getLogger(CardCatalogue.class);
	private static int version = 1;

	private final static Map<String, Card> cards = new LinkedHashMap<>();
	private final static Map<String, CardCatalogueRecord> records = new LinkedHashMap<>();
	private final static Map<String, List<CardCatalogueRecord>> recordsByName = new LinkedHashMap<>();

	public static void add(Card card) {
		cards.put(card.getCardId(), card);
	}

	public static CardList getAll() {
		CardList result = new CardArrayList();
		for (Card card : cards.values()) {
			result.addCard(card.clone());
		}
		return result;
	}

	/**
	 * Gets a card from the card database by a specific ID. These IDs correspond to the names of the JSON files in the
	 * {@code cards/src/main/resources/cards} directory. Staging cards are never retrieved. The catalogue does not respect
	 * {@link DeckFormat} filters here, and will return any card with a valid ID.
	 * <p>
	 * Some effects, like {@link net.demilich.metastone.game.spells.CastFromGroupSpell}, create temporary cards that exist
	 * only in the game context. Generally, you should call {@link net.demilich.metastone.game.GameContext#getCardById(String)}
	 * in order to correctly retrieve those cards.
	 *
	 * @param id
	 * @return
	 * @throws NullPointerException if the card cannot be found or if the card's version exceeds the currently configured
	 *                              version. (Versions are only used for {@link net.demilich.metastone.game.logic.Trace}
	 *                              objects.)
	 */
	public @NotNull
	static Card getCardById(@NotNull String id) {
		Card card = cards.getOrDefault(id.toLowerCase(), null);
		if (card != null) {
			card = card.getCopy();
		} else {
			throw new NullPointerException(String.format("getCardById: %s could not be found", id));
		}
		if (card.getDesc().getFileFormatVersion() > version) {
			throw new NullPointerException(String.format("getCardById: %s is not in this version", id));
		}
		return card;
	}

	/**
	 * Gets all the {@link CardCatalogueRecord} objects specified in the {@code cards} module.
	 *
	 * @return
	 */
	public static Map<String, CardCatalogueRecord> getRecords() {
		return Collections.unmodifiableMap(records);
	}

	public static Card getCardByName(String name) {
		CardCatalogueRecord namedCard = recordsByName.get(name).stream().filter(ccr -> ccr.getDesc().isCollectible()).findFirst().orElse(recordsByName.get(name).get(0));
		if (namedCard != null) {
			return getCardById(namedCard.getId());
		}
		return null;
	}

	public static CardList getHeroes() {
		return query(null, card -> card.getCardSet() == CardSet.BASIC && card.getCardType() == CardType.HERO);
	}

	public static CardList query(DeckFormat deckFormat) {
		return query(deckFormat, (CardType) null, (Rarity) null, (HeroClass) null, (Attribute) null);
	}

	public static CardList query(DeckFormat deckFormat, CardType cardType) {
		return query(deckFormat, cardType, (Rarity) null, (HeroClass) null, (Attribute) null);
	}

	public static CardList query(DeckFormat deckFormat, HeroClass heroClass) {
		return query(deckFormat, (CardType) null, (Rarity) null, heroClass, (Attribute) null);
	}

	public static CardList query(DeckFormat deckFormat, CardType cardType, Rarity rarity, HeroClass heroClass) {
		return query(deckFormat, cardType, rarity, heroClass, (Attribute) null);
	}

	public static CardList query(DeckFormat deckFormat, HeroClass heroClass, HeroClass actualHeroClass) {
		return query(deckFormat, (CardType) null, (Rarity) null, heroClass, (Attribute) null, actualHeroClass);
	}

	public static CardList query(DeckFormat deckFormat, CardType cardType, Rarity rarity, HeroClass heroClass, Attribute tag) {
		return query(deckFormat, cardType, rarity, heroClass, tag, null);
	}

	public static CardList query(DeckFormat deckFormat, CardType cardType, Rarity rarity, HeroClass heroClass, Attribute tag, HeroClass actualHeroClass) {
		CardList result = new CardArrayList();
		for (Card card : cards.values()) {
			if (card.getDesc().getFileFormatVersion() > version) {
				continue;
			}

			if (!deckFormat.isInFormat(card)) {
				continue;
			}
			if (!card.isCollectible()) {
				continue;
			}
			if (cardType != null && !card.getCardType().isCardType(cardType)) {
				continue;
			}
			// per default, do not include hero powers
			if (card.getCardType().isCardType(CardType.HERO_POWER) || card.isQuest()) {
				continue;
			}
			if (rarity != null && !card.getRarity().isRarity(rarity)) {
				continue;
			}
			if (heroClass != null && !card.hasHeroClass(heroClass)) {
				continue;
			}
			if (tag != null && !card.hasAttribute(tag)) {
				continue;
			}
			result.addCard(card.clone());
		}

		return result;
	}

	/**
	 * Loads all the cards specified in the {@code "cards/src/main/resources" + CARDS_FOLDER } directory in the {@code
	 * cards} module. This can be called multiple times, but will not "refresh" the catalogue file.
	 */
	public static void loadCardsFromPackage()  /*IOException, URISyntaxException*/ /*, CardParseException*/ {
		synchronized (cards) {
			if (!cards.isEmpty()) {
				return;
			}

			Collection<ResourceInputStream> inputStreams = null;
			try {
				inputStreams = ResourceLoader.loadInputStreams(CARDS_FOLDER, false);
				loadCards(inputStreams);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static CardList query(DeckFormat deckFormat, Predicate<Card> filter) {
		CardList result = new CardArrayList();
		for (Card card : cards.values()) {
			if (card.getDesc().getFileFormatVersion() > version) {
				continue;
			}

			if (deckFormat != null && !deckFormat.isInFormat(card)) {
				continue;
			}

			if (filter.test(card)) {
				result.addCard(card.clone());
			}
		}
		return result;
	}


	private static void loadCards(Collection<ResourceInputStream> inputStreams) throws IOException, URISyntaxException, CardParseException {
		Map<String, CardDesc> cardDesc = new HashMap<String, CardDesc>();
		ArrayList<String> badCards = new ArrayList<>();
		CardParser cardParser = new CardParser();

		for (ResourceInputStream resourceInputStream : inputStreams) {
			try {
				final CardCatalogueRecord record = cardParser.parseCard(resourceInputStream);
				CardDesc desc = record.getDesc();
				if (cardDesc.containsKey(desc.getId())) {
					logger.error("loadCards: Card id {} is duplicated!", desc.getId());
				}
				cardDesc.put(desc.getId(), desc);
				records.put(desc.getId(), record);
				recordsByName.putIfAbsent(desc.getName(), new ArrayList<>());
				recordsByName.get(desc.getName()).add(record);
			} catch (Exception e) {
				logger.error("loadCards: An error occurred while processing {}: {}", resourceInputStream.fileName, e.toString());
				badCards.add(resourceInputStream.fileName);
			}
		}

		for (CardDesc desc : cardDesc.values()) {
			Card instance = desc.create();
			CardCatalogue.add(instance);
		}

		logger.debug("loadCards: {} cards loaded.", CardCatalogue.cards.size());
	}

	public static Stream<Card> stream() {
		return cards.values().stream().filter(card -> card.getDesc().getFileFormatVersion() <= version);
	}

	public static int getVersion() {
		return version;
	}

	public static void setVersion(int version) {
		CardCatalogue.version = version;
	}
}
