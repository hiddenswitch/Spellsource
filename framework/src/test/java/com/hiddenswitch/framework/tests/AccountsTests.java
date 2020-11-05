package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Accounts;
import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.rpc.AccountsGrpc;
import com.hiddenswitch.framework.rpc.GetAccountsReply;
import com.hiddenswitch.framework.rpc.GetAccountsRequest;
import com.hiddenswitch.framework.rpc.UserEntity;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.function.Function;

import static io.vertx.junit5.web.TestRequest.testRequest;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AccountsTests extends FrameworkTestBase {

	@Test
	public void testCreateUser(Vertx vertx, VertxTestContext testContext) {
		Accounts
				.createUser("test2@hiddenswitch.com", "username", "password")
				.onComplete(testContext.completing());
	}

	@Test
	public void testCreateAndLoginUserWithGrpc(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);
		client.createAndLogin("testusername2", "email@hiddenswitch.com", "password")
				.onComplete(testContext.completing());
	}

	@Test
	public void testCreateAndLoginUserWithGrpcAuth1(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);
		Accounts.createUser("other@hiddenswitch.com", "username4", "password")
				.compose(otherUser -> client.createAndLogin("testusername3", "email1@hiddenswitch.com", "password")
						.compose(myAccountReply -> {
							var stub = AccountsGrpc.newVertxStub(client.channel()).withCallCredentials(client.credentials());
							var promise = Promise.<GetAccountsReply>promise();
							var otherId = otherUser.getId();
							var myAccount = myAccountReply.getUserEntity();
							stub.getAccounts(GetAccountsRequest.newBuilder()
									.addIds(otherId)
									.addIds(myAccount.getId())
									.build(), promise);
							return promise.future()
									.onSuccess(reply2 -> {
										testContext.verify(() -> {
											var records = reply2.getUserEntitiesList().stream().collect(toMap(UserEntity::getId, Function.identity()));
											assertEquals("email1@hiddenswitch.com", records.get(myAccount.getId()).getEmail(), "should see my own email");
											assertEquals("", records.get(otherId).getEmail(), "should not receive emails of other users");
										});
									});
						}))
				.onComplete(testContext.completing());
	}

	@Test
	public void testLoginWithRestClient(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);
		client.privilegedCreateAndLogin("testusername1", "testemail@hiddenswitch.com", "password")
				.onComplete(ignored -> {
					testContext.verify(() -> {
						assertNotNull(client.getAccessToken());
						assertNotNull(client.getUserEntity());
					});
				})
				.onComplete(testContext.completing());
	}

	@Test
	public void testLoginWithRest(WebClient client, Vertx vertx, VertxTestContext testContext) {
		var testUser = new UserRepresentation();
		testUser.setEmail("test@hiddenswitch.com");
		testUser.setUsername("DoctorTestgloss");
		testUser.setFirstName("Test");
		testUser.setLastName("McTester");
		testUser.setEnabled(true);
		var credential = new CredentialRepresentation();
		testUser.setCredentials(Collections.singletonList(credential));
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue("password");
		credential.setTemporary(false);

		// Create an account programmatically
		Accounts.get()
				.compose(hiddenswitch -> Environment.executeBlocking(() -> hiddenswitch.users().create(testUser)))
				.compose(ignored -> {
					var url = FrameworkTestBase.keycloak.getAuthServerUrl() + "/realms/hiddenswitch/protocol/openid-connect/token";

					// Login
					return testRequest(client.postAbs(url))
							.with(req -> req.followRedirects(false))
							.expect(res -> {
								testContext.verify(() -> {
									var tokenContainer = res.bodyAsJsonObject();
									assertEquals("bearer", tokenContainer.getString("token_type"));
								});
							})
							.sendURLEncodedForm(MultiMap.caseInsensitiveMultiMap()
									.add("client_id", CLIENT_ID)
									.add("grant_type", "password")
									.add("client_secret", CLIENT_SECRET)
									.add("scope", "openid")
									// username or password can be used here
									.add("username", testUser.getEmail())
									.add("password", credential.getValue()), testContext);
				})
				.onComplete(testContext.completing());
	}
}
