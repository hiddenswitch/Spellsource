package com.hiddenswitch.framework.impl;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.BannedDraftCards;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.Cards;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.HardRemovalCards;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.decks.DeckFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vertx.await.Async.await;
import static org.jooq.impl.DSL.field;

/**
 * Retrieves card catalogue data from the SQL server.
 * <p>
 * For now, does not use caching.
 */
public class SqlCardCatalogue implements CardCatalogue {
	Cache<String, DeckFormat> cachedFormats = CacheBuilder.newBuilder().initialCapacity(2).build();

	Cache<String, String> cachedBannedCards = CacheBuilder.newBuilder().initialCapacity(60).expireAfterWrite(Duration.ofMinutes(30)).build();

	Cache<String, String> hardRemovalCards = CacheBuilder.newBuilder().initialCapacity(60).expireAfterWrite(Duration.ofMinutes(30)).build();

	LoadingCache<String, Card> cards = CacheBuilder.newBuilder().initialCapacity(8196).refreshAfterWrite(Duration.ofHours(24)).build(new CacheLoader<>() {
		@Override
		public @NotNull Card load(@NotNull String key) {
			var cards = await(Environment.callRoutine(RowMappers.getCardsMapper(), Routines.cardCatalogueGetCardById(key)));
			if (cards.size() == 0) {
				throw new NullPointerException(key);
			}
			var card = cards.get(0);
			return new Card(card.getCardScript().mapTo(CardDesc.class));
		}
	});

	private static DeckFormat toDeckFormat(Cards format) {
		@SuppressWarnings("unchecked") List<String> sets = format.getCardScript().getJsonArray("sets").getList();
		return new DeckFormat().withName(format.getCardScript().getString("name")).withCardSets(sets);
	}

	@Override
	public Map<String, DeckFormat> formats() {
		if (cachedFormats.size() == 0) {
			var formats = await(Environment.callRoutine(RowMappers.getCardsMapper(), Routines.cardCatalogueFormats()));
			var map = formats.stream().collect(Collectors.toMap(format -> new CardScript(format).name(), SqlCardCatalogue::toDeckFormat));
			cachedFormats.putAll(map);
		}
		return cachedFormats.asMap();
	}

	@Override
	public DeckFormat getFormat(String name) {
		try {
			return cachedFormats.get(name, () -> {
				var format = await(Environment.callRoutine(RowMappers.getCardsMapper(), Routines.cardCatalogueGetFormat(name)));
				if (format.size() == 0) {
					return null;
				}
				return toDeckFormat(format.get(0));
			});
		} catch (ExecutionException e) {
			throw (RuntimeException) Throwables.getRootCause(e);
		}
	}

	/**
	 * todo: should be changed to query if a specific card is banned in draft
	 *
	 * @return
	 */
	@Override
	public Set<String> getBannedDraftCards() {
		if (cachedBannedCards.size() == 0) {
			var bannedCards = await(Environment.callRoutine(RowMappers.getBannedDraftCardsMapper(), Routines.cardCatalogueGetBannedDraftCards())).stream().collect(Collectors.toMap(BannedDraftCards::getCardId, BannedDraftCards::getCardId));

			cachedBannedCards.putAll(bannedCards);
		}
		return cachedBannedCards.asMap().keySet();
	}

	/**
	 * todo: should be changed to query if a specific card is hard removal
	 *
	 * @return
	 */
	@Override
	public Set<String> getHardRemovalCardIds() {
		if (hardRemovalCards.size() == 0) {
			var hardRemovals = await(Environment.callRoutine(RowMappers.getHardRemovalCardsMapper(), Routines.cardCatalogueGetHardRemovalCards())).stream().collect(Collectors.toMap(HardRemovalCards::getCardId, HardRemovalCards::getCardId));

			hardRemovalCards.putAll(hardRemovals);
		}
		return hardRemovalCards.asMap().keySet();
	}

	@Override
	public @NotNull CardList getAll() {
		throw new UnsupportedOperationException("getAll");
	}

	@Override
	public @NotNull Map<String, Card> getCards() {
		throw new UnsupportedOperationException("getCards");
	}

	@Override
	public @NotNull Card getCardById(@NotNull String id) {
		try {
			return cards.get(id).getCopy();
		} catch (ExecutionException e) {
			var underlying = Throwables.getRootCause(e);
			if (underlying instanceof RuntimeException runtimeException) {
				throw runtimeException;
			} else {
				throw new RuntimeException(underlying);
			}
		}
	}

	@Override
	public @NotNull Map<String, CardCatalogueRecord> getRecords() {
		throw new UnsupportedOperationException("getRecords");
	}

	@Override
	public @Nullable Card getCardByName(String name) {
		throw new UnsupportedOperationException("getCardByName(name)");
	}

	@Override
	public Card getCardByName(String name, String heroClass) {
		throw new UnsupportedOperationException("getCardByName(name, heroClass)");
	}

	public record QueryArgs(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType,
	                        Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag) {
	}

	@Override
	public @NotNull CardList query(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag, boolean clone) {
		return null;
	}

	private CardList query(QueryArgs args) {
		return null;
	}

	@Override
	public Card getFormatCard(String name) {
		return null;
	}

	@Override
	public Card getHeroCard(String heroClass) {
		return null;
	}

	@Override
	public CardList getClassCards(DeckFormat format) {
		return null;
	}

	@Override
	public List<String> getBaseClasses(DeckFormat deckFormat) {
		return null;
	}

	@Override
	public Stream<Card> stream() {
		return null;
	}

	@Override
	public CardList queryClassCards(DeckFormat format, String hero, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		return null;
	}

	@Override
	public CardList queryNeutrals(DeckFormat format, Set<String> bannedCards, Spellsource.RarityMessage.Rarity rarity, Set<Spellsource.CardTypeMessage.CardType> validCardTypes) {
		return null;
	}

	@Override
	public CardList queryUncollectible(DeckFormat deckFormat) {
		return null;
	}

	record CardScript(Cards card) {
		String name() {
			return card.getCardScript().getString("name");
		}
	}
}
