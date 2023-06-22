package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.impl.SqlCardCatalogue;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.vertx.await.Async;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlCardCatalogueTests extends FrameworkTestBase {

	@Test
	public void testGetFormats(Vertx vertx, VertxTestContext vertxTestContext) {
		startGateway(vertx)
				.compose(v -> {
					var catalogue = new SqlCardCatalogue();
					var async = new Async(vertx, true);
					var promise = Promise.<Void>promise();
					async.run(v2 -> {
						try {
							var formats = catalogue.formats();
							vertxTestContext.verify(() -> {
								Assertions.assertTrue(formats.size() > 0);
								Assertions.assertTrue(formats.containsKey("format_spellsource"));
							});
						} catch (Throwable t) {
							promise.fail(t);
						}

						promise.complete();
					});
					return promise.future();
				})
				.onComplete(vertxTestContext.succeedingThenComplete());
	}
}
