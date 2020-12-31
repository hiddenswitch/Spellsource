package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.schema.spellsource.Tables;
import io.vertx.core.Future;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.impl.DSL;

import java.util.UUID;

public class V6__Create_bot_user_accounts extends BaseJavaMigration {

	@Override
	public Integer getChecksum() {
		return 1;
	}

	@Override
	public void migrate(Context context) throws Exception {
		Accounts.createUser("botcharles@hiddenswitch.com", "Botcharles", UUID.randomUUID().toString())
				.compose(userEntity -> Accounts.disableUser(userEntity.getId()).map(userEntity.getId()))
				.compose(userId -> Future.fromCompletionStage(DSL.using(context.getConnection()).insertInto(Tables.BOT_USERS).set(Tables.BOT_USERS.ID, userId).executeAsync()))
				.toCompletionStage().toCompletableFuture().join();
	}
}
