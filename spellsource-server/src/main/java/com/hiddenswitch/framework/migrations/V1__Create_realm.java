package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Accounts;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class V1__Create_realm extends BaseJavaMigration {
	@Override
	public Integer getChecksum() {
		return 1;
	}

	@Override
	public void migrate(Context context) throws Exception {
		// First ensure the schema exists
		var dsl = DSL.using(context.getConnection(), SQLDialect.POSTGRES);
		dsl.createSchemaIfNotExists("hiddenswitch").execute();

		var realm = Accounts.createRealmIfAbsent().toCompletionStage().toCompletableFuture().join();
	}
}
