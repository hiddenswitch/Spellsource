package net.demilich.metastone.game.cards;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.shared.utils.ResourceInputStream;
import net.demilich.metastone.game.shared.utils.ResourceLoader;
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

	public static final String CARDS_FOLDER = "cards";

	private static Logger logger = LoggerFactory.getLogger(CardCatalogue.class);

	private final static Map<String, Card> cards = new HashMap<>();
	private final static Map<String, CardCatalogueRecord> records = new HashMap<>();
	private final static Map<String, CardCatalogueRecord> recordsByName = new HashMap<>();

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

	public static Card getCardById(String id) {
		Card card = cards.getOrDefault(id.toLowerCase(), null);
		if (card != null) {
			card = card.getCopy();
		}
		return card;
	}

	public static Map<String, CardCatalogueRecord> getRecords() {
		return Collections.unmodifiableMap(records);
	}

	public static Card getCardByName(String name) {
		CardCatalogueRecord namedCard = recordsByName.get(name);
		if (namedCard != null) {
			return getCardById(namedCard.getId());
		}
		return null;
	}

	public static CardList getHeroes() {
		return query(null, card -> card.getCardSet() == CardSet.BASIC && card.getCardType() == CardType.HERO);
	}

	public static CardList getHeroPowers(DeckFormat deckFormat) {
		return query(deckFormat, card -> card.isCollectible() && card.getCardType() == CardType.HERO_POWER);
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
			if (card.getCardType().isCardType(CardType.HERO_POWER)) {
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

	public static void loadCardsFromPackage()  /*IOException, URISyntaxException*/ /*, CardParseException*/ {
		synchronized (cards) {
			if (!cards.isEmpty()) {
				return;
			}

			Collection<ResourceInputStream> inputStreams = null;
			try {
				inputStreams = ResourceLoader.loadJsonInputStreams(CARDS_FOLDER, false);
				loadCards(inputStreams);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
//			try {

//			} catch (CardParseException e) {
//				logger.error(e.getMessage());
////				throw e;
//			}
		}
	}

	public static CardList query(DeckFormat deckFormat, Predicate<Card> filter) {
		CardList result = new CardArrayList();
		for (Card card : cards.values()) {
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
				if (cardDesc.containsKey(desc.id)) {
					logger.error("Card id {} is duplicated!", desc.id);
				}
				cardDesc.put(desc.id, desc);
				records.put(desc.id, record);
				recordsByName.put(desc.name, record);
			} catch (Exception e) {
				//logger.error("Error parsing card '{}'", resourceInputStream.fileName);
				logger.error(e.toString());
				badCards.add(resourceInputStream.fileName);
//				throw e;
			}
		}

		for (CardDesc desc : cardDesc.values()) {
			Card instance = desc.createInstance();
			CardCatalogue.add(instance);

		}

		logger.debug("{} cards loaded.", CardCatalogue.cards.size());

//		if (!badCards.isEmpty()) {
//			throw new CardParseException(badCards);
//		}
	}

	public static Stream<Card> stream() {
		return cards.values().stream();
	}
}
