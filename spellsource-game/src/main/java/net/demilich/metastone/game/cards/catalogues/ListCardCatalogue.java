package net.demilich.metastone.game.cards.catalogues;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.hiddenswitch.protos.Serialization;
import com.hiddenswitch.spellsource.core.ResourceInputStream;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class ListCardCatalogue implements CardCatalogue {
	private static final Logger LOGGER = LoggerFactory.getLogger(ListCardCatalogue.class);

	/**
	 * Cached query results. Key is a composite of query parameters, value is an immutable list of cards matching
	 * the query. Invalidated whenever cards are loaded or removed.
	 */
	private final Cache<QueryKey, List<Card>> queryCache = Caffeine.newBuilder()
			.maximumSize(256)
			.build();
	private final java.util.concurrent.ConcurrentHashMap<DeckFormat, List<Card>> defaultQueryByFormat = new java.util.concurrent.ConcurrentHashMap<>(16);
	private final java.util.concurrent.ConcurrentHashMap<DeckFormat, List<Card>> uncollectibleQueryByFormat = new java.util.concurrent.ConcurrentHashMap<>(16);

	private record QueryKey(Set<String> formatSets, Spellsource.CardTypeMessage.CardType cardType,
	                        Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag) {
	}

	static {
		Serialization.configureSerialization();
	}

	protected final ReadWriteLock lock = new ReentrantReadWriteLock();
	protected final Map<String, DeckFormat> formatsByName = new LinkedHashMap<>(64);
	protected final Set<String> bannedCardIds = new LinkedHashSet<>();
	protected final Set<String> hardRemovalCardIds = new LinkedHashSet<>();
	protected final Map<String, Card> cards = new LinkedHashMap<>(8196);
	protected final Multimap<String, Card> cardsByName = Multimaps.newSetMultimap(new LinkedHashMap<>(8196), LinkedHashSet::new);
	protected final Map<String, Card> classCards = new LinkedHashMap<>(64);
	protected final Map<String, Card> heroCards = new LinkedHashMap<>(64);
	protected final Map<String, Card> formatCardsByName = new LinkedHashMap<>(64);
	protected final Multimap<DeckFormat, Card> classCardsForFormat = Multimaps.newSetMultimap(new LinkedHashMap<>(64), LinkedHashSet::new);
	protected final Multimap<DeckFormat, String> baseClassesForFormat = Multimaps.newSetMultimap(new LinkedHashMap<>(64), LinkedHashSet::new);
	protected final java.util.concurrent.ConcurrentHashMap<DeckFormat, List<String>> cachedBaseClassesForFormat = new java.util.concurrent.ConcurrentHashMap<>(16);

	@Override
	public Map<String, DeckFormat> formats() {
		return formatsByName;
	}

	@Override
	public DeckFormat getFormat(String name) {
		return formatsByName.get(name);
	}

	@Override
	public Set<String> getBannedDraftCards() {
		return Collections.unmodifiableSet(bannedCardIds);
	}

	@Override
	public Set<String> getHardRemovalCardIds() {
		return Collections.unmodifiableSet(hardRemovalCardIds);
	}

	@Override
	@NotNull
	public CardList getAll() {
		lock.readLock().lock();
		try {
			CardList result = new CardArrayList();
			for (var card : cards.values()) {
				result.addCard(card.clone());
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public @NotNull Map<String, Card> getCards() {
		lock.readLock().lock();
		try {
			return cards;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public @NotNull Card getCardById(@NotNull String id) {
		lock.readLock().lock();
		try {
			var card = cards.getOrDefault(id, null);
			if (card != null) {
				card = card.getCopy();
			} else {
				throw new NullPointerException(id);
			}
			return card;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	@Nullable
	public Card getCardByName(String name) {
		lock.readLock().lock();
		try {
			var namedCard = cardsByName.get(name).stream().filter(ccr -> ccr.getDesc().isCollectible()).findFirst().orElse(cardsByName.get(name).stream().findFirst().orElse(null));
			if (namedCard != null) {
				return getCardById(namedCard.getCardId());
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	@NotNull
	public Card getCardByName(String name, String heroClass) {
		lock.readLock().lock();
		try {
			var namedCards = cardsByName.get(name).stream().filter(ccr -> ccr.getDesc().isCollectible()).toList();
			if (!namedCards.isEmpty()) {
				if (namedCards.size() > 1) {
					for (var namedCard : namedCards) {
						var card = getCardById(namedCard.getCardId());
						if (card.hasHeroClass(heroClass)) {
							return card;
						}
					}
				}
				return getCardById(namedCards.get(0).getCardId());
			}
			return getCardById(cardsByName.get(name).stream().findFirst().orElseThrow(NullPointerException::new).getDesc().getId());
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	@NotNull
	public CardList query(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag, boolean clone) {
		boolean isDefaultQuery = cardType == null && rarity == null && heroClass == null && tag == null && deckFormat.getClass() == DeckFormat.class;
		if (isDefaultQuery) {
			List<Card> cached = defaultQueryByFormat.get(deckFormat);
			if (cached != null) {
				if (!clone) {
					return new CardArrayList(cached);
				}
				CardArrayList result = new CardArrayList(cached.size());
				for (int i = 0, n = cached.size(); i < n; i++) {
					result.addCard(cached.get(i).clone());
				}
				return result;
			}
		}
		lock.readLock().lock();
		try {
			List<Card> matched;
			if (deckFormat.getClass() == DeckFormat.class) {
				var sets = new LinkedHashSet<>(deckFormat.getSets());
				sets.remove(null);
				var key = new QueryKey(sets, cardType, rarity, heroClass, tag);
				matched = queryCache.get(key, k -> computeQuery(deckFormat, cardType, rarity, heroClass, tag));
				if (isDefaultQuery) {
					defaultQueryByFormat.put(deckFormat, matched);
				}
			} else {
				matched = computeQuery(deckFormat, cardType, rarity, heroClass, tag);
			}
			if (!clone) {
				return new CardArrayList(matched);
			}
			CardArrayList result = new CardArrayList(matched.size());
			for (int i = 0, n = matched.size(); i < n; i++) {
				result.addCard(matched.get(i).clone());
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	private List<Card> computeQuery(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag) {
		var matched = new ArrayList<Card>();
		for (var card : cards.values()) {
			var desc = card.getDesc();
			var actualCardType = card.getCardType();
			if (!deckFormat.isInFormat(card)) {
				continue;
			}
			if (!desc.isCollectible()) {
				continue;
			}
			if (hasStaticAttribute(desc, Attribute.PERMANENT)) {
				continue;
			}
			if (cardType != null && !GameLogic.isCardType(actualCardType, cardType)) {
				continue;
			}
			if (actualCardType == Spellsource.CardTypeMessage.CardType.HERO_POWER
					|| card.isQuest()
					|| (actualCardType == Spellsource.CardTypeMessage.CardType.CLASS && cardType != Spellsource.CardTypeMessage.CardType.CLASS)
					|| (actualCardType == Spellsource.CardTypeMessage.CardType.FORMAT && cardType != Spellsource.CardTypeMessage.CardType.FORMAT)) {
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
			matched.add(card);
		}
		return List.copyOf(matched);
	}

	public void removeCard(String id) {
		lock.writeLock().lock();
		try {
			queryCache.invalidateAll();
			defaultQueryByFormat.clear();
			uncollectibleQueryByFormat.clear();
			cachedBaseClassesForFormat.clear();
			var res = cards.remove(id);
			if (res != null) {
				cardsByName.remove(res.getDesc().getName(), res);
				switch (res.getDesc().getType()) {
					case FORMAT -> {
						formatCardsByName.remove(res.getDesc().getName());
						formatsByName.remove(res.getDesc().getName());
					}
					case CLASS -> {
						classCards.remove(res.getDesc().getHeroClass());
						for (var format : classCardsForFormat.keySet()) {
							classCardsForFormat.get(format).removeIf(c -> c.getDesc().getId().equals(res.getCardId()));
						}
					}
					case HERO -> heroCards.remove(res.getCardId());
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public Card getFormatCard(String name) {
		lock.readLock().lock();
		try {
			return formatCardsByName.getOrDefault(name, null);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Card getHeroCard(String heroClass) {
		lock.readLock().lock();
		try {
			var heroCard = heroCards.getOrDefault(heroClass, null);
			if (heroCard == null) {
				return getCardById(this.getNeutralHero());
			}
			return heroCard;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public CardList getClassCards(DeckFormat format) {
		lock.readLock().lock();
		try {
			return new CardArrayList(classCardsForFormat.get(format));
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<String> getBaseClasses(DeckFormat deckFormat) {
		var cached = cachedBaseClassesForFormat.get(deckFormat);
		if (cached != null) {
			return cached;
		}
		lock.readLock().lock();
		try {
			var list = List.copyOf(baseClassesForFormat.get(deckFormat));
			cachedBaseClassesForFormat.put(deckFormat, list);
			return list;
		} finally {
			lock.readLock().unlock();
		}
	}

	public CardList query(DeckFormat deckFormat, Predicate<Card> filter) {
		lock.readLock().lock();
		try {
			CardList result = new CardArrayList();
			for (var card : cards.values()) {
				if (GameLogic.isCardType(card.getCardType(), Spellsource.CardTypeMessage.CardType.CLASS) || GameLogic.isCardType(card.getCardType(), Spellsource.CardTypeMessage.CardType.FORMAT)) {
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
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Stream<Card> stream() {
		return cards.values().stream();
	}

	@Override
	public CardList queryClassCards(DeckFormat format, String hero, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		lock.readLock().lock();
		try {
			CardList result = new CardArrayList();
			for (var card : cards.values()) {
				if (isDeckCandidate(card, format, validCardTypes)
						&& card.hasHeroClass(hero)
						&& !bannedCards.contains(card.getCardId())
						&& card.getRarity() == rarity) {
					result.addCard(card.clone());
				}
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public CardList queryNeutrals(DeckFormat format, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		lock.readLock().lock();
		try {
			CardList result = new CardArrayList();
			for (var card : cards.values()) {
				if (isDeckCandidate(card, format, validCardTypes)
						&& card.hasHeroClass(HeroClass.ANY)
						&& !bannedCards.contains(card.getCardId())
						&& card.getRarity() == rarity) {
					result.addCard(card.clone());
				}
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<Card> queryClassCards(DeckFormat deckFormat, String heroClass) {
		lock.readLock().lock();
		try {
			var result = new ArrayList<Card>();
			for (var card : cards.values()) {
				if (isDeckCandidate(card, deckFormat, null) && card.hasHeroClass(heroClass)) {
					result.add(card.clone());
				}
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<Card> queryNeutrals(DeckFormat deckFormat) {
		lock.readLock().lock();
		try {
			var result = new ArrayList<Card>();
			for (var card : cards.values()) {
				if (isDeckCandidate(card, deckFormat, null) && card.hasHeroClass(HeroClass.ANY)) {
					result.add(card.clone());
				}
			}
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	private static boolean hasStaticAttribute(CardDesc desc, Attribute attribute) {
		if (desc.getAttributes() == null) {
			return false;
		}
		Object value = desc.getAttributes().get(attribute);
		if (value instanceof Boolean booleanValue) {
			return booleanValue;
		}
		if (value instanceof Integer intValue) {
			return intValue != 0;
		}
		return value != null;
	}

	private static boolean isDeckCandidate(Card card, DeckFormat deckFormat, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		var desc = card.getDesc();
		var cardType = card.getCardType();
		if (!deckFormat.isInFormat(card) || !desc.isCollectible() || card.isQuest() || hasStaticAttribute(desc, Attribute.PERMANENT)) {
			return false;
		}
		if (cardType == Spellsource.CardTypeMessage.CardType.HERO_POWER
				|| cardType == Spellsource.CardTypeMessage.CardType.CLASS
				|| cardType == Spellsource.CardTypeMessage.CardType.FORMAT) {
			return false;
		}
		return validCardTypes == null || validCardTypes.contains(cardType);
	}

	@Override
	public CardList queryUncollectible(DeckFormat deckFormat) {
		if (deckFormat != null && deckFormat.getClass() == DeckFormat.class) {
			List<Card> cached = uncollectibleQueryByFormat.get(deckFormat);
			if (cached != null) {
				return new CardArrayList(cached);
			}
		}
		lock.readLock().lock();
		try {
			var matched = new ArrayList<Card>();
			for (var card : cards.values()) {
				if (GameLogic.isCardType(card.getCardType(), Spellsource.CardTypeMessage.CardType.CLASS) || GameLogic.isCardType(card.getCardType(), Spellsource.CardTypeMessage.CardType.FORMAT)) {
					continue;
				}
				if (deckFormat != null && !deckFormat.isInFormat(card)) {
					continue;
				}
				matched.add(card);
			}
			List<Card> immutable = List.copyOf(matched);
			if (deckFormat != null && deckFormat.getClass() == DeckFormat.class) {
				uncollectibleQueryByFormat.put(deckFormat, immutable);
			}
			return new CardArrayList(immutable);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Adds or replaces a card for the given JSON.
	 *
	 * @param json
	 */
	public String addOrReplaceCard(String json) {
		var cardDesc = Json.decodeValue(json, CardDesc.class);
		return addOrReplaceCard(cardDesc);
	}

	public String addOrReplaceCard(CardDesc cardDesc) {
		if (cardDesc.getName() == null || cardDesc.getName().isEmpty()) {
			throw new NullPointerException("cardDesc.name");
		}
		if (cardDesc.getType() == null) {
			throw new NullPointerException("cardDesc.type");
		}
		if (cardDesc.getId() == null) {
			// TODO new random id ?
			cardDesc.setId(CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert((cardDesc.getType().name().toLowerCase() + "" + cardDesc.getName()).replace(" ", "")));
		}

		var id = cardDesc.getId();
		loadCards(Collections.singletonList(cardDesc));
		return id;
	}

	/**
	 * Clears all the cards in this catalogue.
	 */
	public void clear() {
		lock.writeLock().lock();
		try {
			queryCache.invalidateAll();
			defaultQueryByFormat.clear();
			uncollectibleQueryByFormat.clear();
			formatsByName.clear();
			bannedCardIds.clear();
			hardRemovalCardIds.clear();
			cards.clear();
			cardsByName.clear();
			classCards.clear();
			heroCards.clear();
			formatCardsByName.clear();
			classCardsForFormat.clear();
			baseClassesForFormat.clear();
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Copies all cards and format indices from another {@link ListCardCatalogue} whose cards are already frozen.
	 *
	 * @param other The source catalogue to copy from.
	 */
	protected void copyFrom(ListCardCatalogue other) {
		lock.writeLock().lock();
		other.lock.readLock().lock();
		try {
			clear();
			formatsByName.putAll(other.formatsByName);
			bannedCardIds.addAll(other.bannedCardIds);
			hardRemovalCardIds.addAll(other.hardRemovalCardIds);
			cards.putAll(other.cards);
			cardsByName.putAll(other.cardsByName);
			classCards.putAll(other.classCards);
			heroCards.putAll(other.heroCards);
			formatCardsByName.putAll(other.formatCardsByName);
			classCardsForFormat.putAll(other.classCardsForFormat);
			baseClassesForFormat.putAll(other.baseClassesForFormat);
			cachedBaseClassesForFormat.clear();
		} finally {
			other.lock.readLock().unlock();
			lock.writeLock().unlock();
		}
	}

	/**
	 * Loads all the cards from the specified card descriptions.
	 *
	 * @param cardDescs
	 */
	public void loadCards(List<CardDesc> cardDescs) {
		updatedWith(cardDescs.stream().collect(toMap(CardDesc::getId, Function.identity())));
	}

	/**
	 * Loads all the cards from the specified {@link ResourceInputStream} instances, which can be a mix of files and resources.
	 *
	 * @param inputStreams
	 */
	public void loadCards(Collection<ResourceInputStream> inputStreams) {
		Map<String, CardDesc> cardDescs = new LinkedHashMap<>();
		var cardParser = new CardParser();

		for (var resourceInputStream : inputStreams) {
			try {
				var desc = cardParser.parseCard(resourceInputStream).desc();
				if (cardDescs.containsKey(desc.getId())) {
					LOGGER.error("loadCards: Card id {} is duplicated!", desc.getId());
				}
				cardDescs.put(desc.getId(), desc);
			} catch (Exception e) {
				LOGGER.error("loadCards: An error occurred while processing {}: {}", resourceInputStream.getFileName(), e.toString());
			}
		}

		updatedWith(cardDescs);
	}

	/**
	 * Updates the internal data structures with the provided card descs
	 *
	 * @param cardDescs
	 */
	protected void updatedWith(Map<String, CardDesc> cardDescs) {
		lock.writeLock().lock();
		try {
			queryCache.invalidateAll();
			defaultQueryByFormat.clear();
			uncollectibleQueryByFormat.clear();
			cachedBaseClassesForFormat.clear();
			var newCards = new ArrayList<Card>(cardDescs.size());
			// sort so that this is more consistent
			List<CardDesc> values = new ArrayList<>(cardDescs.values());
			values.sort(Comparator.comparing(CardDesc::getId));
			for (var desc : values) {
				// TODO more manual checks for whether a card is valid for play (references nonexistent cards / attributes / etc)
				var instance = desc.create();
				newCards.add(instance);
				// find old name and remove it if it exists
				var existing = cards.get(desc.getId());
				if (existing != null) {
					cardsByName.remove(existing.getDesc().getName(), existing);
					switch (existing.getDesc().getType()) {
						case FORMAT -> {
							formatsByName.remove(existing.getDesc().getName());
							formatCardsByName.remove(existing.getDesc().getName());
						}
						case CLASS -> {
							classCards.remove(existing.getDesc().getHeroClass());
							for (var format : classCardsForFormat.keySet()) {
								classCardsForFormat.get(format).removeIf(c -> c.getDesc().getId().equals(existing.getCardId()));
							}
						}
						case HERO -> heroCards.remove(existing.getCardId());
					}
				}

				cards.put(instance.getCardId(), instance);
				cardsByName.put(desc.getName(), instance);
				if (desc.draft() != null && desc.draft().getBanned()) {
					bannedCardIds.add(desc.getId());
				} else {
					bannedCardIds.remove(desc.getId());
				}
				if (desc.artificialIntelligence() != null && desc.artificialIntelligence().getHardRemoval()) {
					hardRemovalCardIds.add(desc.getId());
				} else {
					hardRemovalCardIds.remove(desc.getId());
				}
			}

			for (var card : newCards) {
				all.addSet(card.getCardSet());
			}

			var formatCards = newCards.stream().filter(card -> card.getCardType() == Spellsource.CardTypeMessage.CardType.FORMAT).toList();

			formatsByName.put(FORMAT_NAME_ALL, this.all);
			for (var formatCard : formatCards) {
				formatsByName.put(formatCard.getName(), new DeckFormat().setSecondPlayerBonusCards(formatCard.getDesc().getSecondPlayerBonusCards()).setValidDeckCondition(formatCard.getDesc().getCondition()).withName(formatCard.getName()).withCardSets(formatCard.getCardSets()));
			}
			formatCardsByName.putAll(formatCards.stream().collect(toMap(Card::getName, Function.identity())));
			// Populate the class and hero cards
			classCards.putAll(newCards.stream().filter(c -> c.getCardType() == Spellsource.CardTypeMessage.CardType.CLASS).collect(toMap(c -> c.getDesc().getHeroClass(), Function.identity())));
			for (var classCard : classCards.values()) {
				String heroClass = classCard.getDesc().getHeroClass();
				// skip the Neutral class
				if (heroClass.equals(HeroClass.ANY)) {
					continue;
				}

				// populate the tables
				for (var format : formats().values()) {
					if (format.isInFormat(classCard)) {
						classCardsForFormat.get(format).removeIf(existing -> Objects.equals(existing.getCardId(), classCard.getCardId()));
						classCardsForFormat.put(format, classCard);
						baseClassesForFormat.put(format, heroClass);
					}
				}
				classCardsForFormat.get(all()).removeIf(existing -> Objects.equals(existing.getCardId(), classCard.getCardId()));
				classCardsForFormat.put(all(), classCard);
				baseClassesForFormat.put(all(), heroClass);
			}
			heroCards.putAll(classCards
					.values()
					.stream()
					// when concatenating multiple cards together, you may have a class card without its hero card
					// or vice versa. This should not cause a problem in terms of the consistency of the card catalogue
					// even if it's not super useful for a game
					.filter(card -> card.getHero() != null && cards.containsKey(card.getHero()))
					.map(value -> cards.get(Objects.requireNonNull(value).getHero()))
					.collect(toMap(c -> c.getDesc().getHeroClass(), Function.identity())));
			// Freeze all cards so their descs are read-only and shared safely
			for (var card : newCards) {
				card.freeze();
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Walks the provided filesystem directories and adds all the JSON files located in them
	 *
	 * @param directories
	 */
	public void loadCardsFromFilesystemDirectories(String... directories) {
		lock.writeLock().lock();
		try {
			var inputStreams = new ArrayList<ResourceInputStream>();

			try {
				for (var directory : directories) {
					var path = Path.of(directory);
					if (!Files.exists(path)) {
						continue;
					}
					var walk = Files.walk(path, FileVisitOption.FOLLOW_LINKS);

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
		} finally {
			lock.writeLock().unlock();
		}

	}
}
