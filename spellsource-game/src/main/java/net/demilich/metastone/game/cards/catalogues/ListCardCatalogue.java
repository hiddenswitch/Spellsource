package net.demilich.metastone.game.cards.catalogues;

import com.google.common.base.CaseFormat;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.hiddenswitch.protos.Serialization;
import com.hiddenswitch.spellsource.core.ResourceInputStream;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.await.Async;
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
	static {
		Serialization.configureSerialization();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ListCardCatalogue.class);
	protected final ReadWriteLock lock = new ReentrantReadWriteLock();
	protected final Map<String, DeckFormat> formatsByName = new HashMap<>(64);
	protected final Set<String> bannedCardIds = new HashSet<>();
	protected final Set<String> hardRemovalCardIds = new HashSet<>();
	protected final Map<String, Card> cards = new HashMap<>(8196);
	protected final Map<String, CardCatalogueRecord> records = new HashMap<>(8196);
	protected final Multimap<String, CardCatalogueRecord> recordsByName = Multimaps.newSetMultimap(new HashMap<>(8196), HashSet::new);
	protected final Map<String, Card> classCards = new HashMap<>(64);
	protected final Map<String, Card> heroCards = new HashMap<>(64);
	protected final Map<String, Card> formatCardsByName = new HashMap<>(64);
	protected final Multimap<DeckFormat, Card> classCardsForFormat = Multimaps.newSetMultimap(new HashMap<>(64), HashSet::new);
	protected final Multimap<DeckFormat, String> baseClassesForFormat = Multimaps.newSetMultimap(new HashMap<>(64), HashSet::new);

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
		Async.lock(lock.readLock());
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
		Async.lock(lock.readLock());
		try {
			return cards;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public @NotNull Card getCardById(@NotNull String id) {
		Async.lock(lock.readLock());
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
	@NotNull
	public Map<String, CardCatalogueRecord> getRecords() {
		return Collections.unmodifiableMap(records);
	}

	@Override
	@Nullable
	public Card getCardByName(String name) {
		Async.lock(lock.readLock());
		try {
			var namedCard = recordsByName.get(name).stream().filter(ccr -> ccr.getDesc().isCollectible()).findFirst().orElse(recordsByName.get(name).stream().findFirst().orElse(null));
			if (namedCard != null) {
				return getCardById(namedCard.getId());
			}
			return null;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	@NotNull
	public Card getCardByName(String name, String heroClass) {
		Async.lock(lock.readLock());
		try {
			var namedCards = recordsByName.get(name).stream().filter(ccr -> ccr.getDesc().isCollectible()).toList();
			if (!namedCards.isEmpty()) {
				if (namedCards.size() > 1) {
					for (var namedCard : namedCards) {
						var card = getCardById(namedCard.getId());
						if (card.hasHeroClass(heroClass)) {
							return card;
						}
					}
				}
				return getCardById(namedCards.get(0).getId());
			}
			return getCardById(recordsByName.get(name).stream().findFirst().orElseThrow(NullPointerException::new).getDesc().getId());
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	@NotNull
	public CardList query(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag, boolean clone) {
		Async.lock(lock.readLock());
		try {
			CardList result = new CardArrayList();
			for (var card : cards.values()) {

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
				if (GameLogic.isCardType(card.getCardType(), Spellsource.CardTypeMessage.CardType.HERO_POWER) || card.isQuest() || (GameLogic.isCardType(card.getCardType(),
						Spellsource.CardTypeMessage.CardType.CLASS) && cardType != Spellsource.CardTypeMessage.CardType.CLASS) || (GameLogic.isCardType(card.getCardType(),
						Spellsource.CardTypeMessage.CardType.FORMAT) && cardType != Spellsource.CardTypeMessage.CardType.FORMAT)) {
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
				} else {
					throw new UnsupportedOperationException("must clone");
				}
				result.addCard(card);
			}

			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	public void removeCard(String id) {
		Async.lock(lock.writeLock());
		try {
			var res = records.remove(id);
			cards.remove(id);
			if (res != null) {
				recordsByName.remove(res.getDesc().getName(), res);
				switch (res.desc().getType()) {
					case FORMAT -> {
						formatCardsByName.remove(res.getDesc().getName());
						formatsByName.remove(res.getDesc().getName());
					}
					case CLASS -> {
						classCards.remove(res.getDesc().getHeroClass());
						for (var format : classCardsForFormat.keySet()) {
							classCardsForFormat.get(format).removeIf(c -> c.getDesc().getId().equals(res.getId()));
						}
					}
					case HERO -> heroCards.remove(res.id());
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public Card getFormatCard(String name) {
		Async.lock(lock.readLock());
		try {
			return formatCardsByName.getOrDefault(name, null);
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public Card getHeroCard(String heroClass) {
		Async.lock(lock.readLock());
		try {
			return heroCards.getOrDefault(heroClass, getCardById(this.getNeutralHero()));
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public CardList getClassCards(DeckFormat format) {
		Async.lock(lock.readLock());
		try {
			return new CardArrayList(classCardsForFormat.get(format));
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public List<String> getBaseClasses(DeckFormat deckFormat) {
		Async.lock(lock.readLock());
		try {
			return new ArrayList<>(baseClassesForFormat.get(deckFormat));
		} finally {
			lock.readLock().unlock();
		}
	}

	public CardList query(DeckFormat deckFormat, Predicate<Card> filter) {
		Async.lock(lock.readLock());
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
		Async.lock(lock.readLock());
		try {
			return query(format, c -> c.hasHeroClass(hero) && !bannedCards.contains(c.getCardId()) && c.getRarity() == rarity && validCardTypes.contains(c.getCardType()) && c.isCollectible());
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public CardList queryNeutrals(DeckFormat format, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		Async.lock(lock.readLock());
		try {
			return query(format,
					c -> c.hasHeroClass(HeroClass.ANY) && !bannedCards.contains(c.getCardId()) && c.getRarity() == rarity && validCardTypes.contains(c.getCardType()) && c.isCollectible());
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public CardList queryUncollectible(DeckFormat deckFormat) {
		Async.lock(lock.readLock());
		try {
			return query(deckFormat, always -> true);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Adds or replaces a card for the given JSON.
	 *
	 * @param json
	 */
	public String addOrReplaceCard(String json) throws IOException {
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
		Async.lock(lock.writeLock());
		try {


			formatsByName.clear();
			bannedCardIds.clear();
			hardRemovalCardIds.clear();
			cards.clear();
			records.clear();
			recordsByName.clear();
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
	 * Loads all the cards from the specified card descriptions.
	 *
	 * @param cardDescs
	 */
	public void loadCards(List<CardDesc> cardDescs) {
		updatedWith(cardDescs.stream().collect(toMap(CardDesc::getId, Function.identity())));
	}

	/**
	 * Loads all the cards from the specified {@link ResourceInputStream} instances, which can be a mix of files and
	 * resources.
	 *
	 * @param inputStreams
	 */
	public void loadCards(Collection<ResourceInputStream> inputStreams) {
		Map<String, CardDesc> cardDescs = new HashMap<>();
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
		Async.lock(lock.writeLock());
		try {
			var newCards = new ArrayList<Card>(cardDescs.size());
			for (var desc : cardDescs.values()) {
				// TODO more manual checks for whether a card is valid for play (references nonexistent cards / attributes / etc)
				var instance = desc.create();
				newCards.add(instance);
				cards.put(instance.getCardId(), instance);
				var record = new CardCatalogueRecord(desc.getId(), desc);
				records.put(desc.getId(), record);
				// find old name and remove it if it exists
				var existing = records.get(desc.getId());
				if (existing != null) {
					recordsByName.remove(existing.desc().getName(), existing);
					if (existing.desc().getType() == Spellsource.CardTypeMessage.CardType.FORMAT) {
						formatsByName.remove(existing.desc().getName());
						formatCardsByName.remove(existing.desc().getName());
					}
				}
				recordsByName.put(desc.getName(), record);
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
			classCards.putAll(newCards.stream().filter(c -> c.getCardType() == Spellsource.CardTypeMessage.CardType.CLASS).collect(toMap(Card::getHeroClass, Function.identity())));
			for (var classCard : classCards.values()) {
				for (var format : formats().values()) {
					if (format.isInFormat(classCard)) {
						classCardsForFormat.get(format).removeIf(existing -> Objects.equals(existing.getCardId(), classCard.getCardId()));
						classCardsForFormat.put(format, classCard);
						baseClassesForFormat.put(format, classCard.getHeroClass());
					}
				}
				classCardsForFormat.get(all()).removeIf(existing -> Objects.equals(existing.getCardId(), classCard.getCardId()));
				classCardsForFormat.put(all(), classCard);
				baseClassesForFormat.put(all(), classCard.getHeroClass());
			}

			heroCards.putAll(classCards.values().stream().map(value -> getCardById(Objects.requireNonNull(value).getHero())).collect(toMap(Card::getHeroClass, Function.identity())));
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void loadCardsFromFilesystemDirectories(String... directories) {
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
}
