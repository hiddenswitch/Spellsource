package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.api.DefaultApi;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.Accounts;
import com.hiddenswitch.spellsource.net.Friends;
import com.hiddenswitch.spellsource.net.impl.SpellsourceAuthHandler;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import com.hiddenswitch.spellsource.net.models.CreateAccountResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitEvent;

public class FriendTest extends SpellsourceTestBase {


	@Test
	public void testFriendsApi(TestContext testContext) {
		Vertx vertx = contextRule.vertx();
		sync(() -> {
			DefaultApi defaultApi = getApi();
			// create first account
			CreateAccountResponse createAccount1Response = createRandomAccount();
			// create second account
			CreateAccountResponse createAccount2Response = createRandomAccount();

			vertx.executeBlocking(fut -> {
				// authenticate with first account
				String token = createAccount1Response.getLoginToken().getToken();
				testContext.assertNotNull(token, "auth token is null");
				defaultApi.getApiClient().setApiKey(token);

				// Test putting a friend by user id is illegal
				FriendPutResponse friendPutResponseDoesNotExist = null;
				try {
					friendPutResponseDoesNotExist = defaultApi.friendPut(new FriendPutRequest().friendId("illegal"));
				} catch (ApiException e) {
					testContext.assertEquals(409, e.getCode(), "Cannot use friendId. Should return 409");
				}
				testContext.assertNull(friendPutResponseDoesNotExist);
				String usernameWithToken1 = createAccount1Response.getRecord().getUsername() + "#" + createAccount1Response.getRecord().getPrivacyToken();
				String usernameWithToken2 = createAccount2Response.getRecord().getUsername() + "#" + createAccount2Response.getRecord().getPrivacyToken();


				// add second account as friend
				FriendPutResponse friendPutResponse = null;
				try {
					friendPutResponse = defaultApi.friendPut(
							new FriendPutRequest().usernameWithToken(usernameWithToken2));
				} catch (ApiException e) {
					testContext.assertEquals(200, e.getCode(), "Adding new friend. Should return 200");
				}

				testContext.assertEquals(createAccount2Response.getUserId(), friendPutResponse.getFriend().getFriendId());

				// test putting friend that already exists
				try {
					defaultApi.friendPut(new FriendPutRequest().usernameWithToken(usernameWithToken2));
				} catch (ApiException e) {
					testContext.assertEquals(409, e.getCode(), "Adding existing friend. Should return 409");
				}

				// test putting friend that already exists - second direction
				defaultApi.getApiClient().setApiKey(createAccount2Response.getLoginToken().getToken()); //reauth as friend
				try {
					defaultApi.friendPut(new FriendPutRequest().usernameWithToken(usernameWithToken1));
				} catch (ApiException e) {
					testContext.assertEquals(409, e.getCode(),
							"Adding existing friend (second direction). Should return 409");
				}

				// unfriend a user that doesn't exist
				try {
					defaultApi.friendDelete("idontexist");
				} catch (ApiException e) {
					testContext.assertEquals(404, e.getCode(),
							"Friend account doesn't exist. Should return 404");
				}

				// unfriend the first user
				UnfriendResponse unfriendResponse = null;
				try {
					unfriendResponse = defaultApi.friendDelete(createAccount1Response.getUserId());
				} catch (ApiException e) {
					testContext.assertEquals(200, e.getCode(),
							"Unfriending an existing friend. expecting 200");
				}
				testContext.assertNotNull(unfriendResponse.getDeletedFriend(),
						"unfriend response should include the friend details");
				testContext.assertEquals(unfriendResponse.getDeletedFriend().getFriendId(),
						createAccount1Response.getUserId());

				// try to unfriend the first user again
				try {
					defaultApi.friendDelete(createAccount1Response.getUserId());
				} catch (ApiException e) {
					testContext.assertEquals(404, e.getCode(), "Not friends. should return 404");
				}

				// try to unfriend from the other direction
				defaultApi.getApiClient().setApiKey(createAccount1Response.getLoginToken().getToken());
				try {
					defaultApi.friendDelete(createAccount2Response.getUserId());
				} catch (ApiException e) {
					testContext.assertEquals(404, e.getCode(),
							"Not friends (2nd direction). should return 404");
				}
				fut.complete();
			}, testContext.asyncAssertSuccess());
		}, testContext);

	}

	@Test
	public void testDoesNotifyPresence(TestContext context) {
		sync(() -> {
			Collection<AsyncResult<WebSocket>> sockets = new ConcurrentLinkedDeque<>();
			CountDownLatch latch = new CountDownLatch(1);
			try {
				CreateAccountResponse account1 = createRandomAccount();
				CreateAccountResponse account2 = createRandomAccount();
				HttpClient httpClient = Vertx.currentContext().owner().createHttpClient();
				AtomicBoolean didGetOnline = new AtomicBoolean();
				AtomicBoolean didGetOffline = new AtomicBoolean();
				CountDownLatch atLeastConnected = new CountDownLatch(1);

				httpClient.webSocket(8080, "localhost", "/realtime?" + SpellsourceAuthHandler.HEADER + "=" + account1.getLoginToken().getToken(), sockets::add);

				httpClient.webSocket(8080, "localhost", "/realtime?" + SpellsourceAuthHandler.HEADER + "=" + account2.getLoginToken().getToken(), ws2 -> {
					sockets.add(ws2);

					if (ws2.succeeded()) {
						ws2.result().handler(buf -> {
							Envelope msg = Json.decodeValue(buf, Envelope.class);
							atLeastConnected.countDown();
							if ((msg.getChanged() != null && msg.getChanged().getFriend() != null)
									|| (msg.getAdded() != null && msg.getAdded().getFriend() != null)) {
								Friend friend = msg.getChanged() == null ? msg.getAdded().getFriend() : msg.getChanged().getFriend();
								switch (friend.getPresence()) {
									case ONLINE:
										context.assertTrue(didGetOffline.compareAndSet(false, false));
										context.assertTrue(didGetOnline.compareAndSet(false, true));
										latch.countDown();
										break;
									case OFFLINE:
										context.assertTrue(didGetOnline.compareAndSet(true, false));
										context.assertTrue(didGetOffline.compareAndSet(false, true));
										latch.countDown();
										break;
								}
							}
						});
					}

				});
				atLeastConnected.await();
				Friends.putFriend(mongo().findOne(Accounts.USERS, json("_id", account1.getUserId()), UserRecord.class), new FriendPutRequest().usernameWithToken(account2.getRecord().getUsername() + "#" + account2.getRecord().getPrivacyToken()));
				latch.await();
			} finally {
				for (AsyncResult<WebSocket> socket : sockets) {
					if (socket.result() != null) {
						socket.result().close();
					}
				}
			}
		}, context);
	}
}
