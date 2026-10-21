package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Environment;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import static com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS;

/**
 * Run this migration twice - once now, so that the legacy server migration executes correctly, and again in a repeating
 * migration to update all the cards on update.
 */
public class V7__Create_cards extends BaseJavaMigration {

	static {
	}

	@Override
	public Integer getChecksum() {
		return 0;
	}

	@Override
	public void migrate(Context context) throws Exception {
		// No-op: git-authored cards are loaded directly in-memory from ClasspathCardCatalogue.INSTANCE
	}
}
