package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.impl.SqlCachedCardCatalogue;
import com.hiddenswitch.framework.rpc.Hiddenswitch.*;
import com.hiddenswitch.framework.schema.spellsource.Routines;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxTestContext;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.await.Async.await;
import static org.junit.jupiter.api.Assertions.*;

public class CardTests extends FrameworkTestBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(CardTests.class);

	@Test
	public void testGitCardsFile(Vertx vertx, VertxTestContext vertxTestContext) {
		var client = new Client(vertx);
		startGateway(vertx)
				.compose(v -> client.createAndLogin())
				.compose(v -> client.unauthenticatedCards().getCards(GetCardsRequest.getDefaultInstance()))
				.compose(res -> {
					vertxTestContext.verify(() -> {
						assertNotEquals("", res.getVersion());
						assertTrue(res.getContent().getCardsCount() > 1000);
						assertFalse(res.getCachedOk());
						for (var cardRecord : res.getContent().getCardsList()) {
							assertNotEquals("", cardRecord.getEntity().getCardId());
						}
					});
					return Future.succeededFuture(res.getVersion());
				})
				.compose(version -> client.unauthenticatedCards().getCards(GetCardsRequest.newBuilder().setIfNoneMatch(version).build()))
				.onSuccess(res -> {
					vertxTestContext.verify(() -> {
						assertTrue(res.getCachedOk());
						assertEquals("", res.getVersion());
						assertFalse(res.hasContent());
					});
				})
				.eventually(v -> client.closeFut())
				.onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testCachePersonalEmpty(Vertx vertx, VertxTestContext vertxTestContext) {
		var client = new Client(vertx);
		startGateway(vertx)
				.compose(v -> client.createAndLogin())
				.compose(v -> client.cards().getCardsByUser(GetCardsRequest.newBuilder().setUserId(client.getUserEntity().getId()).build()))
				.compose(res -> {
					vertxTestContext.verify(() -> {
						assertNotEquals("", res.getVersion());
						assertEquals(0, res.getContent().getCardsCount());
						assertFalse(res.getCachedOk());
					});
					return Future.succeededFuture(res.getVersion());
				})
				// should default to my own user id
				.compose(version -> client.cards().getCardsByUser(GetCardsRequest.newBuilder().setIfNoneMatch(version).build()))
				.onSuccess(res -> {
					vertxTestContext.verify(() -> {
						assertTrue(res.getCachedOk());
						assertEquals("", res.getVersion());
						assertFalse(res.hasContent());
					});
				})
				.eventually(v -> client.closeFut())
				.onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testSaveCard(Vertx vertx, VertxTestContext vertxTestContext) {
		testVirtual(vertx, vertxTestContext, () -> {
			var cardCatalogue = new SqlCachedCardCatalogue();
			cardCatalogue.invalidateAllAndRefresh();
			await(startGateway(vertx));
			var client = new Client(vertx);
			await(client.createAndLogin());
			// put a card
			var minion = cardCatalogue.getCardById("minion_merciless_corruptor");
			var desc = JsonObject.mapFrom(minion.getDesc());
			var cardId = "xtz_test_123";
			desc.put("id", cardId);
			var jooq = Environment.jooqAkaDaoConfiguration();

			var userId = jooq.dsl().setLocal(DSL.name("user.id"), DSL.value(client.getUserEntity().getId()));
			var cardRecord = jooq.dsl().select(Routines.saveCard(cardId, new JsonObject(), desc));
			var publish = jooq.dsl().select(Routines.publishCard(cardId));
			var saveCard = userId.getSQL(ParamType.INLINED) + ";" + cardRecord.getSQL(ParamType.INLINED) + ";" + publish.getSQL(ParamType.INLINED);
			LOGGER.warn("about to save with {}", saveCard);
			var saved = await(Environment.sqlClient().query(saveCard).execute());
			LOGGER.warn("saved", saveCard);
			await(Environment.sleep(200));
			LOGGER.warn("getting cards");
			var res = await(client.cards().getCardsByUser(GetCardsRequest.newBuilder().setUserId(client.getUserEntity().getId()).build()));
			LOGGER.warn("got em");
			assertNotEquals("", res.getVersion());
			assertEquals(1, res.getContent().getCardsCount());
			assertEquals(minion.getName(), res.getContent().getCards(0).getEntity().getName());
			assertFalse(res.getCachedOk());
			LOGGER.warn("finished");
		});
	}
}
