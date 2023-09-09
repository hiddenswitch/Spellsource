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
		return Environment.cardsChecksum();
	}

	@Override
	public void migrate(Context context) throws Exception {
		var dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);
		var ownerUserId = Environment.getSpellsourceUserId();
		ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
		var publish = ClasspathCardCatalogue.INSTANCE.getCards().values().stream().map(card -> {
			var encoded = JsonObject.mapFrom(card.getDesc());
			return dsl.select(Routines.publishGitCard(card.getCardId(), encoded, ownerUserId));
		}).collect(Collectors.toList());
		dsl.batch(publish).execute();
		// invalidate the corresponding redis key for unity clients that need the card files
		SqlCachedCardCatalogue.invalidateGitCardsFile();
		// todo: notify running copies of the server that this has been updated?
		// i think the subscription will stream in the thousands of cards that were changed
	}
}
