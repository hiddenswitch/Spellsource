package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.proto3.net.client.ApiException;
import com.hiddenswitch.proto3.net.client.Configuration;
import com.hiddenswitch.proto3.net.client.api.DefaultApi;
import com.hiddenswitch.proto3.net.client.models.Account;
import com.hiddenswitch.proto3.net.client.models.CreateAccountRequest;
import com.hiddenswitch.proto3.net.client.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.client.models.GetAccountsResponse;
import com.hiddenswitch.proto3.net.impl.ServerImpl;
import com.hiddenswitch.proto3.net.util.ServiceTest;
import com.hiddenswitch.proto3.net.util.UnityClient;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

/**
 * Created by bberman on 2/18/17.
 */
public class ServerTest extends ServiceTest<ServerImpl> {
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
		final int tests = 10;
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

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<ServerImpl>> done) {
		ServerImpl instance = new ServerImpl();
		vertx.deployVerticle(instance, then -> {
			done.handle(Future.succeededFuture(instance));
		});
	}
}
