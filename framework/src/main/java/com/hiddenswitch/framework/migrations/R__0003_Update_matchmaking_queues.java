package com.hiddenswitch.framework.migrations;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.schema.spellsource.tables.records.MatchmakingQueuesRecord;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.Arrays;

import static com.hiddenswitch.framework.schema.spellsource.Tables.MATCHMAKING_QUEUES;

public class R__0003_Update_matchmaking_queues extends BaseJavaMigration {
	private MatchmakingQueuesRecord[] records() {
		var quickPlay = MATCHMAKING_QUEUES.newRecord()
				.setName("Quick Play")
				.setId("quickPlay")
				.setAutomaticallyClose(false)
				.setLobbySize(1)
				.setOnce(false)
				.setBotOpponent(true)
				.setPrivateLobby(false)
				.setAwaitingLobbyTimeout(0L)
				.setEmptyLobbyTimeout(0L)
				.setStillConnectedTimeout(4000L)
				.setStartsAutomatically(true);
		var constructed = MATCHMAKING_QUEUES.newRecord()
				.setName("Constructed")
				.setId("constructed")
				.setAutomaticallyClose(false)
				.setLobbySize(2)
				.setOnce(false)
				.setBotOpponent(false)
				.setPrivateLobby(false)
				.setAwaitingLobbyTimeout(0L)
				.setEmptyLobbyTimeout(0L)
				.setStillConnectedTimeout(1000L)
				.setStartsAutomatically(true);

		return new MatchmakingQueuesRecord[]{quickPlay, constructed};
	}

	@Override
	public Integer getChecksum() {
		return Arrays.hashCode(records());
	}

	@Override
	public void migrate(Context context) throws Exception {
		var inserts = Arrays.stream(records()).map(record -> Environment.upsert(dsl(context), record)).toArray(Insert[]::new);
		dsl(context).batch(inserts).execute();
	}

	private DSLContext dsl(Context context) {
		return DSL.using(context.getConnection(), SQLDialect.POSTGRES);
	}
}
