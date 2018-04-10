package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.Suspendable;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.impl.AccountsImpl;
import com.hiddenswitch.spellsource.impl.SpellsourceAuthHandler;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.util.Mongo;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class RealtimeTest {
	@Test
	public void testConversationRealtime(TestContext context) {
		// Test clustered
		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance(Cluster.getConfig(5701));

		final Async async = context.async();
		final Vertx[] vertices = new Vertx[1];
		Vertx.clusteredVertx(new VertxOptions().setClusterManager(new HazelcastClusterManager(hazelcastInstance)), context.asyncAssertSuccess(vertx -> {
			vertices[0] = vertx;
			Mongo.mongo().connectWithEnvironment(vertx);

			Future<String> f1 = Future.future();
			Future<String> f2 = Future.future();
			final AccountsImpl accounts = new AccountsImpl();
			CreateAccountResponse[] user1 = new CreateAccountResponse[1];
			CreateAccountResponse[] user2 = new CreateAccountResponse[1];
			vertx.exceptionHandler(context.exceptionHandler());
			vertx.deployVerticle(accounts, f1);
			vertx.deployVerticle(new SyncVerticle() {
				@Override
				@Suspendable
				public void start() throws Exception {
					Router route = Router.router(vertx);
					AuthHandler authHandler = SpellsourceAuthHandler.create();

					route.route("/realtime")
							.method(HttpMethod.GET)
							.handler(authHandler);

					route.route("/realtime")
							.method(HttpMethod.GET)
							.handler(Realtime.create());

					Conversations.realtime();

					user1[0] = accounts.createAccount(RandomStringUtils.randomAlphanumeric(64) + "@gmail.com", "pass1234", RandomStringUtils.randomAlphanumeric(64));
					user2[0] = accounts.createAccount(RandomStringUtils.randomAlphanumeric(64) + "@gmail.com", "pass1234", RandomStringUtils.randomAlphanumeric(64));

					getVertx().createHttpServer().requestHandler(route::accept).listen(8080);
				}
			}, f2);

			CompositeFuture.join(f1, f2).setHandler(context.asyncAssertSuccess(then2 -> {

				// Deployed. Subscribe to conversation
				// User 1 client
				final HttpClientOptions options = new HttpClientOptions().setDefaultPort(8080).setDefaultHost("localhost");
				vertx.createHttpClient(options).websocket("/realtime?X-Auth-Token=" + user1[0].getLoginToken().getToken(), handler -> {
					// Subscribe
					handler.write(Json.encodeToBuffer(new Envelope().sub(new EnvelopeSub().conversation(new EnvelopeSubConversation().conversationId("testConversation1")))));

					// Send message
					handler.write(Json.encodeToBuffer(new Envelope().method(new EnvelopeMethod().sendMessage(new EnvelopeMethodSendMessage().conversationId("testConversation1").message("hello")))));
				});

				// User 2 client
				vertx.createHttpClient(options).websocket("/realtime?X-Auth-Token=" + user2[0].getLoginToken().getToken(), handler -> {
					handler.handler(incoming -> {
						Envelope envelope = Json.decodeValue(incoming, Envelope.class);
						context.assertEquals(envelope.getAdded().getChatMessage().getMessage(), "hello");
						// TODO: Undeploy
						async.complete();
					});
					// Subscribe
					handler.write(Json.encodeToBuffer(new Envelope().sub(new EnvelopeSub().conversation(new EnvelopeSubConversation().conversationId("testConversation1")))));
				});
			}));
		}));

		async.awaitSuccess(35000L);
		Async async2 = context.async();
		if (vertices[0] != null) {
			vertices[0].close(then -> {
				Mongo.mongo().close();
				hazelcastInstance.shutdown();
				async2.complete();
			});
		}
		async2.awaitSuccess(35000L);
	}
}
