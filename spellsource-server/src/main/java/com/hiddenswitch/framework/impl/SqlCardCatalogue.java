package com.hiddenswitch.framework.impl;

import com.google.common.collect.Iterables;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
	@Override
	public Map<String, DeckFormat> formats() {
		var formats = await(Environment.callRoutine(RowMappers.getCardsMapper(), Routines.cardCatalogueFormats()));
		return formats.stream().collect(Collectors.toMap(
				format -> format.getCardScript().getString("name"),
				format -> {
					@SuppressWarnings("unchecked")
					List<String> sets = format.getCardScript().getJsonArray("sets").getList();
					return new DeckFormat()
							.withName(format.getCardScript().getString("name"))
							.withCardSets(sets);
				}));
	}

	@Override
	public DeckFormat getFormat(String name) {
		return null;
	}

	@Override
	public Set<String> getBannedDraftCards() {
		return null;
	}

	@Override
	public Set<String> getHardRemovalCardIds() {
		return null;
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
	public Card getCardById(@NotNull String id) {
		return null;
	}

	@Override
	public @NotNull Map<String, CardCatalogueRecord> getRecords() {
		throw new UnsupportedOperationException("getRecords");
	}

	@Override
	public @Nullable Card getCardByName(String name) {
		throw new UnsupportedOperationException("getCardByName");
	}

	@Override
	public Card getCardByName(String name, String heroClass) {
		return null;
	}

	@Override
	public @NotNull CardList query(DeckFormat deckFormat, Spellsource.CardTypeMessage.CardType cardType, Spellsource.RarityMessage.Rarity rarity, String heroClass, Attribute tag, boolean clone) {
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
}
