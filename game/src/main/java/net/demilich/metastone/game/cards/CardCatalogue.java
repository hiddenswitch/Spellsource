package net.demilich.metastone.game.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hiddenswitch.spellsource.CardResource;
import com.hiddenswitch.spellsource.CardResources;
import com.hiddenswitch.spellsource.CardsModule;
import com.hiddenswitch.spellsource.ResourceInputStream;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A place that stores {@link CardCatalogueRecord} records that were generated from the "cards" Java package.
 */
public class CardCatalogue {
	/**
	 * A class that describes injected card resources used internally by a static global card catalogue..
	 */
	public static class InjectedCardResources {
		private Set<CardResources> cardResources;

		@Inject
		public InjectedCardResources(Set<CardResources> cardResources) {
			this.cardResources = cardResources;
		}
	}

	private static final Injector INJECTOR;

	static {
		// This code iterates through all the classes on the classpath that are subtypes of CardsModule as "plugins", then
		// makes the cards they define available to the card catalogue.
		INJECTOR = Guice.createInjector(new Reflections("com.hiddenswitch.spellsource").getSubTypesOf(CardsModule.class).stream().map(clazz -> {
			try {
				return clazz.getConstructor().newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toList()));
	}

	private static Logger LOGGER = LoggerFactory.getLogger(CardCatalogue.class);
	private static int version = 1;
	private static AtomicBoolean loaded = new AtomicBoolean();
	private final static Map<String, Card> cards = new LinkedHashMap<>();
	private final static Map<String, CardCatalogueRecord> records = new LinkedHashMap<>();
	private final static Map<String, List<CardCatalogueRecord>> recordsByName = new LinkedHashMap<>();

	@NotNull
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
	@NotNull
	public static Map<String, CardCatalogueRecord> getRecords() {
		return Collections.unmodifiableMap(records);
	}

	@Nullable
	public static Card getCardByName(String name) {
		CardCatalogueRecord namedCard = recordsByName.get(name).stream().filter(ccr -> ccr.getDesc().isCollectible()).findFirst().orElse(recordsByName.get(name).get(0));
		if (namedCard != null) {
			return getCardById(namedCard.getId());
		}
		return null;
	}

	public static Card getCardByName(String name, String heroClass) {
		List<CardCatalogueRecord> namedCards = recordsByName.get(name).stream().filter(ccr -> ccr.getDesc().isCollectible()).collect(Collectors.toList());
		if (!namedCards.isEmpty()) {
			if (namedCards.size() > 1) {
				for (CardCatalogueRecord namedCard : namedCards) {
					Card card = getCardById(namedCard.getId());
					if (card.hasHeroClass(heroClass)) {
						return card;
					}
				}
			}
			return getCardById(namedCards.get(0).getId());
		}
		return getCardById(recordsByName.get(name).get(0).getDesc().getId());
	}

	public static CardList getHeroes() {
		return query(null, card -> card.getCardSet() == CardSet.BASIC && card.getCardType() == CardType.HERO);
	}

	public static CardList query(DeckFormat deckFormat) {
		return query(deckFormat, (CardType) null, (Rarity) null, (String) null, (Attribute) null);
	}

	public static CardList query(DeckFormat deckFormat, CardType cardType) {
		return query(deckFormat, cardType, (Rarity) null, (String) null, (Attribute) null);
	}

	public static CardList query(DeckFormat deckFormat, String heroClass) {
		return query(deckFormat, (CardType) null, (Rarity) null, heroClass, (Attribute) null);
	}

	public static CardList query(DeckFormat deckFormat, CardType cardType, Rarity rarity, String heroClass) {
		return query(deckFormat, cardType, rarity, heroClass, (Attribute) null);
	}

	public static CardList query(DeckFormat deckFormat, String heroClass, String actualHeroClass) {
		return query(deckFormat, (CardType) null, (Rarity) null, heroClass, (Attribute) null, actualHeroClass);
	}

	public static CardList query(DeckFormat deckFormat, CardType cardType, Rarity rarity, String heroClass, Attribute tag) {
		return query(deckFormat, cardType, rarity, heroClass, tag, null);
	}

	public static CardList query(DeckFormat deckFormat, CardType cardType, Rarity rarity, String heroClass, Attribute tag, String actualHeroClass) {
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
	 * Loads all the cards specified in the {@code "cards/src/main/resources" + DEFAULT_CARDS_FOLDER } directory in the
	 * {@code cards} module. This can be called multiple times, but will not "refresh" the catalogue file.
	 */
	public static void loadCardsFromPackage()  /*IOException, URISyntaxException*/ /*, CardParseException*/ {
		if (!loaded.compareAndSet(false, true)) {
			return;
		}

		try {
			Json.mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
			InjectedCardResources cardResources = INJECTOR.getInstance(InjectedCardResources.class);
			Collection<ResourceInputStream> inputStreams = cardResources.cardResources.stream().flatMap(resource -> resource.getResources().stream()).map(resource -> ((CardResource) resource)).map(resource -> (ResourceInputStream) resource).collect(Collectors.toList());
			Map<String, CardDesc> cardDesc = new HashMap<>();
			ArrayList<String> badCards = new ArrayList<>();
			CardParser cardParser = new CardParser();

			for (ResourceInputStream resourceInputStream : inputStreams) {
				try {
					final CardCatalogueRecord record = cardParser.parseCard(resourceInputStream);
					CardDesc desc = record.getDesc();
					if (cardDesc.containsKey(desc.getId())) {
						LOGGER.error("loadCards: Card id {} is duplicated!", desc.getId());
					}
					cardDesc.put(desc.getId(), desc);
					records.put(desc.getId(), record);
					recordsByName.putIfAbsent(desc.getName(), new ArrayList<>());
					recordsByName.get(desc.getName()).add(record);
				} catch (Exception e) {
					LOGGER.error("loadCards: An error occurred while processing {}: {}", resourceInputStream.getFileName(), e.toString());
					badCards.add(resourceInputStream.getFileName());
				}
			}

			for (CardDesc desc : cardDesc.values()) {
				Card instance = desc.create();
				cards.put(instance.getCardId(), instance);
			}

			LOGGER.debug("loadCards: {} cards loaded.", CardCatalogue.cards.size());
		} catch (Exception e) {
			throw new RuntimeException(e);
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
