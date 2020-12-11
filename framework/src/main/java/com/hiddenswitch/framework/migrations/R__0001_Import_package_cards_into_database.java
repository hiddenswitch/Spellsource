package com.hiddenswitch.framework.migrations;

import com.google.common.hash.Hashing;
import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.impl.MigrationUtils;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardCatalogueRecord;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.JSONB;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.stream.Collectors;

import static com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS;

public class R__0001_Import_package_cards_into_database extends BaseJavaMigration {

	static {
		CardCatalogue.loadCardsFromPackage();
	}

	@Override
	public Integer getChecksum() {
		var checksum = Hashing.crc32().newHasher();
		CardCatalogue.getRecords().values().stream()
				.sorted(Comparator.comparing(CardCatalogueRecord::getId))
				.map(CardCatalogueRecord::getDesc)
				.map(Json::encode)
				.forEach(str -> checksum.putString(str, Charset.defaultCharset()));
		return checksum.hash().asInt();
	}

	@Override
	public void migrate(Context context) throws Exception {
		var dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);
		var ownerUserId = MigrationUtils.getSpellsourceUserId();
		var insertAndUpdate = CardCatalogue.getRecords().values().stream().map(record -> {
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
