package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.impl.SqlCachedCardCatalogue;
import com.hiddenswitch.framework.impl.SqlCardCatalogue;
import com.hiddenswitch.framework.schema.spellsource.routines.GetLatestCard;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.CardsDao;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;

import static io.vertx.await.Async.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlCardCatalogueTests extends FrameworkTestBase {

	private void testSqlCatalogue(Vertx vertx, VertxTestContext vertxTestContext, Handler<CardCatalogue> handler) {
		startGateway(vertx)
				.compose(v -> {
					var verticle = new AbstractVirtualThreadVerticle() {
						@Override
						public void startVirtual() throws Exception {
							var catalogue = new SqlCardCatalogue();
							handler.handle(catalogue);
						}
					};
					return vertx.deployVerticle(verticle);
				})
				.onComplete(vertxTestContext.succeedingThenComplete());
	}

	private void testCachedCardCatalogue(Vertx vertx, VertxTestContext vertxTestContext, Handler<CardCatalogue> handler) {
		startGateway(vertx)
				.compose(v -> {
					var verticle = new AbstractVirtualThreadVerticle() {
						@Override
						public void startVirtual() throws Exception {
							var catalogue = new SqlCachedCardCatalogue();
							catalogue.invalidateAllAndRefresh();
							await(catalogue.subscribe());
							vertxTestContext.verify(() -> handler.handle(catalogue));
						}
					};
					return vertx.deployVerticle(verticle);
				})
				.onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testGetFormats(Vertx vertx, VertxTestContext vertxTestContext) {
		testCachedCardCatalogue(vertx, vertxTestContext, catalogue -> {
			var formats = catalogue.formats();
			assertTrue(formats.size() > 0);
			assertTrue(formats.containsKey("Spellsource"));
		});
	}

	@Test
	public void testInvalidatesOnChange(Vertx vertx, VertxTestContext vertxTestContext) {
		testCachedCardCatalogue(vertx, vertxTestContext, catalogue -> {
			assertTrue(Thread.currentThread().isVirtual());
			var cardIdTested = "minion_abholos";
			var beforeChangeCard = catalogue.getCardById(cardIdTested);
			assertFalse(beforeChangeCard.getAttributes().containsKey(Attribute.RESERVED_BOOLEAN_1));
			var cardsDao = new CardsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
			var beforeChangeCardJson = await(cardsDao.findOneById(cardIdTested));
			beforeChangeCardJson.getCardScript().getJsonObject("attributes").put("RESERVED_BOOLEAN_1", true);
			await(cardsDao.update(beforeChangeCardJson));
			var afterChangeCard = catalogue.getCardById(cardIdTested);
			assertTrue(afterChangeCard.getAttributes().containsKey(Attribute.RESERVED_BOOLEAN_1));
		});
	}
}
