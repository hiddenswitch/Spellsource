package com.hiddenswitch.framework.impl;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.migrations.V8__Migrate_from_previous_server;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.CardsDao;
import com.hiddenswitch.spellsource.rpc.Spellsource;
import io.vertx.await.Async;
import io.vertx.sqlclient.SqlConnection;
import net.demilich.metastone.game.cards.*;
import net.demilich.metastone.game.decks.DeckFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.JSONB;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.hiddenswitch.framework.schema.spellsource.Tables.CARDS;
import static io.vertx.await.Async.await;
import static org.jooq.impl.DSL.field;

public class SqlCardCatalogue implements CardCatalogue {
	@Override
	public Map<String, DeckFormat> formats() {
//		SqlConnection connection = null;
//		try {
//			connection = await(Environment.pgPoolAkaDaoDelegate().getConnection());
//			var dao = new CardsDao(Environment.jooqAkaDaoConfiguration(), connection);
//
//			var formats = await(dao.findManyByCondition(field("card_script ->> 'type'").eq(Spellsource.CardTypeMessage.CardType.FORMAT.name())));
//
//		} finally {
//			connection.close();
//		}
		return null;
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
		return null;
	}

	@Override
	public @NotNull Map<String, Card> getCards() {
		return null;
	}

	@Override
	public Card getCardById(@NotNull String id) {
		return null;
	}

	@Override
	public @NotNull Map<String, CardCatalogueRecord> getRecords() {
		return null;
	}

	@Override
	public @Nullable Card getCardByName(String name) {
		return null;
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
