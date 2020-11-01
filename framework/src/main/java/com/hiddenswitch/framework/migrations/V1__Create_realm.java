package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Accounts;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V1__Create_realm extends BaseJavaMigration {
	@Override
	public Integer getChecksum() {
		return 1;
	}

	@Override
	public void migrate(Context context) throws Exception {
		var realm = Accounts.createRealmIfAbsent().toCompletionStage().toCompletableFuture().join();
	}
}
