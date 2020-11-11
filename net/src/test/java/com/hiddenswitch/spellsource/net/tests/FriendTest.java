package com.hiddenswitch.spellsource.net.tests;

import co.paralleluniverse.strands.concurrent.CountDownLatch;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.FriendPutRequest;
import com.hiddenswitch.spellsource.client.models.FriendPutResponse;
import com.hiddenswitch.spellsource.client.models.UnfriendResponse;
import com.hiddenswitch.spellsource.net.Accounts;
import com.hiddenswitch.spellsource.net.Friends;
import com.hiddenswitch.spellsource.net.impl.SpellsourceAuthHandler;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import com.hiddenswitch.spellsource.net.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static org.junit.jupiter.api.Assertions.*;

public class FriendTest extends SpellsourceTestBase {

	@Test
	public void testFriendsApi(Vertx vertx, VertxTestContext testContext) throws ExecutionException, InterruptedException {
		var createAccount1Response = createRandomAccount(testContext, vertx);
		var createAccount2Response = createRandomAccount(testContext, vertx);

		var defaultApi = getApi();

		// authenticate with first account
		var token = createAccount1Response.getLoginToken().getToken();
		assertNotNull(token, "auth token is null");
		defaultApi.getApiClient().setApiKey(token);

		// Test putting a friend by user id is illegal
		FriendPutResponse friendPutResponseDoesNotExist = null;
		try {
			friendPutResponseDoesNotExist = defaultApi.friendPut(new FriendPutRequest().friendId("illegal"));
		} catch (ApiException e) {
			assertEquals(500, e.getCode(), "Cannot use friendId. Should return 409");
		}
		assertNull(friendPutResponseDoesNotExist);
		var usernameWithToken1 = createAccount1Response.getRecord().getUsername() + "#" + createAccount1Response.getRecord().getPrivacyToken();
		var usernameWithToken2 = createAccount2Response.getRecord().getUsername() + "#" + createAccount2Response.getRecord().getPrivacyToken();


		// add second account as friend
		FriendPutResponse friendPutResponse = null;
		try {
			friendPutResponse = defaultApi.friendPut(
					new FriendPutRequest().usernameWithToken(usernameWithToken2));
		} catch (ApiException e) {
			assertEquals(200, e.getCode(), "Adding new friend. Should return 200");
		}

		assertEquals(createAccount2Response.getUserId(), friendPutResponse.getFriend().getFriendId());

		// test putting friend that already exists
		try {
			defaultApi.friendPut(new FriendPutRequest().usernameWithToken(usernameWithToken2));
			fail("should not reach");
		} catch (ApiException e) {
			assertEquals(500, e.getCode(), "Adding existing friend. Should return 409");
		}

		// test putting friend that already exists - second direction
		defaultApi.getApiClient().setApiKey(createAccount2Response.getLoginToken().getToken()); //reauth as friend
		try {
			defaultApi.friendPut(new FriendPutRequest().usernameWithToken(usernameWithToken1));
			fail("should not reach");
		} catch (ApiException e) {
			assertEquals(500, e.getCode(),
					"Adding existing friend (second direction). Should return 409");
		}

		// unfriend a user that doesn't exist
		try {
			defaultApi.friendDelete("idontexist");
			fail("should not reach");
		} catch (ApiException e) {
			assertEquals(500, e.getCode(),
					"Friend account doesn't exist. Should return 404");
		}

		// unfriend the first user
		UnfriendResponse unfriendResponse = null;
		try {
			unfriendResponse = defaultApi.friendDelete(createAccount1Response.getUserId());
		} catch (ApiException e) {
			assertEquals(200, e.getCode(),
					"Unfriending an existing friend. expecting 200");
		}
		assertNotNull(unfriendResponse.getDeletedFriend(),
				"unfriend response should include the friend details");
		assertEquals(unfriendResponse.getDeletedFriend().getFriendId(),
				createAccount1Response.getUserId());

		// try to unfriend the first user again
		try {
			defaultApi.friendDelete(createAccount1Response.getUserId());
			fail("should not reach");
		} catch (ApiException e) {
			assertEquals(500, e.getCode(), "Not friends. should return 404");
		}

		// try to unfriend from the other direction
		defaultApi.getApiClient().setApiKey(createAccount1Response.getLoginToken().getToken());
		try {
			defaultApi.friendDelete(createAccount2Response.getUserId());
			fail("should not reach");
		} catch (ApiException e) {
			assertEquals(500, e.getCode(),
					"Not friends (2nd direction). should return 404");
		}

		testContext.completeNow();
	}

	@Test
	public void testDoesNotifyPresence(Vertx vertx, VertxTestContext testContext) {
		runOnFiberContext(() -> {
			Collection<AsyncResult<WebSocket>> sockets = new ConcurrentLinkedDeque<>();
			var latch = new CountDownLatch(1);
			try {
				var account1 = createRandomAccount();
				var account2 = createRandomAccount();
				var httpClient = Vertx.currentContext().owner().createHttpClient();
				var didGetOnline = new AtomicBoolean();
				var didGetOffline = new AtomicBoolean();
				var atLeastConnected = new CountDownLatch(1);

				httpClient.webSocket(8080, "localhost", "/realtime?" + SpellsourceAuthHandler.HEADER + "=" + account1.getLoginToken().getToken(), sockets::add);
				httpClient.webSocket(8080, "localhost", "/realtime?" + SpellsourceAuthHandler.HEADER + "=" + account2.getLoginToken().getToken(), ws2 -> {
					sockets.add(ws2);
					if (ws2.succeeded()) {
						ws2.result().handler(buf -> {
							verify(testContext, () -> {
								var msg = Json.decodeValue(buf, Envelope.class);
								atLeastConnected.countDown();
								if ((msg.getChanged() != null && msg.getChanged().getFriend() != null)
										|| (msg.getAdded() != null && msg.getAdded().getFriend() != null)) {
									var friend = msg.getChanged() == null ? msg.getAdded().getFriend() : msg.getChanged().getFriend();
									switch (friend.getPresence()) {
										case ONLINE:
											assertTrue(didGetOffline.compareAndSet(false, false));
											assertTrue(didGetOnline.compareAndSet(false, true));
											latch.countDown();
											break;
										case OFFLINE:
											assertTrue(didGetOnline.compareAndSet(true, false));
											assertTrue(didGetOffline.compareAndSet(false, true));
											latch.countDown();
											break;
									}
								}
							});

						});
					}

				});
				atLeastConnected.await();
				Friends.putFriend(mongo().findOne(Accounts.USERS, json("_id", account1.getUserId()), UserRecord.class), new FriendPutRequest().usernameWithToken(account2.getRecord().getUsername() + "#" + account2.getRecord().getPrivacyToken()));
				latch.await();
			} finally {
				for (var socket : sockets) {
					if (socket.result() != null) {
						socket.result().close();
					}
				}
			}
		}, testContext, vertx);
	}
}
