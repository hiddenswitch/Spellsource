package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.impl.ModelConversions;
import com.hiddenswitch.framework.impl.SqlCachedCardCatalogue;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.CardsDao;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.Cards;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static com.hiddenswitch.framework.schema.spellsource.tables.Cards.CARDS;
import static io.vertx.await.Async.await;
import static org.junit.jupiter.api.Assertions.*;

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
		vertx.deployVerticle(verticle, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD))
				.onComplete(vertxTestContext.succeedingThenComplete());
	}

	private Cards ensureCardInSql(CardsDao cardsDao, net.demilich.metastone.game.cards.Card card) {
		var existing = await(cardsDao.findOneByCondition(CARDS.ID.eq(card.getCardId()).and(CARDS.IS_PUBLISHED.eq(true)).and(CARDS.IS_ARCHIVED.eq(false))));
		if (existing != null) {
			return existing;
		}
		var descJson = JsonObject.mapFrom(card.getDesc());
		await(Environment.withDslContext(dsl -> dsl.insertInto(CARDS)
				.set(CARDS.ID, card.getCardId())
				.set(CARDS.CREATED_BY, Environment.getSpellsourceUserId())
				.set(CARDS.IS_PUBLISHED, true)
				.set(CARDS.IS_ARCHIVED, false)
				.set(CARDS.CARD_SCRIPT, descJson)));
		return await(cardsDao.findOneByCondition(CARDS.ID.eq(card.getCardId()).and(CARDS.IS_PUBLISHED.eq(true)).and(CARDS.IS_ARCHIVED.eq(false))));
	}

	@Test
	public void testGetFormats(Vertx vertx, VertxTestContext vertxTestContext) {
		testCachedCardCatalogue(vertx, vertxTestContext, catalogue -> {
			var formats = catalogue.formats();
			assertFalse(formats.isEmpty());
			assertTrue(formats.containsKey("Spellsource"));
		});
	}

	@ParameterizedTest
	@ValueSource(strings = {"hero_neutral"})
	public void testGetCards(String cardId, Vertx vertx, VertxTestContext vertxTestContext) {
		testCachedCardCatalogue(vertx, vertxTestContext, catalogue -> assertNotNull(catalogue.getCardById(cardId)));
	}

	@Test
	public void testInvalidatesOnChange(Vertx vertx, VertxTestContext vertxTestContext) {
		testCachedCardCatalogue(vertx, vertxTestContext, catalogue -> {
			assertTrue(Thread.currentThread().isVirtual());
			var cardIdTested = "minion_abholos";
			var beforeChangeCard = catalogue.getCardById(cardIdTested);
			assertFalse(beforeChangeCard.getAttributes().containsKey(Attribute.RESERVED_BOOLEAN_1));
			var cardsDao = new CardsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
			var beforeChangeCardJson = ensureCardInSql(cardsDao, beforeChangeCard);
			beforeChangeCardJson.getCardScript().getJsonObject("attributes").put("RESERVED_BOOLEAN_1", true);
			await(Environment.withDslContext(dsl -> dsl.update(CARDS)
					.set(CARDS.CARD_SCRIPT, beforeChangeCardJson.getCardScript())
					.where(CARDS.ID.eq(beforeChangeCard.getCardId()))));
			await(Environment.sleep(100));
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
			catalogue2.invalidateAllAndRefresh();
			await(catalogue2.subscribe());
			var cardsDao = new CardsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
			var beforeChangeCardJson = ensureCardInSql(cardsDao, beforeChangeCard);
			beforeChangeCardJson.getCardScript().getJsonObject("attributes").put("RESERVED_BOOLEAN_1", true);
			await(Environment.withDslContext(dsl -> dsl.update(CARDS).set(CARDS.CARD_SCRIPT, beforeChangeCardJson.getCardScript()).where(CARDS.ID.eq(beforeChangeCard.getCardId()))));

			// todo: it still has to wait for the notification, because the update will complete before notifications are sent
			// you can't apparently wait for all subscriptions to be notified
			await(Environment.sleep(100));
			var afterChangeCard = catalogue2.getCardById(cardIdTested);
			assertTrue(afterChangeCard.getAttributes().containsKey(Attribute.RESERVED_BOOLEAN_1));
		});
	}

	@Test
	public void testArtRouteAndModelConversion(Vertx vertx, VertxTestContext vertxTestContext) {
		testCachedCardCatalogue(vertx, vertxTestContext, catalogue -> {
			var card = catalogue.getCardById("hero_neutral");
			assertNotNull(card);

			// Without registered custom art, Sprite.named falls back to card.getName()
			var defaultArt = ModelConversions.toArt(card);
			assertEquals(card.getName(), defaultArt.getSprite().getNamed());

			// Register content-addressed PNG bytes for this card name
			byte[] fakePng = "\u0089PNG\r\n\u001a\nspellsource-test-art".getBytes(StandardCharsets.UTF_8);
			String hash = Environment.registerArt(card.getName(), fakePng);
			assertNotNull(hash);
			assertEquals(64, hash.length());

			// ModelConversions.toArt(card) now resolves to /art/<hash>.png
			var hashedArt = ModelConversions.toArt(card);
			assertEquals("/art/" + hash + ".png", hashedArt.getSprite().getNamed());

			// Spin up an HTTP server with registerArtRoutes and verify 200 OK + ETag + 304 Not Modified
			var router = Router.router(vertx);
			Environment.registerArtRoutes(router);
			var server = await(vertx.createHttpServer().requestHandler(router).listen(0));
			try {
				var webClient = WebClient.create(vertx);
				var res200 = await(webClient.get(server.actualPort(), "127.0.0.1", "/art/" + hash + ".png").send());
				assertEquals(200, res200.statusCode());
				assertEquals("\"" + hash + "\"", res200.getHeader("ETag"));
				assertEquals("public, max-age=31536000, immutable", res200.getHeader("Cache-Control"));
				assertArrayEquals(fakePng, res200.body().getBytes());

				var res304 = await(webClient.get(server.actualPort(), "127.0.0.1", "/art/" + hash + ".png")
						.putHeader("If-None-Match", "\"" + hash + "\"")
						.send());
				assertEquals(304, res304.statusCode());

				var res404 = await(webClient.get(server.actualPort(), "127.0.0.1", "/art/0000000000000000000000000000000000000000000000000000000000000000.png").send());
				assertEquals(404, res404.statusCode());
			} finally {
				await(server.close());
			}
		});
	}
}
