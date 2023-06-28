package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.impl.MigrationUtils;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.stream.Collectors;

public class R__0001_Update_cards extends BaseJavaMigration {

	static {
	}

	@Override
	public Integer getChecksum() {
		return MigrationUtils.cardsChecksum();
	}

	@Override
	public void migrate(Context context) throws Exception {
		var dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);
		var ownerUserId = MigrationUtils.getSpellsourceUserId();
		ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
		var publish = ClasspathCardCatalogue.INSTANCE.getRecords().values().stream().map(record -> {
			var encoded = JsonObject.mapFrom(record.getDesc());
			return dsl.select(Routines.publishGitCard(record.getId(), encoded, ownerUserId));
		}).collect(Collectors.toList());
		dsl.batch(publish).execute();
	}

}
