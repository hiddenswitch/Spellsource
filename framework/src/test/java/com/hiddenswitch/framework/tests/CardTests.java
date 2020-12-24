package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.rpc.GetCardsRequest;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardTests extends FrameworkTestBase {
	@Test
	public void testCards1(Vertx vertx, VertxTestContext vertxTestContext) {
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
}
