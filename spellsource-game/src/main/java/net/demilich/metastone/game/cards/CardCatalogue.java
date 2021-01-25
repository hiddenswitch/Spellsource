package net.demilich.metastone.game.cards;

import com.google.common.base.CaseFormat;
import com.google.common.io.CharSource;
import com.hiddenswitch.spellsource.rpc.Spellsource.CardTypeMessage.CardType;
import com.hiddenswitch.spellsource.rpc.Spellsource.RarityMessage.Rarity;
import com.hiddenswitch.spellsource.core.CardResource;
import com.hiddenswitch.spellsource.core.CardResources;
import com.hiddenswitch.spellsource.core.ResourceInputStream;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.logic.GameLogic;
import org.apache.commons.io.input.ReaderInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

/**
 * A place that stores {@link CardCatalogueRecord} records that were generated from the "cards" Java package.
 */
public class CardCatalogue {
	public static final Map<String, DeckFormat> FORMATS = new HashMap<>();
	private static Map<String, Card> classCards;
	private static Map<String, Card> heroCards;
	private static Map<String, Card> formatCards;
	private static Map<DeckFormat, CardList> classCardsForFormat;
	private static Map<DeckFormat, List<String>> baseClassesForFormat;

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
	private final static Map<String, Card> cards = new ConcurrentHashMap<>(8196);
	private final static Map<String, CardCatalogueRecord> records = new LinkedHashMap<>(8196);
	private final static Map<String, List<CardCatalogueRecord>> recordsByName = new LinkedHashMap<>(8196);

	@NotNull
	public static CardList getAll() {
		CardList result = new CardArrayList();
		for (Card card : cards.values()) {
			result.addCard(card.clone());
		}
		return result;
	}

	/**
	 * Returns a mutable reference to the cards loaded into this catalogue.
	 * <p>
	 * Changing the cards here changes all their references in all games. However, {@link #getCardById(String)} returns
	 * clones.
	 *
	 * @return
	 */
	public @NotNull
	static Map<String, Card> getCards() {
		return cards;
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
		return query(deckFormat, (CardType) null, (Rarity) null, (String) null, (Attribute) null, true);
	}

	public static CardList query(DeckFormat deckFormat, CardType cardType) {
		return query(deckFormat, cardType, (Rarity) null, (String) null, (Attribute) null, true);
	}

	public static CardList query(DeckFormat deckFormat, String heroClass) {
		return query(deckFormat, (CardType) null, (Rarity) null, heroClass, (Attribute) null, true);
	}

	public static CardList query(DeckFormat deckFormat, CardType cardType, Rarity rarity, String heroClass) {
		return query(deckFormat, cardType, rarity, heroClass, (Attribute) null, true);
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
	@NotNull
	public static CardList query(DeckFormat deckFormat, CardType cardType, Rarity rarity, String heroClass, Attribute tag, boolean clone) {
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
			if (card.hasAttribute(Attribute.PERMANENT)) {
				continue;
			}
			if (cardType != null && !GameLogic.isCardType(card.getCardType(), cardType)) {
				continue;
			}
			// per default, do not include hero powers, quests, classes, and formats
			if (GameLogic.isCardType(card.getCardType(), CardType.HERO_POWER) || card.isQuest() ||
					(GameLogic.isCardType(card.getCardType(), CardType.CLASS) && cardType != CardType.CLASS) ||
					(GameLogic.isCardType(card.getCardType(), CardType.FORMAT) && cardType != CardType.FORMAT)) {
				continue;
			}
			if (rarity != null && !GameLogic.isRarity(card.getRarity(), rarity)) {
				continue;
			}
			if (heroClass != null && !card.hasHeroClass(heroClass)) {
				continue;
			}
			if (tag != null && !card.hasAttribute(tag)) {
				continue;
			}
			if (clone) {
				card = card.clone();
			}
			result.addCard(card);
		}

		return result;
	}

	/**
	 * Loads all the cards from the specified {@link CardResources} instances.
	 *
	 * @param cardResources
	 */
	public static void loadCardsFromPackage(List<CardResources> cardResources) {
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
		loadCards(inputStreams);

		LOGGER.debug("loadCards: {} cards loaded.", CardCatalogue.cards.size());
	}

	/**
	 * Adds or replaces a card for the given JSON.
	 * <p>
	 * If a {@link CardDesc#getName()} {@code name} field is specified and no {@code id}, the
	 *
	 * @param json
	 */
	public static String addOrReplaceCard(String json) throws IOException {
		var cardDesc = Json.decodeValue(json, CardDesc.class);
		if (cardDesc.getName() == null || cardDesc.getName().isEmpty()) {
			throw new NullPointerException("cardDesc.name");
		}
		if (cardDesc.getType() == null) {
			throw new NullPointerException("cardDesc.type");
		}
		if (cardDesc.getId() == null) {
			cardDesc.setId(CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert((cardDesc.getType().name().toLowerCase() + "" + cardDesc.getName()).replace(" ", "")));
		}

		var id = cardDesc.getId();
		ReaderInputStream targetStream = null;
		try {
			targetStream =
					new ReaderInputStream(CharSource.wrap(Json.encode(cardDesc)).openStream());
			loadCards(Collections.singletonList(new ResourceInputStream(id + ".json", targetStream)));
		} finally {
			if (targetStream != null) {
				targetStream.close();
			}
		}
		return id;
	}

	/**
	 * Removes the specified card by ID from the catalogue.
	 *
	 * @param id
	 */
	public static void removeCard(String id) {
		var res = records.remove(id);
		cards.remove(id);
		if (res != null) {
			recordsByName.remove(res.getDesc().getName());
			if (res.getDesc().getType() == CardType.FORMAT) {
				formatCards.remove(res.getDesc().getName());
			}
			if (res.getDesc().getType() == CardType.CLASS) {
				classCards.remove(res.getDesc().getHeroClass());
				for (var format : classCardsForFormat.keySet()) {
					classCardsForFormat.get(format).removeIf(c -> c.getDesc().getId().equals(res.getId()));
				}
			}
		}
	}

	/**
	 * Loads all the cards from the specified {@link ResourceInputStream} instances, which can be a mix of files and
	 * resources.
	 *
	 * @param inputStreams
	 */
	public static void loadCards(Collection<ResourceInputStream> inputStreams) {
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
		formatCards = formats.stream().collect(toMap(Card::getName, Function.identity()));
		// Populate the class and hero cards
		classCards = cards.values().stream().filter(c -> c.getCardType() == CardType.CLASS).collect(toMap(Card::getHeroClass, Function.identity()));
		classCardsForFormat = formatCards.entrySet().stream().collect(toMap(kv -> DeckFormat.getFormat(kv.getKey()), kv -> {
			var key = DeckFormat.getFormat(kv.getKey());
			return classCards.values().stream().filter(key::isInFormat).collect(Collectors.toCollection(CardArrayList::new));
		}));
		var allClassCards = new CardArrayList();
		allClassCards.addAll(classCards.values());
		classCardsForFormat.put(DeckFormat.all(), allClassCards);
		heroCards =
				classCards.values().stream().map(value -> getCardById(Objects.requireNonNull(value).getHero())).collect(toMap(Card::getHeroClass, i -> i));
		baseClassesForFormat = formatCards.entrySet().stream().collect(toMap(kv -> DeckFormat.getFormat(kv.getKey()), kv -> {
			var key = DeckFormat.getFormat(kv.getKey());
			return classCards.values().stream().filter(c -> c.isCollectible() && key.isInFormat(c)).map(Card::getHeroClass).collect(Collectors.toList());
		}));
		baseClassesForFormat.put(DeckFormat.all(), allClassCards.stream().map(Card::getHeroClass).distinct().collect(Collectors.toList()));
	}

	/**
	 * Loads all the cards from all classpath resources that are recursively inside the "cards" directory.
	 */
	public static void loadAllCards() {
		loadAllCards("cards");
	}

	public static void loadCardsFromFilesystemDirectories(String... directories) {
		if (!firstLoad()) {
			return;
		}

		var inputStreams = new ArrayList<ResourceInputStream>();

		try {
			for (var directory : directories) {
				var path = Path.of(directory);
				if (!Files.exists(path)) {
					continue;
				}
				Stream<Path> walk = null;
				walk = Files.walk(path, FileVisitOption.FOLLOW_LINKS);

				for (var it = walk.iterator(); it.hasNext(); ) {
					var filename = it.next();
					if (filename.getFileName().toString().endsWith(".json")) {
						inputStreams.add(new ResourceInputStream(filename.getFileName().toString(), Files.newInputStream(filename)));
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		loadCards(inputStreams);

		for (var inputStream : inputStreams) {
			try {
				inputStream.getInputStream().close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Loads all the cards from the specified directory.
	 * <p>
	 * Prevents {@link CardCatalogue#loadCardsFromPackage()} from also redundantly loading the same cards.
	 * <p>
	 * Does <b>not</b> use ClassGraph so does not need to allocate direct byte buffers.
	 *
	 * @param directory
	 */
	public static void loadAllCards(String directory) {
		if (!firstLoad()) {
			return;
		}

		var inputStreams = new ArrayList<ResourceInputStream>();
		var closeables = new ArrayList<AutoCloseable>();

		try {
			var allCardDirs = ClassLoader.getSystemClassLoader().getResources(directory).asIterator();
			for (; allCardDirs.hasNext(); ) {
				var uri = allCardDirs.next().toURI();
				var JAR = "jar";
				if (Objects.equals(uri.getScheme(), JAR)) {
					for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
						if (provider.getScheme().equalsIgnoreCase(JAR)) {
							try {
								closeables.add(provider.getFileSystem(uri));
							} catch (FileSystemNotFoundException e) {
								// in this case we need to initialize it first:
								closeables.add(provider.newFileSystem(uri, Collections.emptyMap()));
							}
						}
					}
				}
				var path = Paths.get(uri);
				var walk = Files.walk(path, FileVisitOption.FOLLOW_LINKS);
				for (var it = walk.iterator(); it.hasNext(); ) {
					var filename = it.next();
					if (filename.getFileName().toString().endsWith(".json")) {
						inputStreams.add(new ResourceInputStream(filename.getFileName().toString(), Files.newInputStream(filename)));
					}
				}
			}
		} catch (IOException | URISyntaxException e) {
			throw new RuntimeException(e);
		}

		loadCards(inputStreams);

		for (var inputStream : inputStreams) {
			try {
				inputStream.getInputStream().close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		for (var closeable : closeables) {
			try {
				closeable.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void unloadCards() {
		if (loaded.compareAndSet(true, false)) {
			bannedCardIds.clear();
			hardRemovalCardIds.clear();
			records.clear();
			recordsByName.clear();
			cards.clear();
			formatCards.clear();
			classCards.clear();
			classCardsForFormat.clear();
			heroCards.clear();
			baseClassesForFormat.clear();
			FORMATS.clear();
		}
	}

	/**
	 * Loads all the cards specified in the {@code "cards/src/main/resources" + DEFAULT_CARDS_FOLDER } directory in the
	 * {@code cards} module. This can be called multiple times, but will not "refresh" the catalogue file.
	 */
	public synchronized static void loadCardsFromPackage()  /*IOException, URISyntaxException*/ /*, CardParseException*/ {
		if (!firstLoad()) {
			return;
		}
		List<CardResources> cardResources = null;

		try (ScanResult scanResult =
				     new ClassGraph()
						     .enableClassInfo()
						     .disableRuntimeInvisibleAnnotations()
						     .acceptPackages("com.hiddenswitch.spellsource.cards.*")
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
			loadCardsFromPackage(cardResources);
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

	public static boolean firstLoad() {
		return loaded.compareAndSet(false, true);
	}

	private static void workaroundClassGraphCreatingDirectBuffers() {
		// Workaround for scan result
		try {
			var initialized1 = ScanResult.class.getDeclaredField("initialized");
			initialized1.setAccessible(true);
			AtomicBoolean initialized = (AtomicBoolean) initialized1.get(null);
			initialized.set(true);
		} catch (IllegalAccessException | NoSuchFieldException e) {
			throw new RuntimeException(e);
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
		return classCardsForFormat.get(format);
	}

	/**
	 * Retrieves the {@link net.demilich.metastone.game.entities.heroes.HeroClass} strings that correspond to the classes
	 * in the specified format.
	 *
	 * @param deckFormat
	 * @return
	 */
	public static List<String> getBaseClasses(DeckFormat deckFormat) {
		return baseClassesForFormat.get(deckFormat);
	}

	public static CardList query(DeckFormat deckFormat, Predicate<Card> filter) {
		CardList result = new CardArrayList();
		for (Card card : cards.values()) {
			if (card.getDesc().getFileFormatVersion() > version) {
				continue;
			}

			if (GameLogic.isCardType(card.getCardType(), CardType.CLASS) || GameLogic.isCardType(card.getCardType(), CardType.FORMAT)) {
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
