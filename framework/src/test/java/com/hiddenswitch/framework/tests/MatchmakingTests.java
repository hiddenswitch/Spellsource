package com.hiddenswitch.framework.tests;

import com.google.protobuf.Empty;
import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Matchmaking;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.spellsource.rpc.MatchmakingQueueConfiguration;
import com.hiddenswitch.spellsource.rpc.MatchmakingQueuePutRequest;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.streams.WriteStream;
import io.vertx.junit5.VertxTestContext;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MatchmakingTests extends FrameworkTestBase {

	@Test
	public void testNoSuchQueueExists(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);

		client.createAndLogin()
				.compose(ignored -> client.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var matchmaking = client.matchmaking();
					var commandsFut = Promise.<WriteStream<MatchmakingQueuePutRequest>>promise();
					var response = matchmaking.enqueue(commandsFut::complete);
					var ended = Promise.<Void>promise();
					response.endHandler(ended::complete);
					commandsFut.future().compose(commands -> commands.write(MatchmakingQueuePutRequest.newBuilder()
							.setQueueId("nonexistent queue")
							.setDeckId(decks.getDecks(0).getCollection().getId())
							.build()));
					// silently closes
					response.handler(f -> testContext.failNow("should not receive any queue messages, should just fail immediately"));
					return ended.future();
				})
				.onComplete(testContext.succeedingThenComplete())
				.onComplete(client::close);
	}

	@Test
	public void testNoDeckIdSpecified(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var queueId = "single player 1";
		client.createAndLogin()
				.compose(ignored -> vertx.deployVerticle(new Matchmaking(createSinglePlayerQueue(queueId))))
				.compose(ignored -> {
					var matchmaking = client.matchmaking();
					var commandsFut = Promise.<WriteStream<MatchmakingQueuePutRequest>>promise();
					var response = matchmaking.enqueue(commandsFut::complete);
					var ended = Promise.<Void>promise();
					response.endHandler(ended::complete);
					response.handler(f -> testContext.failNow("should not receive any queue messages, should just fail immediately"));
					commandsFut.future().compose(commands -> commands.write(MatchmakingQueuePutRequest.newBuilder()
							.setQueueId(queueId)
							.setDeckId("1")
							.build()));
					return ended.future();
				})
				.onComplete(testContext.succeedingThenComplete())
				.onComplete(client::close);
	}

	@Test
	public void testCreateQueueNoExceptions(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var queueId = "single player 1";

		client.createAndLogin()
				.compose(ignored -> {
					var verticle = new Matchmaking(createSinglePlayerQueue(queueId));
					return vertx.deployVerticle(verticle).compose(v -> verticle.queueStarted());
				})
				.onComplete(testContext.succeedingThenComplete())
				.onComplete(client::close);
	}

	@Test
	public void testSinglePlayerQueueCreatesMatch(Vertx vertx, VertxTestContext testContext) {
		var gameCreated = testContext.checkpoint();
		var client = new Client(vertx);

		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking(createSinglePlayerQueue(queueId)) {
			@Override
			public Future<Long> createGame(MatchmakingQueueConfiguration configuration, MatchmakingTickets... tickets) {
				testContext.verify(() -> {
					assertEquals(1, tickets.length);
					assertEquals(client.getUserEntity().getId(), tickets[0].getUserId());
					gameCreated.flag();
				});
				return super.createGame(configuration, tickets);
			}
		};

		vertx.deployVerticle(matchmakingQueue)
				.compose(v -> client.createAndLogin())
				.compose(v -> client.matchmake(queueId))
				.onSuccess(shouldFindGame -> {
					testContext.verify(() -> {
						assertNotNull(shouldFindGame);
						assertTrue(shouldFindGame.hasUnityConnection());
					});
				})
				.onComplete(client::close)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testMultiplayerQueueCreatesMatch(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var gameCreated = testContext.checkpoint();

		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking(createMultiplayerQueue(queueId)) {
			@Override
			public Future<Long> createGame(MatchmakingQueueConfiguration configuration, MatchmakingTickets... tickets) {
				testContext.verify(() -> {
					assertEquals(2, tickets.length);
					assertEquals(queueId, configuration.getId());
					gameCreated.flag();
				});
				return super.createGame(configuration, tickets);
			}
		};

		vertx.deployVerticle(matchmakingQueue)
				.compose(v -> client1.createAndLogin())
				.compose(v -> client2.createAndLogin())
				.compose(v -> CompositeFuture.join(client1.matchmake(queueId), client2.matchmake(queueId)))
				.onComplete(testContext.succeedingThenComplete())
				.onComplete(client1::close)
				.onComplete(client2::close);
	}

	@Test
	public void testWaitsInMultiplayerQueue(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);

		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking(createMultiplayerQueue(queueId)) {
			@Override
			public Future<Long> createGame(MatchmakingQueueConfiguration configuration, MatchmakingTickets... tickets) {
				testContext.failNow("should not create game");
				return super.createGame(configuration, tickets);
			}
		};

		vertx.deployVerticle(matchmakingQueue)
				.compose(v -> client1.createAndLogin())
				.compose(v -> CompositeFuture.any(client1.matchmake(queueId), Environment.sleep(vertx, 5000)))
				.onSuccess(v -> testContext.verify(() -> {
					assertFalse(client1.matchmakingResponse().succeeded());
					assertFalse(client1.matchmakingResponse().failed());
				}))
				.onComplete(testContext.succeedingThenComplete())
				.onComplete(client1::close);
	}

	@Test
	public void testMultiplayerPlayerQueueCancel(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var client3 = new Client(vertx);
		var gameCreated = testContext.checkpoint();

		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking(createMultiplayerQueue(queueId)) {
			@Override
			public Future<Long> createGame(MatchmakingQueueConfiguration configuration, MatchmakingTickets... tickets) {
				testContext.verify(() -> {
					assertEquals(2, tickets.length);
					assertEquals(queueId, configuration.getId());
					var ticket2 = Arrays.stream(tickets).filter(t -> t.getUserId().equals(client2.getUserEntity().getId())).findFirst().orElseThrow(AssertionError::new);
					var ticket3 = Arrays.stream(tickets).filter(t -> t.getUserId().equals(client3.getUserEntity().getId())).findFirst().orElseThrow(AssertionError::new);
					assertEquals(queueId, ticket2.getQueueId());
					assertEquals(queueId, ticket3.getQueueId());
					gameCreated.flag();
				});
				return super.createGame(configuration, tickets);
			}
		};

		vertx.deployVerticle(matchmakingQueue)
				.compose(v -> CompositeFuture.join(client1.createAndLogin(), client2.createAndLogin(), client3.createAndLogin()))
				.compose(v1 -> {
					client1.matchmake(queueId);
					return Environment.sleep(vertx, 5000).compose(v2 -> client1.cancelMatchmaking());
				})
				.compose(v -> CompositeFuture.join(client2.matchmake(queueId), client3.matchmake(queueId)))
				.onComplete(testContext.succeedingThenComplete())
				.onComplete(client1::close)
				.onComplete(client2::close);
	}

	@NotNull
	private MatchmakingQueueConfiguration createSinglePlayerQueue(String queueId) {
		return MatchmakingQueueConfiguration.newBuilder()
				.setId(queueId)
				.setAutomaticallyClose(false)
				.setLobbySize(1)
				.setAwaitingLobbyTimeout(0)
				.setBotOpponent(true)
				.setEmptyLobbyTimeout(0)
				.setMaxTicketsToProcess(100)
				.setName("single player test")
				.setPrivateLobby(false)
				.setOnce(false)
				.setScanFrequency(1000)
				.setStartsAutomatically(true)
				.setStillConnectedTimeout(0)
				.build();
	}

	private MatchmakingQueueConfiguration createMultiplayerQueue(String queueId) {
		return MatchmakingQueueConfiguration.newBuilder()
				.setId(queueId)
				.setAutomaticallyClose(false)
				.setLobbySize(2)
				.setAwaitingLobbyTimeout(0)
				.setBotOpponent(false)
				.setEmptyLobbyTimeout(0)
				.setMaxTicketsToProcess(100)
				.setName("multiplayer test")
				.setPrivateLobby(false)
				.setOnce(false)
				.setScanFrequency(1000)
				.setStartsAutomatically(true)
				.setStillConnectedTimeout(0)
				.build();
	}
}
