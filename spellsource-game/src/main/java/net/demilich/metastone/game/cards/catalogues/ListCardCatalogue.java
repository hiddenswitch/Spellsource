package net.demilich.metastone.game.cards.catalogues;

import com.google.common.base.CaseFormat;
import com.google.common.io.CharSource;
import com.hiddenswitch.protos.Serialization;
import com.hiddenswitch.spellsource.core.ResourceInputStream;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.core.json.Json;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.logic.GameLogic;
import org.apache.commons.io.input.ReaderInputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public class ListCardCatalogue implements CardCatalogue {
	static {
		Serialization.configureSerialization();
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ListCardCatalogue.class);
	protected final Map<String, DeckFormat> formats = new HashMap<>();
	protected final Set<String> bannedCardIds = new HashSet<>();
	protected final Set<String> hardRemovalCardIds = new HashSet<>();
	protected final Map<String, Card> cards = new ConcurrentHashMap<>(8196);
	protected final Map<String, CardCatalogueRecord> records = new LinkedHashMap<>(8196);
	protected final Map<String, List<CardCatalogueRecord>> recordsByName = new LinkedHashMap<>(8196);
	protected Map<String, Card> classCards;
	protected Map<String, Card> heroCards;
	protected Map<String, Card> formatCards;
	protected Map<DeckFormat, CardList> classCardsForFormat;
	protected Map<DeckFormat, List<String>> baseClassesForFormat;

	@Override
	public Map<String, DeckFormat> formats() {
		return formats;
	}

	@Override
	public DeckFormat getFormat(String name) {
		return formats.get(name);
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
		CardList result = new CardArrayList();
		for (Card card : cards.values()) {
			result.addCard(card.clone());
		}
		return result;
	}

	@Override
	public @NotNull
	Map<String, Card> getCards() {
		return cards;
	}

	@Override
	public @NotNull
	Card getCardById(@NotNull String id) {
		Card card = cards.getOrDefault(id.toLowerCase(), null);
		if (card != null) {
			card = card.getCopy();
		} else {
			throw new NullPointerException(id);
		}
		return card;
	}

	@Override
	@NotNull
	public Map<String, CardCatalogueRecord> getRecords() {
		return Collections.unmodifiableMap(records);
	}

	@Override
	@Nullable
	public Card getCardByName(String name) {
		CardCatalogueRecord namedCard = recordsByName.get(name).stream().filter(ccr -> ccr.getDesc().isCollectible()).findFirst().orElse(recordsByName.get(name).get(0));
		if (namedCard != null) {
			return getCardById(namedCard.getId());
		}
		return null;
	}

	@Override
	public Card getCardByName(String name, String heroClass) {
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

	@Override
	@NotNull
	public CardList query(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag, boolean clone) {
		CardList result = new CardArrayList();
		for (Card card : cards.values()) {

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
			if (GameLogic.isCardType(card.getCardType(), Spellsource.CardTypeMessage.CardType.HERO_POWER) || card.isQuest() ||
					(GameLogic.isCardType(card.getCardType(), Spellsource.CardTypeMessage.CardType.CLASS) && cardType != Spellsource.CardTypeMessage.CardType.CLASS) ||
					(GameLogic.isCardType(card.getCardType(), Spellsource.CardTypeMessage.CardType.FORMAT) && cardType != Spellsource.CardTypeMessage.CardType.FORMAT)) {
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

	public void removeCard(String id) {
		var res = records.remove(id);
		cards.remove(id);
		if (res != null) {
			recordsByName.remove(res.getDesc().getName());
			if (res.getDesc().getType() == Spellsource.CardTypeMessage.CardType.FORMAT) {
				formatCards.remove(res.getDesc().getName());
			}
			if (res.getDesc().getType() == Spellsource.CardTypeMessage.CardType.CLASS) {
				classCards.remove(res.getDesc().getHeroClass());
				for (var format : classCardsForFormat.keySet()) {
					classCardsForFormat.get(format).removeIf(c -> c.getDesc().getId().equals(res.getId()));
				}
			}
		}
	}

	@Override
	public Card getFormatCard(String name) {
		return formatCards.getOrDefault(name, null);
	}

	@Override
	public Card getHeroCard(String heroClass) {
		return heroCards.getOrDefault(heroClass, getCardById(this.getNeutralHero()));
	}

	@Override
	public CardList getClassCards(DeckFormat format) {
		return classCardsForFormat.get(format);
	}

	@Override
	public List<String> getBaseClasses(DeckFormat deckFormat) {
		return baseClassesForFormat.get(deckFormat);
	}

	public CardList query(DeckFormat deckFormat, Predicate<Card> filter) {
		CardList result = new CardArrayList();
		for (Card card : cards.values()) {
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
	}

	@Override
	public Stream<Card> stream() {
		return cards.values().stream();
	}

	@Override
	public CardList queryClassCards(DeckFormat format, String hero, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		return query(format, c ->
				c.hasHeroClass(hero)
						&& !bannedCards.contains(c.getCardId())
						&& c.getRarity() == rarity
						&& validCardTypes.contains(c.getCardType())
						&& c.isCollectible());
	}

	@Override
	public CardList queryNeutrals(DeckFormat format, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		return query(format, c -> c.hasHeroClass(HeroClass.ANY)
				&& !bannedCards.contains(c.getCardId())
				&& c.getRarity() == rarity
				&& validCardTypes.contains(c.getCardType())
				&& c.isCollectible());
	}

	@Override
	public CardList queryUncollectible(DeckFormat deckFormat) {
		return query(deckFormat, always -> true);
	}

	/**
	 * Adds or replaces a card for the given JSON.
	 * <p>
	 * If a {@link CardDesc#getName()} {@code name} field is specified and no {@code id}, the
	 *
	 * @param json
	 */
	public String addOrReplaceCard(String json) throws IOException {
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
		try (ReaderInputStream targetStream = new ReaderInputStream(CharSource.wrap(Json.encode(cardDesc)).openStream(), Charset.defaultCharset())) {
			loadCards(Collections.singletonList(new ResourceInputStream(id + ".json", targetStream)));
		}
		return id;
	}

	/**
	 * Loads all the cards from the specified {@link ResourceInputStream} instances, which can be a mix of files and
	 * resources.
	 *
	 * @param inputStreams
	 */
	public void loadCards(Collection<ResourceInputStream> inputStreams) {
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
		all.getSets().clear();
		for (String set : sets) {
			all.addSet(set);
		}
		CardList formats = cards.values().stream()
				.filter(card -> card.getCardType() == Spellsource.CardTypeMessage.CardType.FORMAT).collect(Collectors.toCollection(CardArrayList::new));
		this.formats.put(FORMAT_NAME_ALL, this.all);
		for (Card formatCard : formats) {
			this.formats.put(formatCard.getName(), new DeckFormat()
					.setSecondPlayerBonusCards(formatCard.getDesc().getSecondPlayerBonusCards())
					.setValidDeckCondition(formatCard.getDesc().getCondition())
					.withName(formatCard.getName())
					.withCardSets(formatCard.getCardSets()));
		}
		formatCards = formats.stream().collect(toMap(Card::getName, Function.identity()));
		// Populate the class and hero cards
		classCards = cards.values().stream().filter(c -> c.getCardType() == Spellsource.CardTypeMessage.CardType.CLASS).collect(toMap(Card::getHeroClass, Function.identity()));
		classCardsForFormat = formatCards.entrySet().stream().collect(toMap(kv -> getFormat(kv.getKey()), kv -> {
			var key = getFormat(kv.getKey());
			return classCards.values().stream().filter(key::isInFormat).collect(Collectors.toCollection(CardArrayList::new));
		}));
		var allClassCards = new CardArrayList();
		allClassCards.addAll(classCards.values());
		classCardsForFormat.put(all(), allClassCards);
		heroCards =
				classCards.values().stream().map(value -> getCardById(Objects.requireNonNull(value).getHero())).collect(toMap(Card::getHeroClass, i -> i));
		baseClassesForFormat = formatCards.entrySet().stream().collect(toMap(kv -> getFormat(kv.getKey()), kv -> {
			var key = getFormat(kv.getKey());
			return classCards.values().stream().filter(c -> c.isCollectible() && key.isInFormat(c)).map(Card::getHeroClass).collect(Collectors.toList());
		}));
		baseClassesForFormat.put(all(), allClassCards.stream().map(Card::getHeroClass).distinct().collect(Collectors.toList()));
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
