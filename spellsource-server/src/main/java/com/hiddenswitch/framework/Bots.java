package com.hiddenswitch.framework;

import com.hiddenswitch.framework.schema.keycloak.tables.mappers.RowMappers;
import com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity;
import io.vertx.core.Future;

import static com.hiddenswitch.framework.schema.keycloak.Tables.USER_ENTITY;
import static com.hiddenswitch.framework.schema.spellsource.Tables.BOT_USERS;
import static org.jooq.impl.DSL.asterisk;

public class Bots {
	public static Future<UserEntity> bot() {
		var executor = Environment.queryExecutor();
		return executor
				.findOneRow(dsl -> dsl.select(asterisk()).from(BOT_USERS).join(USER_ENTITY).onKey().limit(1))
				.map(RowMappers.getUserEntityMapper());
	}
}
