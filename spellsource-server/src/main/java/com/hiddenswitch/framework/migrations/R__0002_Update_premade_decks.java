package com.hiddenswitch.framework.migrations;

import com.google.common.hash.Hashing;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.spellsource.rpc.Spellsource.InventoryCollection;
import io.vertx.core.json.Json;
import io.vertx.sqlclient.Tuple;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.nio.charset.Charset;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hiddenswitch.framework.schema.spellsource.Tables.CARDS_IN_DECK;
import static com.hiddenswitch.framework.schema.spellsource.Tables.DECKS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public class R__0002_Update_premade_decks extends BaseJavaMigration {

	private static final String deckIdPrefix = "premade-";
	private static final List<DeckCreateRequest> premadeDecks = Legacy.getPremadeDecks();

	@Override
	public Integer getChecksum() {
		var checksum = Hashing.crc32().newHasher();
		premadeDecks.stream().sorted(Comparator.comparing(DeckCreateRequest::getName))
				.map(Json::encode)
				.forEach(str -> checksum.putString(str, Charset.defaultCharset()));
		return checksum.hash().asInt();
	}

	@Override
	public void migrate(Context context) throws Exception {
		if (premadeDecks.isEmpty()) {
			return;
		}

		if ((long) premadeDecks.size() != premadeDecks.stream().map(DeckCreateRequest::getName).distinct().count()) {
			throw new RuntimeException("duplicate names in premade decks: " + premadeDecks.stream().map(DeckCreateRequest::getName).collect(Collectors.groupingBy(identity(), counting())).entrySet().stream().filter(e -> e.getValue() > 1L).map(Map.Entry::getKey).collect(joining(", ")));
		}

		var ownerUserId = Environment.getSpellsourceUserId();
		var currentDecksInBuild = premadeDecks.stream().map(DeckCreateRequest::getName).map(name -> deckIdPrefix + name).collect(toList());
		// insert and update new decks
		var inserts = new ArrayList<Query>();
		var dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);

		var records = premadeDecks.stream().map(dcr -> DECKS.newRecord()
				.setName(dcr.getName())
				.setCreatedBy(ownerUserId)
				.setLastEditedBy(ownerUserId)
				.setDeckType(InventoryCollection.InventoryCollectionDeckType.CONSTRUCTED_VALUE)
				.setFormat(dcr.getFormat())
				.setHeroClass(dcr.getHeroClass())
				.setId(deckIdPrefix + dcr.getName())
				.setPermittedToDuplicate(true)
				.setIsPremade(true))
				.collect(toList());

		for (var record : records) {
			dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);
			var step = dsl.insertInto(DECKS).set(record);
			Objects.requireNonNull(step);
			var excluded = DECKS.as(DSL.unquotedName("excluded"));
			var fields = DECKS.fieldStream().filter(f -> record.changed(f) && !DECKS.ID.equals(f)).collect(toList());
			inserts.add(step.onDuplicateKeyUpdate().set(fields.stream().collect(toMap(identity(), excluded::field))));
		}

		dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);
		// Sync deck trashed status
		var update1 = dsl.update(DECKS).set(DECKS.TRASHED, true).where(DECKS.IS_PREMADE.eq(true).and(DECKS.ID.notIn(currentDecksInBuild)));
		dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);
		var update2 = dsl.update(DECKS).set(DECKS.TRASHED, false).where(DECKS.ID.in(currentDecksInBuild));
		// Sync the cards
		dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);
		var deleteExistingCardRecords = dsl.deleteFrom(CARDS_IN_DECK).where(CARDS_IN_DECK.DECK_ID.in(currentDecksInBuild));
		dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);
		var insert2 = dsl.insertInto(CARDS_IN_DECK, CARDS_IN_DECK.DECK_ID, CARDS_IN_DECK.CARD_ID);
		premadeDecks.stream()
				.flatMap(deckCreateRequest -> deckCreateRequest.getCardIds().stream().map(cardId -> Tuple.of(deckIdPrefix + deckCreateRequest.getName(), cardId)))
				.forEach(tuple -> insert2.values(tuple.getString(0), tuple.getString(1)));
		// TODO: Specify things like signature, player entity attributes in deck creation request
		var collect = Stream.concat(inserts.stream(), Stream.of(update1, update2, deleteExistingCardRecords, insert2)).collect(toList());
		DSL.using(context.getConnection(), SQLDialect.POSTGRES).batch(collect).execute();
	}
}
