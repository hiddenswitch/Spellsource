package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.impl.MigrationUtils;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class R__0001_Update_cards extends BaseJavaMigration {

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
