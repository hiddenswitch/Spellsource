package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.impl.SqlCachedCardCatalogue;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.CardsDao;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.junit.jupiter.api.Test;

import static com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS;
import static io.vertx.await.Async.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SqlCardCatalogueTests extends FrameworkTestBase {

	private void testCachedCardCatalogue(Vertx vertx, VertxTestContext vertxTestContext, Handler<CardCatalogue> handler) {
		var verticle = new AbstractVirtualThreadVerticle() {
			@Override
			public void startVirtual() throws Exception {
				var catalogue = new SqlCachedCardCatalogue();
				catalogue.invalidateAllAndRefresh();
				await(catalogue.subscribe());
				vertxTestContext.verify(() -> handler.handle(catalogue));
			}
		};
		vertx.deployVerticle(verticle)
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
			var beforeChangeCardJson = await(cardsDao.findOneByCondition(CARDS.ID.eq(cardIdTested).and(CARDS.IS_PUBLISHED.eq(true)).and(CARDS.IS_ARCHIVED.eq(false))));
			beforeChangeCardJson.getCardScript().getJsonObject("attributes").put("RESERVED_BOOLEAN_1", true);
			await(Environment.withDslContext(dsl -> dsl.update(CARDS)
					.set(CARDS.CARD_SCRIPT, beforeChangeCardJson.getCardScript())
					.where(CARDS.ID.eq(beforeChangeCard.getCardId()))));
			var afterChangeCard = catalogue.getCardById(cardIdTested);
			assertTrue(afterChangeCard.getAttributes().containsKey(Attribute.RESERVED_BOOLEAN_1));
		});
	}

	@Test
	public void testTwoSubscriptionsProcessCorrectly(Vertx vertx, VertxTestContext vertxTestContext) {
		testCachedCardCatalogue(vertx, vertxTestContext, catalogue -> {
			var cardIdTested = "minion_merciless_corruptor";
			var beforeChangeCard = catalogue.getCardById(cardIdTested);
			assertFalse(beforeChangeCard.getAttributes().containsKey(Attribute.RESERVED_BOOLEAN_1));
			var catalogue2 = new SqlCachedCardCatalogue();
			await(catalogue2.subscribe());
			var cardsDao = new CardsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
			var beforeChangeCardJson = await(cardsDao.findOneByCondition(CARDS.ID.eq(cardIdTested).and(CARDS.IS_PUBLISHED.eq(true)).and(CARDS.IS_ARCHIVED.eq(false))));
			beforeChangeCardJson.getCardScript().getJsonObject("attributes").put("RESERVED_BOOLEAN_1", true);
			await(Environment.withDslContext(dsl -> dsl.update(CARDS).set(CARDS.CARD_SCRIPT, beforeChangeCardJson.getCardScript()).where(CARDS.ID.eq(beforeChangeCard.getCardId()))));

			// todo: it still has to wait for the notification, because the update will complete before notifications are sent
			// you can't apparently wait for all subscriptions to be notified
			await(Environment.sleep(100));
			var afterChangeCard = catalogue2.getCardById(cardIdTested);
			assertTrue(afterChangeCard.getAttributes().containsKey(Attribute.RESERVED_BOOLEAN_1));
		});
	}
}
