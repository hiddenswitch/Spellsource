package com.hiddenswitch.proto3.net;

import com.hiddenswitch.proto3.net.impl.ServerImpl;
import com.hiddenswitch.proto3.net.util.GsonMessageBodyHandler;
import io.github.robwin.swagger.test.SwaggerAssert;
import io.github.robwin.swagger.test.SwaggerAssertions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Created by bberman on 1/23/17.
 */
@RunWith(VertxUnitRunner.class)
@Ignore
public class APITest {
	private static Vertx vertx;
	private static String contractPath;
	private ResteasyClient client;

	static {
		contractPath = SwaggerAssert.class.getResource("/server.yaml").getPath();
	}

	public APITest() {
		GsonMessageBodyHandler gsonProvider = new GsonMessageBodyHandler();
		client = new ResteasyClientBuilder().register(gsonProvider).build();
	}

	@BeforeClass
	public static void setUp(TestContext context) throws IOException {
		System.setProperty("java.net.preferIPv4Stack", "true");
		vertx = Vertx.vertx();
		vertx.deployVerticle(new ServerImpl(), context.asyncAssertSuccess());
	}

	@Before
	public void createTarget() {
//		checking = api.createWebTarget(client.target("http://localhost:8080"));
	}

	@Test
	public void testMatchmakingEndpoint() {
		SwaggerAssertions.assertThat("http://localhost:8080/v1").satisfiesContract(contractPath);
//		MatchmakingQueuePut request = new MatchmakingQueuePut()
//				.withDeck(DeckFactory.getRandomDeck());
//		checking.path("/matchmaking/constructed/queue")
//				.request()
//				.header("X-Auth-UserId", "testUserId")
//				.put(Entity.json(request));
//		Assert.assertThat(checking.getLastReport(), RamlMatchers.hasNoViolations());
	}
}
