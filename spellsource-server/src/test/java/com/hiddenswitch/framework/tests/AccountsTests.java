package com.hiddenswitch.framework.tests;

import com.google.common.base.Strings;
import com.google.protobuf.Empty;
import com.hiddenswitch.framework.*;
import com.hiddenswitch.framework.rpc.Hiddenswitch.AccessTokenResponse;
import com.hiddenswitch.framework.rpc.Hiddenswitch.CreateAccountRequest;
import com.hiddenswitch.framework.rpc.Hiddenswitch.GetAccountsRequest;
import com.hiddenswitch.framework.rpc.Hiddenswitch.UserEntity;
import com.hiddenswitch.framework.rpc.VertxAccountsGrpcClient;
import com.hiddenswitch.framework.tests.applications.StandaloneApplication;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.RequestOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

import static io.vertx.await.Async.await;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.*;

public class AccountsTests extends FrameworkTestBase {

	@Test
	public void testCreateUser(Vertx vertx, VertxTestContext testContext) {
		startGateway(vertx)
				.compose(v -> Accounts.createUser("test2@hiddenswitch.com", "username", "password"))
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testCreateUser2(VertxTestContext testContext) {
		var application = new Application();
		var deploy = application.deploy();
		deploy
				.compose(v -> Accounts.createUser(UUID.randomUUID() + "@hiddenswitch.com", UUID.randomUUID().toString(), "password"))
				.compose(ue -> {
					testContext.verify(() -> {
						assertNotNull(ue.getId());
					});
					return Future.succeededFuture(ue);
				})
				.eventually(v -> {
					if (deploy.succeeded()) {
						return deploy.result().close();
					}
					return Future.failedFuture(deploy.cause());
				})
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testCreateAndLoginUserWithGrpc(Vertx vertx, VertxTestContext testContext) {
		var webClient = WebClient.create(vertx);
		var client = new Client(vertx, webClient);
		startGateway(vertx)
				.compose(v -> client.createAndLogin("testusername2", "email@hiddenswitch.com", "password"))
				.onComplete(testContext.succeedingThenComplete());
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	public void testCreateUserCheckPremadeDecks(boolean premade, Vertx vertx, VertxTestContext testContext) {
		testVirtual(vertx, testContext, () -> {
			await(startGateway(vertx));
			var client = new Client(vertx);
			var userId = UUID.randomUUID().toString();
			await(client.createAndLogin(userId, userId + "@hiddenswitch.com", "password", premade));
			var decksGetAllResponse = await(client.legacy().decksGetAll(Empty.getDefaultInstance()));
			assertEquals(premade ? Legacy.getPremadeDecks().size() : 0, decksGetAllResponse.getDecksCount(), "premade decks count");
		});
	}

	@Test
	public void testGuestAccountCreation(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		startGateway(vertx)
				.compose(v -> client.unauthenticated().createAccount(CreateAccountRequest.newBuilder()
						.setGuest(true).build()))
				// create two users to assert usernames were unique
				.compose(v -> client.unauthenticated().createAccount(CreateAccountRequest.newBuilder()
						.setGuest(true).build()))
				.compose(reply -> {
					testContext.verify(() -> {
						assertTrue(reply.hasUserEntity());
						assertFalse(Strings.isNullOrEmpty(reply.getUserEntity().getEmail()));
					});
					return Future.succeededFuture();
				}).onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testCreateAndLoginUserWithGrpc2(VertxTestContext testContext) {
		var vertx = Vertx.vertx();
		var application = new Application();
		var deploy = application.deploy();
		var webClient = WebClient.create(vertx);
		var client = new Client(vertx, webClient);
		deploy
				.compose(v -> client.createAndLogin(UUID.randomUUID().toString(), UUID.randomUUID().toString() + "@hiddenswitch.com", "password"))
				.compose(res -> {
					testContext.verify(() -> {
						assertNotEquals("", res.getUserEntity().getId());
					});
					return Future.succeededFuture(res);
				})
				.eventually(v -> vertx.close())
				.eventually(v -> {
					if (deploy.succeeded()) {
						return deploy.result().close();
					}
					return Future.failedFuture(deploy.cause());
				})
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testCreateAndLoginUserWithGrpcAuth1(Vertx vertx, VertxTestContext testContext) {
		var webClient = WebClient.create(vertx);
		var client = new Client(vertx, webClient);
		startGateway(vertx)
				.compose(v -> Accounts.createUser("other@hiddenswitch.com", "username4", "password"))
				.compose(otherUser -> client.createAndLogin("testusername3", "email1@hiddenswitch.com", "password")
						.compose(myAccountReply -> {
							var stub = new VertxAccountsGrpcClient(client.client().setRequestOptions(new RequestOptions().setHeaders(client.credentials())), client.address());
							var otherId = otherUser.getId();
							var myAccount = myAccountReply.getUserEntity();
							return stub.getAccounts(GetAccountsRequest.newBuilder()
											.addIds(otherId)
											.addIds(myAccount.getId())
											.build())
									.onSuccess(reply2 -> {
										testContext.verify(() -> {
											var records = reply2.getUserEntitiesList().stream().collect(toMap(UserEntity::getId, Function.identity()));
											assertEquals("email1@hiddenswitch.com", records.get(myAccount.getId()).getEmail(), "should see my own email");
											assertEquals("", records.get(otherId).getEmail(), "should not receive emails of other users");
										});
									});
						}))
				.compose(v -> client.closeFut())
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Disabled
	public void testLoginWithRestClient(Vertx vertx, VertxTestContext testContext) {
		var webClient = WebClient.create(vertx);
		var client = new Client(vertx, webClient);
		startGateway(vertx)
				.compose(v -> client.privilegedCreateAndLogin("testemail@hiddenswitch.com", "testusername1", "password"))
				.onFailure(testContext::failNow)
				.onComplete(ignored -> {
					testContext.verify(() -> {
						assertNotNull(client.getAccessToken());
						assertNotNull(client.getUserEntity());
					});
				})
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testVerifiesToken(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		startGateway(vertx)
				.compose(v -> client.unauthenticated().verifyToken(AccessTokenResponse.newBuilder().build())
						.onSuccess(b -> testContext.verify(() -> {
							assertFalse(b.getValue(), "should not verify");
						}))
				)
				.compose(v -> client.unauthenticated().verifyToken(AccessTokenResponse.newBuilder()
						.setToken("blah").build()).onSuccess(b -> testContext.verify(() -> {
					assertFalse(b.getValue(), "should not verify");
				})))
				.compose(v -> client.createAndLogin())
				.compose(res -> client.unauthenticated().verifyToken(res.getAccessTokenResponse()).onSuccess(b -> testContext.verify(() -> {
					assertTrue(b.getValue(), "should verify");
				})))
				.compose(v -> client.closeFut())
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testLoginWithRest(Vertx vertx, VertxTestContext testContext) {
		var webClient = WebClient.create(vertx);
		var testUser = new UserRepresentation();
		testUser.setEmail(UUID.randomUUID().toString() + "@test.com");
		testUser.setUsername(UUID.randomUUID().toString());
		testUser.setFirstName("Test");
		testUser.setLastName("McTester");
		testUser.setEnabled(true);
		var credential = new CredentialRepresentation();
		testUser.setCredentials(Collections.singletonList(credential));
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue("password");
		credential.setTemporary(false);

		// Create an account programmatically
		startGateway(vertx)
				.compose(v -> Accounts.realm())
				.compose(hiddenswitch -> Environment.executeBlocking(() -> hiddenswitch.users().create(testUser)))
				.compose(ignored -> {
					var url = StandaloneApplication.KEYCLOAK.getAuthServerUrl() + "/realms/hiddenswitch/protocol/openid-connect/token";

					// Login
					return webClient.postAbs(url)
							.followRedirects(true)
							.sendForm(MultiMap.caseInsensitiveMultiMap()
									.add("client_id", StandaloneApplication.CLIENT_ID)
									.add("grant_type", "password")
									.add("client_secret", StandaloneApplication.CLIENT_SECRET)
									.add("scope", "openid")
									// username or password can be used here
									.add("username", testUser.getEmail())
									.add("password", credential.getValue()))
							.onSuccess(res -> {
								testContext.verify(() -> {
									var tokenContainer = res.bodyAsJsonObject();
									assertEquals("Bearer", tokenContainer.getString("token_type"));
								});
							});
				})
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testTokenExpiryDuration(Vertx vertx, VertxTestContext testContext) {
		var webClient = WebClient.create(vertx);
		var client = new Client(vertx, webClient);

		// Number of seconds in 364 days
		long minimumExpirySeconds = 364L * 24L * 60L * 60L;

		startGateway(vertx)
				.compose(v -> client.createAndLogin(
						UUID.randomUUID().toString(),
						UUID.randomUUID().toString() + "@hiddenswitch.com",
						"password"))
				.onSuccess(loginReply -> {
					testContext.verify(() -> {
						assertTrue(client.getAccessToken().getExpiresIn() >= minimumExpirySeconds,
								String.format("Token expiry duration (%d seconds) should be at least %d seconds (364 days)",
										client.getAccessToken().getExpiresIn(), minimumExpirySeconds));
					});
				})
				.compose(v -> client.closeFut())
				.onComplete(testContext.succeedingThenComplete());
	}
}
