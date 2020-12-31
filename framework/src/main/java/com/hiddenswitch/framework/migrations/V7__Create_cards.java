package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.impl.MigrationUtils;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Run this migration twice - once now, so that the legacy server migration executes correctly, and again in a repeating
 * migration to update all the cards on update.
 */
public class V7__Create_cards extends BaseJavaMigration {

	static {
		CardCatalogue.loadCardsFromPackage();
	}

	@Override
	public Integer getChecksum() {
		return MigrationUtils.cardsChecksum();
	}

	@Override
	public void migrate(Context context) throws Exception {
		MigrationUtils.cardsMigration(context);
	}

}
