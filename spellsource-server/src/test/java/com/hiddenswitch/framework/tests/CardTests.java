package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.rpc.Hiddenswitch.*;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardTests extends FrameworkTestBase {
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

//	@Test
//	public void testPersonalCardsInvalidatedCorrectly(Vertx vertx, VertxTestContext vertxTestContext) {
//		// verify empty
//		var client = new Client(vertx);
//		startGateway(vertx)
//				.compose(v -> client.createAndLogin())
//				.compose(v -> client.cards().getCardsByUser(GetCardsRequest.newBuilder().setUserId(client.getUserEntity().getId()).build()))
//				.compose(res -> {
//					vertxTestContext.verify(() -> {
//						assertNotEquals("", res.getVersion());
//						assertEquals(0, res.getContent().getCardsCount());
//						assertFalse(res.getCachedOk());
//					});
//					return Future.succeededFuture(res.getVersion());
//				});
//	}
}
