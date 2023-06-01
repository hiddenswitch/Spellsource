package com.hiddenswitch.framework.impl;

import com.google.common.hash.Hashing;
import com.hiddenswitch.framework.Accounts;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import org.flywaydb.core.api.migration.Context;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

import static com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS;

public class MigrationUtils {
	public static String getSpellsourceUserId() {
		var realm = Accounts.get().toCompletionStage().toCompletableFuture().join();
		// todo: is this an exact match? could be bad
		var ownerUserId = realm.users().search("Spellsource").stream().findFirst().get().getId();
		return ownerUserId;
	}

	public static int cardsChecksum() {
		var checksum = Hashing.crc32().newHasher();
        ClasspathCardCatalogue.INSTANCE.getRecords().values().stream()
				.sorted(Comparator.comparing(CardCatalogueRecord::getId))
				.map(CardCatalogueRecord::getDesc)
				.map(Json::encode)
				.forEach(str -> checksum.putString(str, Charset.defaultCharset()));
		return checksum.hash().asInt();
	}

	public static void cardsMigration(Context context) {
		var dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);
		var ownerUserId = getSpellsourceUserId();
        var insertAndUpdate = ClasspathCardCatalogue.INSTANCE.getRecords().values().stream().map(record -> {
			var now = OffsetDateTime.now();
			var encoded = JsonObject.mapFrom(record.getDesc());
			return dsl.insertInto(CARDS, CARDS.ID, CARDS.CREATED_BY, CARDS.CREATED_AT, CARDS.LAST_MODIFIED, CARDS.CARD_SCRIPT)
					.values(record.getId(), ownerUserId, now, now, encoded)
					.onDuplicateKeyUpdate()
					.set(CARDS.LAST_MODIFIED, now)
					.set(CARDS.CARD_SCRIPT, encoded);
		}).collect(Collectors.toList());
		dsl.batch(insertAndUpdate).execute();
	}
}
