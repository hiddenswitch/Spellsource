package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.hiddenswitch.framework.tests.DecksTests.createRandomDeck;

public class RateLimiterTests extends FrameworkTestBase {

	@Test
	@Disabled
	public void testCreateManyDecks(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		startGateway(vertx)
				.compose(v -> client.createAndLogin())
				.compose(ignored -> CompositeFuture.all(IntStream.range(0, 100).mapToObj(i -> createRandomDeck(client)).collect(Collectors.toList())))
				.recover(t -> {
					testContext.verify(() -> {
						var ex = (StatusRuntimeException) t;
						Assertions.assertEquals(Status.UNAVAILABLE.getCode(), ex.getStatus().getCode());
					});
					return Future.succeededFuture();
				})
				.onComplete(client::close)
				.onComplete(testContext.succeedingThenComplete());
	}
}
