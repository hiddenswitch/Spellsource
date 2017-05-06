package net.demilich.metastone.game.cards;

import net.demilich.metastone.BuildConfig;
import net.demilich.metastone.game.Attribute;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.utils.MetastoneProperties;
import net.demilich.metastone.utils.ResourceInputStream;
import net.demilich.metastone.utils.ResourceLoader;
import net.demilich.metastone.utils.UserHomeMetastone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Predicate;

public class CardCatalogue {

	public static final String CARDS_FOLDER = "cards";
	public static final String LOCAL_CARDS_FOLDER = "../cards/src/main/resources/cards/";
	public static final String CARDS_FOLDER_PATH = UserHomeMetastone.getPath() + File.separator + CARDS_FOLDER;
	public static final String CARDS_COPIED_PROPERTY = "cardRevision";

	private static Logger logger = LoggerFactory.getLogger(CardCatalogue.class);

	private final static Map<String, Card> cards = new HashMap<>();
	private final static Map<String, CardCatalogueRecord> records = new HashMap<>();

	public static void add(Card card) {
		cards.put(card.getCardId(), card);
	}

	public static CardCollection getAll() {
		CardCollection result = new CardCollectionImpl();
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
		for (Card card : cards.values()) {
			if (card.isCollectible() && card.getName().equals(name)) {
				return card.clone();
			}
		}

		return null;
	}

	public static CardCollection getHeroes() {
		return query(null, card -> card.isCollectible() && card.getCardType() == CardType.HERO);
	}

	public static CardCollection getHeroPowers(DeckFormat deckFormat) {
		return query(deckFormat, card -> card.isCollectible() && card.getCardType() == CardType.HERO_POWER);
	}

	public static CardCollection query(DeckFormat deckFormat) {
		return query(deckFormat, (CardType) null, (Rarity) null, (HeroClass) null, (Attribute) null);
	}

	public static CardCollection query(DeckFormat deckFormat, CardType cardType) {
		return query(deckFormat, cardType, (Rarity) null, (HeroClass) null, (Attribute) null);
	}

	public static CardCollection query(DeckFormat deckFormat, HeroClass heroClass) {
		return query(deckFormat, (CardType) null, (Rarity) null, heroClass, (Attribute) null);
	}

	public static CardCollection query(DeckFormat deckFormat, CardType cardType, Rarity rarity, HeroClass heroClass) {
		return query(deckFormat, cardType, rarity, heroClass, (Attribute) null);
	}

	public static CardCollection query(DeckFormat deckFormat, HeroClass heroClass, HeroClass actualHeroClass) {
		return query(deckFormat, (CardType) null, (Rarity) null, heroClass, (Attribute) null, actualHeroClass);
	}

	public static CardCollection query(DeckFormat deckFormat, CardType cardType, Rarity rarity, HeroClass heroClass, Attribute tag) {
		return query(deckFormat, cardType, rarity, heroClass, tag, null);
	}

	public static CardCollection query(DeckFormat deckFormat, CardType cardType, Rarity rarity, HeroClass heroClass, Attribute tag, HeroClass actualHeroClass) {
		CardCollection result = new CardCollectionImpl();
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
			// per default, do not include heroes or hero powers
			if (card.getCardType().isCardType(CardType.HERO_POWER) || card.getCardType().isCardType(CardType.HERO)) {
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

	public static void loadCardsFromPackage() throws IOException, URISyntaxException, CardParseException {
		synchronized (cards) {
			if (!cards.isEmpty()) {
				return;
			}

			Collection<ResourceInputStream> inputStreams = ResourceLoader.loadJsonInputStreams(CARDS_FOLDER, false);
			try {
				loadCards(inputStreams);
			} catch (CardParseException e) {
				logger.error(e.getMessage());
				throw e;
			}
		}
	}

	public static CardCollection query(DeckFormat deckFormat, Predicate<Card> filter) {
		CardCollection result = new CardCollectionImpl();
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

	public static void loadCardsFromFilesystem() throws IOException, URISyntaxException, CardParseException {
		// load cards from ~/metastone/cards on the file system
		Collection<ResourceInputStream> inputStreams = ResourceLoader.loadJsonInputStreams(CARDS_FOLDER_PATH, true);
		loadCards(inputStreams);
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
			} catch (Exception e) {
				//logger.error("Error parsing card '{}'", resourceInputStream.fileName);
				logger.error(e.toString());
				badCards.add(resourceInputStream.fileName);
				throw e;
			}
		}

		for (CardDesc desc : cardDesc.values()) {
			Card instance = desc.createInstance();
			CardCatalogue.add(instance);

		}

		logger.debug("{} cards loaded.", CardCatalogue.cards.size());

		if (!badCards.isEmpty()) {
			throw new CardParseException(badCards);
		}
	}

	public static void copyCardsFromResources() throws IOException, URISyntaxException {
		// if we have not copied cards to the USER_HOME_METASTONE cards folder,
		// then do so now
		int cardRevision = MetastoneProperties.getInt(CARDS_COPIED_PROPERTY, 0);
		logger.info("Existing card revision = " + cardRevision);
		if (BuildConfig.CARD_REVISION > cardRevision) {
			logger.info("Card update required: MetaStone card revision is: {}, last card update was with revision {}", BuildConfig.CARD_REVISION, cardRevision);
			ResourceLoader.copyFromResources(CARDS_FOLDER, CARDS_FOLDER_PATH);

			// set a property to indicate that we have copied the cards with current version
			MetastoneProperties.setProperty(CARDS_COPIED_PROPERTY, String.valueOf(BuildConfig.CARD_REVISION));
		} else {
			logger.info("Cards in user home folder are up-to-date: rev {}", cardRevision);
		}
	}
}
