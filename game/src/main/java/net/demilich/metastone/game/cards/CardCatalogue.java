package net.demilich.metastone.game.cards;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.hiddenswitch.spellsource.core.CardResource;
import com.hiddenswitch.spellsource.core.CardResources;
import com.hiddenswitch.spellsource.core.ResourceInputStream;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A place that stores {@link CardCatalogueRecord} records that were generated from the "cards" Java package.
 */
public class CardCatalogue {
	private static Map<String, Card> classCards;
	private static Map<String, Card> heroCards;
	private static Map<String, Card> formatCards;
	private static LoadingCache<DeckFormat, CardList> classCardsForFormat;
	private static LoadingCache<DeckFormat, List<String>> baseClassesForFormat;

	public static Set<String> getBannedDraftCards() {
		return Collections.unmodifiableSet(bannedCardIds);
	}

	public static Set<String> getHardRemovalCardIds() {
		return Collections.unmodifiableSet(hardRemovalCardIds);
	}

	public static String getOneOneNeutralMinionCardId() {
		return "minion_neutral_test_1";
	}

	public static String getNeutralHero() {
		return "hero_neutral";
	}

	private static Logger LOGGER = LoggerFactory.getLogger(CardCatalogue.class);
	private static int version = 2;
	private static final Set<String> bannedCardIds = new HashSet<>();
	private static AtomicBoolean loaded = new AtomicBoolean();
	private static final Set<String> hardRemovalCardIds = new HashSet<>();
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
			throw new NullPointerException(id);
		}
		if (card.getDesc().getFileFormatVersion() > version) {
			throw new NullPointerException(id);
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

	/**
	 * Queries the card catalogue for cards that match the specified filters.
	 *
	 * @param deckFormat
	 * @param cardType
	 * @param rarity
	 * @param heroClass
	 * @param tag
	 * @param actualHeroClass
	 * @return
	 */
	@NotNull
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
			// per default, do not include hero powers, quests, classes, and formats
			if (card.getCardType().isCardType(CardType.HERO_POWER) || card.isQuest() ||
					(card.getCardType().isCardType(CardType.CLASS) && cardType != CardType.CLASS) ||
					(card.getCardType().isCardType(CardType.FORMAT) && cardType != CardType.FORMAT)) {
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
		List<CardResources> cardResources = null;
		try (ScanResult scanResult =
				     new ClassGraph()
						     .enableClassInfo()
						     .disableRuntimeInvisibleAnnotations()
						     .whitelistPackages("com.hiddenswitch.spellsource.cards")
						     .scan()) {
			cardResources = scanResult
					.getAllClasses()
					.stream()
					.filter(info -> info.getName().contains("CardResources"))
					.map(ClassInfo::loadClass)
					.filter(CardResources.class::isAssignableFrom)
					.filter(c -> !Modifier.isAbstract(c.getModifiers()))
					.map(thisClass -> {
						try {
							return (CardResources) thisClass.getConstructor().newInstance();
						} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
							throw new RuntimeException(e);
						}
					})
					.collect(Collectors.toList());
			for (CardResources cardResource : cardResources) {
				bannedCardIds.addAll(cardResource.getDraftBannedCardIds());
				hardRemovalCardIds.addAll(cardResource.getHardRemovalCardIds());
			}
			Collection<ResourceInputStream> inputStreams = cardResources
					.stream()
					.flatMap(resource -> resource.getResources().stream())
					.map(resource -> ((CardResource) resource))
					.map(resource -> (ResourceInputStream) resource)
					.collect(Collectors.toList());
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

			List<String> sets = new ArrayList<>();
			cards.forEach((s, card) -> {
				if (!sets.contains(card.getCardSet())) {
					sets.add(card.getCardSet());
				}
			});
			DeckFormat.populateAll(sets);
			CardList formats = cards.values().stream()
					.filter(card -> card.getCardType() == CardType.FORMAT).collect(Collectors.toCollection(CardArrayList::new));
			DeckFormat.populateFormats(formats);
			formatCards = formats.stream().collect(Collectors.toMap(Card::getName, Function.identity()));
			// Populate the class and hero cards
			classCards = cards.values().stream().filter(c -> c.getCardType() == CardType.CLASS).collect(Collectors.toMap(Card::getHeroClass, Function.identity()));
			classCardsForFormat = CacheBuilder.newBuilder()
					.maximumSize(32)
					.build(new CacheLoader<>() {
						@Override
						public CardList load(@NotNull DeckFormat key) {
							return classCards.values().stream().filter(key::isInFormat).collect(Collectors.toCollection(CardArrayList::new));
						}
					});
			heroCards = Maps.transformEntries(classCards, (key, value) -> getCardById(Objects.requireNonNull(value).getHero()));
			baseClassesForFormat = CacheBuilder.newBuilder()
					.maximumSize(32)
					.build(new CacheLoader<>() {
						@Override
						public List<String> load(@NotNull DeckFormat key) {
							return classCards.values().stream().filter(c -> c.isCollectible() && key.isInFormat(c)).map(Card::getHeroClass).collect(Collectors.toList());
						}
					});

			LOGGER.debug("loadCards: {} cards loaded.", CardCatalogue.cards.size());
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (cardResources != null) {
				for (CardResources cardResources1 : cardResources) {
					try {
						cardResources1.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}

		}
	}

	public static Card getFormatCard(String name) {
		return formatCards.getOrDefault(name, null);
	}

	public static Card getHeroCard(String heroClass) {
		return heroCards.getOrDefault(heroClass, getCardById(CardCatalogue.getNeutralHero()));
	}

	/**
	 * Retrieves all the "class_" {@link CardType#CLASS} cards that specify a hero card, color, heroClass string, etc. for
	 * the specified class in the specified format.
	 *
	 * @param format
	 * @return
	 */
	public static CardList getClassCards(DeckFormat format) {
		try {
			return classCardsForFormat.get(format);
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		}
	}

	/**
	 * Retrieves the {@link net.demilich.metastone.game.entities.heroes.HeroClass} strings that correspond to the classes
	 * in the specified format.
	 *
	 * @param deckFormat
	 * @return
	 */
	public static List<String> getBaseClasses(DeckFormat deckFormat) {
		try {
			return baseClassesForFormat.get(deckFormat);
		} catch (ExecutionException e) {
			throw new RuntimeException(e.getCause());
		}
	}

	public static CardList query(DeckFormat deckFormat, Predicate<Card> filter) {
		CardList result = new CardArrayList();
		for (Card card : cards.values()) {
			if (card.getDesc().getFileFormatVersion() > version) {
				continue;
			}

			if (card.getCardType().isCardType(CardType.CLASS) || card.getCardType().isCardType(CardType.FORMAT)) {
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
