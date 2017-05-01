package com.hiddenswitch.proto3.net.impl;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.proto3.net.Games;
import com.hiddenswitch.proto3.net.client.ApiException;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.api.DefaultApi;
import com.hiddenswitch.proto3.net.client.models.Account;
import com.hiddenswitch.proto3.net.client.models.CreateAccountRequest;
import com.hiddenswitch.proto3.net.client.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.client.models.GetAccountsResponse;
import com.hiddenswitch.proto3.net.models.CurrentMatchRequest;
import com.hiddenswitch.proto3.net.util.ServiceTest;
import com.hiddenswitch.proto3.net.util.UnityClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import javax.websocket.Session;

/**
 * Created by bberman on 2/18/17.
 */
public class ServerTest extends ServiceTest<ServerImpl> {
	private String deploymentId;

	@Test
	public void testShutdownAndRestartServer(TestContext context) {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			// Play a match
			UnityClient client = new UnityClient(getContext());
			client.createUserAccount("testaccount");
			client.matchmakeAndPlayAgainstAI(null);
			client.waitUntilDone();
			getContext().assertTrue(client.isGameOver());
			Void r = Sync.awaitResult(h -> vertx.undeploy(deploymentId, h));
			ServerImpl r2 = Sync.awaitResult(h -> deployServices(vertx, h));
			UnityClient client2 = new UnityClient(getContext());
			client2.loginWithUserAccount("testaccount");
			client2.matchmakeAndPlayAgainstAI(null);
			client2.waitUntilDone();
			getContext().assertTrue(client2.isGameOver());
		});
	}

	@Test
	public void testAccountFlow(TestContext context) throws ApiException {
		Configuration.getDefaultApiClient().setBasePath("http://localhost:8080/v1");
		DefaultApi api = new DefaultApi();
		CreateAccountResponse response1 = api.createAccount(new CreateAccountRequest()
				.name("username")
				.email("email@email.com")
				.password("password"));

		api.getApiClient().setApiKey(response1.getLoginToken());
		final String userId = response1.getAccount().getId();
		context.assertNotNull(userId);
		GetAccountsResponse response2 = api.getAccount(userId);
		context.assertTrue(response2.getAccounts().size() > 0);

		for (Account account : new Account[]{response1.getAccount(), response2.getAccounts().get(0)}) {
			context.assertNotNull(account.getId());
			context.assertNotNull(account.getEmail());
			context.assertNotNull(account.getName());
			context.assertNotNull(account.getPersonalCollection());
			context.assertNotNull(account.getDecks());
			context.assertTrue(account.getDecks().size() > 0);
			context.assertTrue(account.getPersonalCollection().getInventory().size() > 0);
		}

		context.async().complete();
	}

	@Test
	public void testUnityClient(TestContext context) throws InterruptedException, SuspendExecution {
		setLoggingLevel(Level.ERROR);
		final String property = System.getProperty("minionate.unityTests");
		final int tests = Integer.parseInt(property != null ? property : "1");
		final Async async = context.async();

		for (int i = 0; i < tests; i++) {
			UnityClient client = new UnityClient(getContext());
			client.createUserAccount(null);
			client.matchmakeAndPlayAgainstAI(null);
			float time = 0f;
			while (!(time > 120f || client.isGameOver())) {
				Strand.sleep(1000);
				time += 1f;
			}
			getContext().assertTrue(client.isGameOver());
		}
		async.complete();
	}

	@Test
	public void testDisconnectingUnityClient(TestContext context) throws InterruptedException, SuspendExecution {
		setLoggingLevel(Level.ERROR);
		getContext().assertEquals(Games.getDefaultNoActivityTimeout(), 8000L);
		final Async async = context.async();

		UnityClient client = new UnityClient(getContext(), 5);
		Thread clientThread = new Thread(() -> {
			client.createUserAccount(null);
			client.matchmakeAndPlayAgainstAI(null);
		});
		clientThread.start();

		// wait 16 seconds
		Strand.sleep(16000);
		// Assert that session was closed
		getContext().assertEquals(service.matchmaking.getCurrentMatch(new CurrentMatchRequest(client.getAccount().getId())).getGameId(), null);
		async.complete();
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<ServerImpl>> done) {
		System.setProperty("games.defaultNoActivityTimeout", "8000");
		ServerImpl instance = new ServerImpl();
		vertx.deployVerticle(instance, then -> {
			deploymentId = then.result();
			done.handle(Future.succeededFuture(instance));
		});
	}
}
