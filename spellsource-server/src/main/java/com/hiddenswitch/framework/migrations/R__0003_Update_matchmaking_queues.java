package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Matchmaking;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.Arrays;

public class R__0003_Update_matchmaking_queues extends BaseJavaMigration {

	@Override
	public Integer getChecksum() {
		return Arrays.hashCode(Matchmaking.defaultQueues());
	}

	@Override
	public void migrate(Context context) throws Exception {
		var inserts = Arrays.stream(Matchmaking.defaultQueues()).map(record -> Environment.upsert(dsl(context), record)).toArray(Insert[]::new);
		dsl(context).batch(inserts).execute();
	}

	private DSLContext dsl(Context context) {
		return DSL.using(context.getConnection(), SQLDialect.POSTGRES);
	}
}
