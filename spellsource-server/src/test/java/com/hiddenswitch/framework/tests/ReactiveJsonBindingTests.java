package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.Tuple;
import org.jooq.JSON;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.vertx.await.Async.await;
import static org.junit.jupiter.api.Assertions.*;

public class ReactiveJsonBindingTests extends FrameworkTestBase {
	@ParameterizedTest
	@ValueSource(strings = {"{\"nested\":[true,null,42]}", "[1,\"two\",null]", "\"text\"", "42", "true", "null"})
	public void testJsonBindingsRoundTrip(String json, Vertx vertx, VertxTestContext context) {
		testVirtual(vertx, context, () -> {
			var row = await(Environment.query(dsl -> dsl.select(
					DSL.val(JSON.valueOf(json), SQLDataType.JSON),
					DSL.val(JSONB.valueOf(json), SQLDataType.JSONB),
					DSL.val((JSONB) null, SQLDataType.JSONB)))).iterator().next();
			var expected = Json.decodeValue(json);
			for (var column = 0; column < 2; column++) {
				if (expected == null) {
					assertSame(Tuple.JSON_NULL, row.getValue(column), "JSON null must remain distinct from SQL NULL");
				} else {
					assertEquals(expected, row.getValue(column));
				}
			}
			assertNull(row.getValue(2));
		});
	}
}
