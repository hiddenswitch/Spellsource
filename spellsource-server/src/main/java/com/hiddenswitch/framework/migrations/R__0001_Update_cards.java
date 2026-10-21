package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.impl.SqlCachedCardCatalogue;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.stream.Collectors;

/**
 * Runs only when the cards in SQL need to be updated with the cards in Git by checksumming the cards in git. Also
 * invalidates the redis key that the server uses for the precomputed cards file.
 */
public class R__0001_Update_cards extends BaseJavaMigration {

	// todo: why do we have an empty static constructor here?
	static {
	}

	@Override
	public Integer getChecksum() {
		return 0;
	}

	@Override
	public void migrate(Context context) throws Exception {
		// No-op: git-authored cards are loaded directly in-memory from ClasspathCardCatalogue.INSTANCE.
		// Invalidate the Redis cache key so clients receive updated card definitions when git cards change.
		SqlCachedCardCatalogue.invalidateGitCardsFile();
	}
}
