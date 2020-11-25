package com.hiddenswitch.framework.tests;

import com.google.protobuf.Empty;
import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Matchmaking;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.GameUsersDao;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.MatchmakingTicketsDao;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.GameUsers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.framework.tests.impl.ToxiClient;
import com.hiddenswitch.spellsource.rpc.MatchmakingQueuePutRequest;
import io.github.jklingsporn.vertx.jooq.classic.reactivepg.ReactiveClassicGenericQueryExecutor;
import io.vertx.core.*;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.streams.WriteStream;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.hiddenswitch.framework.schema.spellsource.Tables.MATCHMAKING_TICKETS;
import static io.vertx.core.CompositeFuture.all;
import static java.util.stream.Collectors.toList;
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
				.onComplete(client::close)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testNoDeckIdSpecified(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var queueId = UUID.randomUUID().toString();
		client.createAndLogin()
				.compose(v -> Matchmaking.createQueue(createSinglePlayerQueue(queueId)))
				.compose(ignored -> vertx.deployVerticle(new Matchmaking()))
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
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(client::close)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testCreateQueueNoExceptions(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var queueId = UUID.randomUUID().toString();

		client.createAndLogin()
				.compose(v -> Matchmaking.createQueue(createSinglePlayerQueue(queueId)))
				.compose(ignored -> {
					var verticle = new Matchmaking();
					return vertx.deployVerticle(verticle).compose(v -> verticle.strand());
				})
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(client::close)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testSinglePlayerQueueCreatesMatch(Vertx vertx, VertxTestContext testContext) {
		var gameCreated = testContext.checkpoint();
		var client = new Client(vertx);

		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking() {
			@Override
			public Future<Long> createGame(ReactiveClassicGenericQueryExecutor connection, MatchmakingQueues configuration, MatchmakingTickets... tickets) {
				testContext.verify(() -> {
					assertEquals(1, tickets.length);
					assertEquals(client.getUserEntity().getId(), tickets[0].getUserId());
					gameCreated.flag();
				});
				return super.createGame(connection, configuration, tickets);
			}
		};

		vertx.deployVerticle(matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createSinglePlayerQueue(queueId)))
				.compose(v -> client.createAndLogin())
				.compose(v -> client.matchmake(queueId))
				.onSuccess(shouldFindGame -> {
					testContext.verify(() -> {
						assertNotNull(shouldFindGame);
						assertTrue(shouldFindGame.hasUnityConnection());
					});
				})
				.onComplete(client::close)
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testMultiplayerQueueCreatesMatch(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var gameCreated = testContext.checkpoint();

		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking() {
			@Override
			public Future<Long> createGame(ReactiveClassicGenericQueryExecutor connection, MatchmakingQueues configuration, MatchmakingTickets... tickets) {
				testContext.verify(() -> {
					assertEquals(2, tickets.length);
					assertEquals(queueId, configuration.getId());
					gameCreated.flag();
				});
				return super.createGame(connection, configuration, tickets);
			}
		};

		vertx.deployVerticle(matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.compose(v -> client1.createAndLogin())
				.compose(v -> client2.createAndLogin())
				.compose(v -> CompositeFuture.join(client1.matchmake(queueId), client2.matchmake(queueId)))
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(client1::close)
				.onComplete(client2::close)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testWaitsInMultiplayerQueue(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var cancelled = testContext.checkpoint();
		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking() {
			@Override
			public Future<Long> createGame(ReactiveClassicGenericQueryExecutor connection, MatchmakingQueues configuration, MatchmakingTickets... tickets) {
				testContext.failNow("should not create game");
				return super.createGame(connection, configuration, tickets);
			}
		};

		vertx.deployVerticle(matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.compose(v -> client1.createAndLogin())
				.compose(v -> {
					client1.matchmake(queueId).onSuccess(res -> {
						cancelled.flag();
						testContext.verify(() -> {
							assertNull(res);
						});
					});
					return Environment.sleep(vertx, 5000);
				})
				.onSuccess(v -> testContext.verify(() -> {
					assertFalse(client1.matchmakingResponse().succeeded());
					assertFalse(client1.matchmakingResponse().failed());
				}))
				.compose(v -> {
					var ticketsDao = new MatchmakingTicketsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
					return ticketsDao.findManyByUserId(Collections.singletonList(client1.getUserEntity().getId()));
				})
				.onSuccess(tickets -> testContext.verify(() -> {
					assertEquals(1, tickets.size());
//					assertNull(tickets.get(0).getGameId());
				}))
				.compose(v -> client1.cancelMatchmaking())
				.compose(v -> {
					var ticketsDao = new MatchmakingTicketsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
					return ticketsDao.findManyByUserId(Collections.singletonList(client1.getUserEntity().getId()));
				})
				.onSuccess(tickets -> testContext.verify(() -> {
					assertEquals(0, tickets.size());
				}))
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(client1::close)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testMultiplayerPlayerQueueCancel(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var client3 = new Client(vertx);
		var gameCreated = testContext.checkpoint();

		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking() {
			@Override
			public Future<Long> createGame(ReactiveClassicGenericQueryExecutor connection, MatchmakingQueues configuration, MatchmakingTickets... tickets) {
				testContext.verify(() -> {
					assertEquals(2, tickets.length);
					assertEquals(queueId, configuration.getId());
					var ticket2 = Arrays.stream(tickets).filter(t -> t.getUserId().equals(client2.getUserEntity().getId())).findFirst().orElseThrow(AssertionError::new);
					var ticket3 = Arrays.stream(tickets).filter(t -> t.getUserId().equals(client3.getUserEntity().getId())).findFirst().orElseThrow(AssertionError::new);
					assertEquals(queueId, ticket2.getQueueId());
					assertEquals(queueId, ticket3.getQueueId());
					gameCreated.flag();
				});
				return super.createGame(connection, configuration, tickets);
			}
		};

		vertx.deployVerticle(matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.compose(v -> CompositeFuture.join(client1.createAndLogin(), client2.createAndLogin(), client3.createAndLogin()))
				.compose(v1 -> {
					client1.matchmake(queueId);
					return Environment.sleep(vertx, 5000).compose(v2 -> client1.cancelMatchmaking());
				})
				.compose(v -> CompositeFuture.join(client2.matchmake(queueId), client3.matchmake(queueId)))
				.onComplete(client1::close)
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(client2::close)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testClientFailureMultiplayerQueueCancel(Vertx vertx, VertxTestContext testContext) {
		var client = new ToxiClient(vertx);
		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking();
		vertx.deployVerticle(matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.compose(v -> client.createAndLogin())
				.compose(v -> {
					client.matchmake(queueId);
					return Environment.sleep(vertx, 4000);
				})
				.onSuccess(v -> toxicGrpcProxy().setConnectionCut(true))
				.compose(v -> {
					var ticketsDao = new MatchmakingTicketsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
					return ticketsDao.findManyByUserId(Collections.singletonList(client.getUserEntity().getId()));
				})
				.onSuccess(tickets -> {
					testContext.verify(() -> {
						assertEquals(1, tickets.size(), "still connected and should have ticket");
					});
				})
				.compose(v -> Environment.sleep(vertx, 20001))
				.compose(v -> {
					var ticketsDao = new MatchmakingTicketsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
					return ticketsDao.findManyByUserId(Collections.singletonList(client.getUserEntity().getId()));
				})
				.onSuccess(tickets -> {
					testContext.verify(() -> {
						assertEquals(0, tickets.size(), "disconnected and should have timed out");
					});
				})
				.onComplete(client::close)
				.onComplete(v -> toxicGrpcProxy().setConnectionCut(false))
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testDeploysMultipleMatchmakingInstances(Vertx vertx, VertxTestContext testContext) {
		var vertxInternal = (VertxInternal) vertx;
		var currentMatchmakingCount = matchmakingCount(vertxInternal);
		vertx.deployVerticle(Matchmaking.class, new DeploymentOptions()
				.setInstances(2))
				.onSuccess(x -> {
					testContext.verify(() -> {
						assertEquals(2L, matchmakingCount(vertxInternal) - currentMatchmakingCount, "should have deployed two instances");
					});
				})
				.onComplete(testContext.succeedingThenComplete());
	}

	private long matchmakingCount(VertxInternal vertxInternal) {
		return vertxInternal.deploymentIDs()
				.stream()
				.map(vertxInternal::getDeployment)
				.flatMap(f -> f.getVerticles().stream())
				.filter(v -> v instanceof Matchmaking).count();
	}

	@Test
	public void testUserCanOnlyMatchmakeIntoOneActiveGame(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var queueId = UUID.randomUUID().toString();
		vertx.deployVerticle(new Matchmaking())
				.compose(v -> Matchmaking.createQueue(createSinglePlayerQueue(queueId)))
				.compose(v -> client.createAndLogin())
				.compose(v -> client.matchmake(queueId).onFailure(Throwable::printStackTrace))
				.compose(response -> {
					var gameId = response.getUnityConnection().getGameId();
					testContext.verify(() -> {
						assertNotNull(gameId);
					});
					return client.matchmake(queueId)
							.onFailure(Throwable::printStackTrace)
							.onSuccess(res2 -> {
								testContext.verify(() -> {
									assertEquals(gameId, res2.getUnityConnection().getGameId(), "game IDs should match because matchmaking repeatedly when a game is still active should return the same game.");
								});
							});
				})
				.onComplete(client::close)
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testManyClientsMatchmakeAcrossInstances(Vertx vertx, VertxTestContext testContext) {
		// dedicated clients vertx
		var clientVertx = Vertx.vertx();
		var queueIds = IntStream
				.of(0, 4)
				.mapToObj(i -> UUID.randomUUID().toString())
				.collect(toList());

		var random = new Random();
		var serverConfiguration = Environment.cachedConfigurationOrGet();
		// deploy queue runners
		all(queueIds
				.stream()
				.map(queueId -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.collect(toList()))
				// deploy verticles
				.compose(v -> vertx.deployVerticle(Matchmaking.class, new DeploymentOptions()
						.setInstances(4)))
				// create clients
				.compose(v1 -> CompositeFuture.join(IntStream
						.range(0, 400)
						.mapToObj(i -> {
							var client = new Client(clientVertx);
							var queueId = queueIds.get(i % queueIds.size());
							return client.createAndLogin()
									.compose(v -> {
										var scanFrequencyMillis = serverConfiguration.getMatchmaking().getScanFrequencyMillis();
										return Environment.sleep(clientVertx, scanFrequencyMillis / 2 + random.nextInt((int) scanFrequencyMillis * 2));
									})
									.compose(v -> client.matchmake(queueId))
									.map(res -> client);
						})
						.collect(toList())))
				.compose(clientsFut -> {
					var clients = clientsFut.<Client>list();
					var userIds = clients.stream().map(client -> client.getUserEntity().getId()).sorted().toArray(String[]::new);
					var gameUsersDao = new GameUsersDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
					return gameUsersDao.findManyByUserId(Arrays.asList(userIds))
							.onSuccess(gameUsers -> {
								testContext.verify(() -> {
									for (var ticket : gameUsers) {
										assertNotNull(ticket.getGameId(), "should have assigned game");
									}
									assertArrayEquals(gameUsers.stream().map(GameUsers::getUserId).sorted().toArray(String[]::new), userIds, "every user ID should appear");
								});
							})
							.map(clientsFut);
				})
				// close all clients
				.compose(clientsFut -> all(clientsFut.<Client>list().stream().map(client -> client.close((Object) null)).collect(toList())))
				// delete all queues
				.compose(v -> all(queueIds.stream().map(Matchmaking::deleteQueue).collect(toList())))
				.onComplete(v -> clientVertx.close())
				.onComplete(testContext.succeedingThenComplete());

	}

	@NotNull
	private MatchmakingQueues createSinglePlayerQueue(String queueId) {
		return new MatchmakingQueues()
				.setId(queueId)
				.setAutomaticallyClose(false)
				.setLobbySize(1)
				.setAwaitingLobbyTimeout(0L)
				.setBotOpponent(true)
				.setEmptyLobbyTimeout(0L)
				.setName("single player test")
				.setPrivateLobby(false)
				.setOnce(false)
				.setStartsAutomatically(true)
				.setStillConnectedTimeout(0L);
	}

	private MatchmakingQueues createMultiplayerQueue(String queueId) {
		return new MatchmakingQueues()
				.setId(queueId)
				.setAutomaticallyClose(false)
				.setLobbySize(2)
				.setAwaitingLobbyTimeout(0L)
				.setBotOpponent(false)
				.setEmptyLobbyTimeout(0L)
				.setName("multiplayer test")
				.setPrivateLobby(false)
				.setOnce(false)
				.setStartsAutomatically(true)
				.setStillConnectedTimeout(0L);
	}
}
