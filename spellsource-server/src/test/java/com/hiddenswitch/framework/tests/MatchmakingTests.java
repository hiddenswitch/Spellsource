package com.hiddenswitch.framework.tests;

import com.google.protobuf.Empty;
import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.Matchmaking;
import com.hiddenswitch.framework.impl.Clustered;
import com.hiddenswitch.framework.impl.ClusteredGames;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import org.infinispan.manager.DefaultCacheManager;
import com.hiddenswitch.framework.rpc.Hiddenswitch;
import com.hiddenswitch.framework.schema.spellsource.enums.GameStateEnum;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.GamesDao;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.GameUsersDao;
import com.hiddenswitch.framework.schema.spellsource.tables.daos.MatchmakingTicketsDao;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.GameUsers;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingQueues;
import com.hiddenswitch.framework.schema.spellsource.tables.pojos.MatchmakingTickets;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.framework.tests.impl.ToxiClient;
import com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutRequest;
import com.hiddenswitch.spellsource.rpc.Spellsource.ServerToClientMessage;
import io.vertx.core.*;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.streams.WriteStream;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.logic.GameLogic;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static com.hiddenswitch.framework.tests.impl.FrameworkTestBase.Checkpoint.awaitCheckpoints;
import static com.hiddenswitch.framework.tests.impl.FrameworkTestBase.Checkpoint.checkpoint;
import static io.vertx.await.Async.await;
import static io.vertx.await.Async.vt;
import static io.vertx.core.Future.all;
import static io.vertx.core.Future.join;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
public class MatchmakingTests extends FrameworkTestBase {
	private static Logger LOGGER = LoggerFactory.getLogger(MatchmakingTests.class);
	private final List<Future<Vertx>> ownedVertices = new ArrayList<>();
	private Hiddenswitch.ServerConfiguration originalClusterTestConfiguration;

	@AfterEach
	public void closeClusterTestVertices() throws Exception {
		try {
			// These instances are created by the test, so VertxExtension does not
			// close them when the test times out before its future chain finishes.
			var closes = ownedVertices.stream()
					.map(created -> created.compose(Vertx::close)).toList();
			if (!closes.isEmpty()) {
				join(closes).toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
			}
		} finally {
			if (originalClusterTestConfiguration != null) {
				Environment.setConfiguration(originalClusterTestConfiguration);
				System.getProperties().remove(GameLogic.GAMES_TURN_TIME_MILLIS);
			}
		}
	}

	protected Future<Void> startServices(Vertx vertx, Matchmaking matchmaker) {
		return startGateway(vertx)
				.compose(v -> vertx.deployVerticle(matchmaker, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(v -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.mapEmpty();
	}


	protected Future<Void> startServices(Vertx vertx) {
		return startServices(vertx, new Matchmaking());
	}

	@Test
	public void testNoSuchQueueExists(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		startServices(vertx)
				.compose(v -> client.createAndLogin())
				.compose(ignored -> client.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var matchmaking = client.matchmaking();
					var commandsFut = Promise.<WriteStream<MatchmakingQueuePutRequest>>promise();
					var responseFut = matchmaking.enqueue(commandsFut::complete);
					var ended = Promise.<Void>promise();
					responseFut.onSuccess(response -> {
						response.endHandler(ended::complete);
						response.handler(f -> testContext.failNow("should not receive any queue messages, should just fail immediately"));
					});
					responseFut.onFailure(testContext::failNow);

					commandsFut.future().compose(commands -> commands.write(MatchmakingQueuePutRequest.newBuilder()
							.setQueueId("nonexistent queue")
							.setDeckId(decks.getDecks(0).getCollection().getId())
							.build()));
					// silently closes
					return ended.future();
				})
				.compose(v -> client.closeFut())
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testNoDeckIdSpecified(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var queueId = UUID.randomUUID().toString();
		startServices(vertx)
				.compose(v -> client.createAndLogin())
				.compose(v -> Matchmaking.createQueue(createSinglePlayerQueue(queueId)))
				.compose(ignored -> {
					var matchmaking = client.matchmaking();
					var commandsFut = Promise.<WriteStream<MatchmakingQueuePutRequest>>promise();
					var responseFut = matchmaking.enqueue(commandsFut::complete);
					var ended = Promise.<Void>promise();
					responseFut.onSuccess(response -> {
						response.endHandler(ended::complete);
						response.handler(f -> testContext.failNow("should not receive any queue messages, should just fail immediately"));
					});
					responseFut.onFailure(testContext::failNow);
					commandsFut.future().compose(commands -> commands.write(MatchmakingQueuePutRequest.newBuilder()
							.setQueueId(queueId)
							.setDeckId("1")
							.build()));
					return ended.future();
				})
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.compose(v -> client.closeFut())
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testCreateQueueNoExceptions(Vertx vertx, VertxTestContext testContext) {
		var queueId = UUID.randomUUID().toString();
		startServices(vertx)
				.compose(v -> Matchmaking.createQueue(createSinglePlayerQueue(queueId)))
				.compose(v -> Environment.sleep(vertx, 2 * Environment.getConfiguration().getMatchmaking().getScanFrequencyMillis()))
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
	public void testQuickPlay(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		startServices(vertx)
				.compose(v -> client.createAndLogin())
				.compose(v -> client.matchmake("quickPlay"))
				.compose(v -> client.playUntilGameOver())
				.compose(v -> client.closeFut())
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
	public void testTwoGamesInRow(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		startServices(vertx)
				.compose(v -> client.createAndLogin())
				.compose(v -> client.matchmake("quickPlay"))
				.compose(v -> client.playUntilGameOver())
				.compose(v -> Environment.sleep(2000))
				.compose(v -> client.matchmake("quickPlay"))
				.compose(v -> client.playUntilGameOver())
				.compose(v -> client.closeFut())
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
	public void testTwoGamesInRowSomeoneInConstructed(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		startServices(vertx)
				.compose(v -> client1.createAndLogin())
				.compose(v -> client2.createAndLogin())
				.onSuccess(v -> client2.matchmake("constructed"))
				.compose(v -> Environment.sleep(2000))
				.compose(v -> client1.matchmake("quickPlay"))
				.compose(v -> client1.playUntilGameOver())
				.compose(v -> Environment.sleep(2000))
				.compose(v -> client1.matchmake("quickPlay"))
				.compose(v -> client1.playUntilGameOver())
				.compose(v -> client1.closeFut())
				.compose(v -> client2.cancelMatchmaking())
				.compose(v -> client2.closeFut())
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testSinglePlayerQueueCreatesMatch(VertxTestContext testContext) throws InterruptedException {
		var vertx = Vertx.vertx();
		var gameCreated = checkpoint(1);
		var client = new Client(vertx);

		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking() {
			@Override
			public Future<String> createGame(MatchmakingQueues configuration, MatchmakingTickets... tickets) {
				testContext.verify(() -> {
					assertEquals(1, tickets.length);
					assertEquals(client.getUserEntity().getId(), tickets[0].getUserId());
					gameCreated.flag();
				});
				return super.createGame(configuration, tickets);
			}
		};

		var fut = startServices(vertx, matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createSinglePlayerQueue(queueId)))
				.compose(v -> client.createAndLogin())
				.compose(v -> client.matchmake(queueId))
				.onSuccess(shouldFindGame -> {
					testContext.verify(() -> {
						assertNotNull(shouldFindGame);
						assertTrue(shouldFindGame.hasUnityConnection());
					});
				})
				.compose(v -> client.playUntilGameOver())
				.onSuccess(gameOverMessage -> {
					testContext.verify(() -> {
						assertTrue(gameOverMessage.getGameOver().hasWinningPlayerId());
					});
				})
				.compose(v -> client.closeFut())
				.compose(v -> awaitCheckpoints(gameCreated))
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.compose(v -> vertx.close());
		// workaround for RejectedExecutionException
		while (!fut.isComplete()) {
			Thread.sleep(2000L);
		}
		if (fut.failed()) {
			testContext.failNow(fut.cause());
			return;
		}
		testContext.completeNow();
	}

	@Test
	public void testMultiplayerQueueCreatesMatch(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var gameCreated = checkpoint(1);

		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking() {
			@Override
			public Future<String> createGame(MatchmakingQueues configuration, MatchmakingTickets... tickets) {
				testContext.verify(() -> {
					assertEquals(2, tickets.length);
					assertEquals(queueId, configuration.getId());
					gameCreated.flag();
				});
				return super.createGame(configuration, tickets);
			}
		};

		startServices(vertx, matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.compose(v -> client1.createAndLogin())
				.compose(v -> client2.createAndLogin())
				.compose(v -> all(client1.matchmake(queueId), client2.matchmake(queueId)))
				.compose(v -> all(client1.playUntilGameOver(), client2.playUntilGameOver()).map(fut -> fut.<ServerToClientMessage>resultAt(0)))
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.compose(v -> client1.closeFut())
				.compose(v -> client2.closeFut())
				.compose(v -> awaitCheckpoints(gameCreated))
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testWaitsInMultiplayerQueue(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var cancelled = checkpoint(1);
		var queueId = UUID.randomUUID().toString();
		var ticketsDao = new MatchmakingTicketsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());

		var matchmakingQueue = new Matchmaking() {
			@Override
			public Future<String> createGame(MatchmakingQueues configuration, MatchmakingTickets... tickets) {
				testContext.failNow("should not create game");
				return super.createGame(configuration, tickets);
			}
		};

		startServices(vertx, matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.compose(v -> client.createAndLogin())
				.compose(v -> {
					client.matchmake(queueId).onSuccess(res -> {
						testContext.verify(() -> {
							assertNull(res);
							cancelled.flag();
						});
					});
					return Environment.sleep(vertx, 5000);
				})
				.onSuccess(v -> testContext.verify(() -> {
					assertFalse(client.matchmakingResponse().succeeded());
					assertFalse(client.matchmakingResponse().failed());
				}))
				.compose(v -> ticketsDao.findOneById(client.getUserEntity().getId()))
				.onSuccess(ticket -> testContext.verify(() -> {
					assertNotNull(ticket);
				}))
				.compose(v -> client.cancelMatchmaking())
				// TODO: figure out why we still need to sleep a tiny bit
				.compose(v -> Environment.sleep(vertx, 200L))
				.compose(v -> ticketsDao.findOneById(client.getUserEntity().getId()))
				.onSuccess(ticket -> testContext.verify(() -> {
					assertNull(ticket);
				}))
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.compose(v -> client.closeFut())
				.compose(v -> awaitCheckpoints(cancelled))
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testMultiplayerPlayerQueueCancel(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var client3 = new Client(vertx);
		var gameCreated = checkpoint(1);

		var queueId = UUID.randomUUID().toString();
		var matchmakingQueue = new Matchmaking() {
			@Override
			public Future<String> createGame(MatchmakingQueues configuration, MatchmakingTickets... tickets) {
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

		startServices(vertx, matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.compose(v -> join(client1.createAndLogin(), client2.createAndLogin(), client3.createAndLogin()))
				.compose(v1 -> {
					client1.matchmake(queueId);
					return Environment.sleep(vertx, 5000).compose(v2 -> client1.cancelMatchmaking());
				})
				.compose(v -> all(client2.matchmake(queueId), client3.matchmake(queueId)))
				.compose(v -> all(client2.playUntilGameOver(), client3.playUntilGameOver()))
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.compose(v -> awaitCheckpoints(gameCreated))
				.compose(client1::close)
				.compose(client2::close)
				.compose(client3::close)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 90, timeUnit = TimeUnit.SECONDS)
	public void testClientFailureMultiplayerQueueCancel(Vertx vertx, VertxTestContext testContext) {
		var client = new ToxiClient(vertx);
		var queueId = UUID.randomUUID().toString();
		startServices(vertx)
				.compose(v -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.compose(v -> client.createAndLogin())
				.compose(v -> {
					client.matchmake(queueId);
					return Environment.sleep(vertx, Environment.getConfiguration().getMatchmaking().getScanFrequencyMillis() + 1000);
				})
				.compose(v -> {
					var ticketsDao = new MatchmakingTicketsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
					return waitForTicket(vertx, ticketsDao, client.getUserEntity().getId(), true,
							System.nanoTime() + TimeUnit.SECONDS.toNanos(10));
				})
				.onSuccess(ticket -> {
					testContext.verify(() -> {
						assertNotNull(ticket, "still connected and should have ticket");
					});
				})
				.onSuccess(v -> {
					try {
						// Zero bandwidth means unlimited traffic in Toxiproxy 2.13+.
						// Timeout zero explicitly drops traffic without closing the socket.
						toxicGrpcProxy().toxics().timeout("matchmaking-offline-down", eu.rekawek.toxiproxy.model.ToxicDirection.DOWNSTREAM, 0);
						toxicGrpcProxy().toxics().timeout("matchmaking-offline-up", eu.rekawek.toxiproxy.model.ToxicDirection.UPSTREAM, 0);
					} catch (IOException error) { throw new RuntimeException(error); }
				})
				.compose(v -> {
					var ticketsDao = new MatchmakingTicketsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
					// Heartbeat expiry and database cleanup run asynchronously. A fixed sleep
					// races their completion on a busy CI worker; still require bounded cleanup.
					return waitForTicket(vertx, ticketsDao, client.getUserEntity().getId(), false,
							System.nanoTime() + TimeUnit.SECONDS.toNanos(45));
				})
				.onSuccess(ticket -> {
					testContext.verify(() -> {
						assertNull(ticket, "disconnected and should have timed out");
					});
				})
				.compose(v -> client.closeFut().otherwiseEmpty())
				.onComplete(v -> {
					var containerProxy = toxicGrpcProxy();
					if (containerProxy == null) {
						return;
					}
					try {
						for (var name : List.of("matchmaking-offline-down", "matchmaking-offline-up")) {
							var toxic = containerProxy.toxics().get(name);
							if (toxic != null) toxic.remove();
						}
					} catch (IOException error) { throw new RuntimeException(error); }
				})
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(testContext.succeedingThenComplete());
	}

	private Future<MatchmakingTickets> waitForTicket(Vertx vertx, MatchmakingTicketsDao tickets,
	                                                       String userId, boolean present, long deadline) {
		return tickets.findOneById(userId).compose(ticket -> {
			if ((ticket != null) == present || System.nanoTime() >= deadline) return Future.succeededFuture(ticket);
			return Environment.sleep(vertx, 250)
					.compose(ignored -> waitForTicket(vertx, tickets, userId, present, deadline));
		});
	}

	@Test
	public void testDeploysMultipleMatchmakingInstances(Vertx vertx, VertxTestContext testContext) {
		var vertxInternal = (VertxInternal) vertx;
		var currentMatchmakingCount = matchmakingCount(vertxInternal);
		vertx.deployVerticle(Matchmaking.class, new DeploymentOptions()
						.setThreadingModel(ThreadingModel.VIRTUAL_THREAD)
						.setInstances(2))
				.onSuccess(x -> {
					testContext.verify(() -> {
						assertEquals(2L, matchmakingCount(vertxInternal) - currentMatchmakingCount, "should have deployed two instances");
					});
				})
				.onComplete(testContext.succeedingThenComplete());
	}

	private long matchmakingCount(VertxInternal vertxInternal) {
		return vertxInternal.deploymentManager().deployments()
				.stream()
				.flatMap(dc -> dc.deployment().instances().stream())
				.filter(deployable -> deployable instanceof Matchmaking).count();
	}

	@Test
	public void testUserCanOnlyMatchmakeIntoOneActiveGame(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var queueId = UUID.randomUUID().toString();
		startServices(vertx)
				.compose(v -> Matchmaking.createQueue(createSinglePlayerQueue(queueId)))
				.compose(v -> client.createAndLogin())
				.compose(v -> client.matchmake(queueId).onFailure(Throwable::printStackTrace))
				.compose(response -> {
					var gameId = response.getUnityConnection().getGameId();
					testContext.verify(() -> {
						assertNotNull(gameId);
					});

					return client.matchmake(queueId)
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

	@ParameterizedTest()
	@Timeout(value = 210, timeUnit = TimeUnit.SECONDS)
	@ValueSource(ints = {24})
	public void testManyClientsMatchmakeAcrossInstances(int clientsToDeploy, VertxTestContext testContext) {
		testManyClientsMatchmakeAcrossInstances(testContext, 1, clientsToDeploy);
	}

	@ParameterizedTest()
	@Timeout(value = 210, timeUnit = TimeUnit.SECONDS)
	@ValueSource(ints = {60})
	public void testManyClientsMatchmakeAcrossClusteredInstances(int clientsToDeploy, VertxTestContext testContext) {
		testManyClientsMatchmakeAcrossInstances(testContext, 2, clientsToDeploy);
	}

	public void testManyClientsMatchmakeAcrossInstances(VertxTestContext testContext, int serverVertices, int clientsToDeploy) {
		var configuration = Environment.getConfiguration();
		originalClusterTestConfiguration = configuration;
		// client configuration
		var verticesToDeploy = (int) Math.ceil((double) clientsToDeploy / ((double) CpuCoreSensor.availableProcessors()));
		// dedicated clients vertx
		var queueIds = IntStream
				.range(0, 1)
				.mapToObj(i -> UUID.randomUUID().toString())
				.toList();
		var clientVertices = IntStream
				.range(0, verticesToDeploy)
				.mapToObj(i -> Vertx.vertx(new VertxOptions().setEventLoopPoolSize(CpuCoreSensor.availableProcessors())))
				.peek(vertx -> ownedVertices.add(Future.succeededFuture(vertx)))
				.toList();

		System.setProperty(GameLogic.GAMES_TURN_TIME_MILLIS, "6000");
		var defaultConfiguration = Hiddenswitch.ServerConfiguration.newBuilder(Environment.getConfiguration()).build();
		var originalPort = configuration.getGrpcConfiguration().getPort();
		// server configuration
		var vertexFuts = join(IntStream.range(0, serverVertices)
				.mapToObj(v -> {
					var thisConfiguration = Hiddenswitch.ServerConfiguration.newBuilder(defaultConfiguration);
					if (serverVertices >= 2) {
						var infinispanPort = 0;
						thisConfiguration.setVertx(Hiddenswitch.ServerConfiguration.VertxConfiguration.newBuilder()
								.setUseInfinispanClusterManager(true)
								.setInfinspanPort(infinispanPort)
								.build());
						Environment.setConfiguration(thisConfiguration.build());
						var cacheManager = Clustered.infinispanClusterManagerUdp(infinispanPort);
						return Vertx.builder()
								.with(Environment.vertxOptions())
								.withClusterManager(new InfinispanClusterManager(cacheManager))
								.buildClustered();
					} else {
						Environment.setConfiguration(thisConfiguration.build());
						return Future.succeededFuture(Vertx.vertx(Environment.vertxOptions()));
					}
				}).peek(ownedVertices::add).toList());

		vertexFuts
				.compose(vertices -> join(vertices.<Vertx>list().stream().map(vertx -> {
					if (serverVertices == 1) {
						return vertices;
					}

					return vt(vertx, () -> {
						// wait for the infinispan membership to be correct
						var clusterManager = (InfinispanClusterManager) ((VertxInternal) vertx).clusterManager();

						while (clusterManager.getNodes().size() != serverVertices) {
							await(Environment.sleep(vertx, 1000));
						}
						return vertx;
					});
				}).toList()).compose(v -> vertices))
				.compose(vertices -> join(IntStream.range(0, vertices.size()).mapToObj(i -> {
					var vertx = vertices.<Vertx>resultAt(i);
					return startGateway(vertx, originalPort + i)
							.compose(v -> all(queueIds
									.stream()
									.map(queueId -> {
										var createQueue = Promise.<Closeable>promise();
										vertx.runOnContext(v1 -> Matchmaking.createQueue(createMultiplayerQueue(queueId)).onComplete(createQueue));
										return createQueue.future();
									})
									.collect(toList())))
							// deploy verticles
							.compose(v -> vertx.deployVerticle(Matchmaking.class, new DeploymentOptions()
									.setThreadingModel(ThreadingModel.VIRTUAL_THREAD)
									.setInstances(1)))
							.compose(v -> vertx.deployVerticle(ClusteredGames.class, new DeploymentOptions()
									.setThreadingModel(ThreadingModel.VIRTUAL_THREAD)
									.setInstances(CpuCoreSensor.availableProcessors())))
							.map(vertx);
				}).toList()))
				.compose(vertices -> {
					if (serverVertices <= 1) {
						return Future.succeededFuture();
					}
					// assert they're actually clustered
					testContext.verify(() -> {
						assertEquals(serverVertices, ((VertxInternal) vertices.resultAt(0)).clusterManager().getNodes().size());
					});
					LOGGER.error("verticles ready");
					return Future.succeededFuture();
				})
				// create clients
				.compose(v1 -> all(IntStream
						.range(0, clientsToDeploy)
						.mapToObj(i -> {
							var clientVertx = clientVertices.get(i % clientVertices.size());
							var clientActor = new TestClientVerticle(queueIds.get(i % queueIds.size()), originalPort + (i % serverVertices));
							return clientVertx.deployVerticle(clientActor, new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)).map(v -> clientActor.getClient());
						})
						.collect(toList())))
				.compose(clientsFut -> {
					var clients = clientsFut.<Client>list();
					var userIds = clients.stream().map(client -> client.getUserEntity().getId()).sorted().toArray(String[]::new);
					var gameUsersDao = new GameUsersDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
					return gameUsersDao.findManyByUserId(Arrays.asList(userIds))
							.onSuccess(gameUsers -> {
								testContext.verify(() -> {
									for (var gameUser : gameUsers) {
										assertNotNull(gameUser.getGameId(), "should have assigned game");
									}
									assertArrayEquals(userIds, gameUsers.stream().map(GameUsers::getUserId).sorted().toArray(String[]::new), "every user ID should appear");
								});
							})
							.map(clientsFut);
				})
				// close all clients
				.compose(clientsFut -> {
					LOGGER.debug("closing all the clients");
					for (var client : clientsFut.<Client>list()) {
						client.close();
					}
					return Future.succeededFuture();
				})
				// delete all queues
				.eventually(() -> all(queueIds.stream().map(Matchmaking::deleteQueue).collect(toList())))
				.eventually(() -> {
					var closed = new ArrayList<Future<Void>>();
					for (var clientVertx : clientVertices) {
						closed.add(clientVertx.close());
					}
					return join(closed);
				})
				.eventually(() -> {
					var closed = new ArrayList<Future<Void>>();
					if (vertexFuts.succeeded()) {
						for (var vertx : vertexFuts.<Vertx>list()) {
							var clusterManager = (InfinispanClusterManager) ((VertxInternal) vertx).clusterManager();
							var closeFut = vertx.close();
							if (clusterManager != null) {
								var cacheManager = ((DefaultCacheManager) clusterManager.getCacheContainer());
								if (cacheManager != null) {
									closeFut = closeFut.andThen(w -> {
										try {
											cacheManager.close();
										} catch (IOException e) {
											// ignore
										}
									});
								}
							}
							closed.add(closeFut);
						}
					}
					System.getProperties().remove(GameLogic.GAMES_TURN_TIME_MILLIS);
					Environment.setConfiguration(defaultConfiguration);
					return join(closed);
				})
				.onComplete(testContext.succeedingThenComplete());
	}

	@RepeatedTest(5)
	public void testBotQueueWorksAfterDisconnect(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var queueId = UUID.randomUUID().toString();
		System.getProperties().setProperty(Games.GAMES_DEFAULT_NO_ACTIVITY_TIMEOUT, "4000");
		startServices(vertx)
				.compose(v -> Matchmaking.createQueue(createSinglePlayerQueue(queueId)))
				.compose(v -> client.createAndLogin())
				.compose(v -> client.matchmake(queueId))
				.compose(response -> {
					var gameId1 = response.getUnityConnection().getGameId();
					return client.connectToGame()
							.compose(v -> Environment.sleep(vertx, 4000L * 2))
							.compose(v -> new GamesDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient()).findOneById(Long.valueOf(gameId1)))
							.compose(game -> {
								testContext.verify(() -> {
									assertEquals(GameStateEnum.STARTED, game.getStatus(), "bot matches should remain active after inactivity");
								});
								return Future.succeededFuture();
							});
				})
				.onComplete(client::close)
				.onComplete(v -> {
					// todo: store the actual default somewhere
					System.getProperties().setProperty(Games.GAMES_DEFAULT_NO_ACTIVITY_TIMEOUT, "225000");
				})
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 90, timeUnit = TimeUnit.SECONDS)
	public void testUngracefulAppCloseDoesNotMatchmakeDeadPlayer(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var client3 = new Client(vertx);
		var gameCreated = checkpoint(1);
		var queueId = UUID.randomUUID().toString();
		var ticketsDao = new MatchmakingTicketsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
		var gameUsersDao = new GameUsersDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());

		var matchmakingQueue = new Matchmaking() {
			@Override
			public Future<String> createGame(MatchmakingQueues configuration, MatchmakingTickets... tickets) {
				testContext.verify(() -> {
					assertEquals(2, tickets.length);
					assertEquals(queueId, configuration.getId());
					assertFalse(Arrays.stream(tickets).anyMatch(t -> t.getUserId().equals(client1.getUserEntity().getId())),
							"dead player 1 must never be matched after closing app");
					assertTrue(Arrays.stream(tickets).anyMatch(t -> t.getUserId().equals(client2.getUserEntity().getId())),
							"live player 2 must be matched");
					assertTrue(Arrays.stream(tickets).anyMatch(t -> t.getUserId().equals(client3.getUserEntity().getId())),
							"live player 3 must be matched");
					gameCreated.flag();
				});
				return super.createGame(configuration, tickets);
			}
		};

		startServices(vertx, matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.compose(v -> join(client1.createAndLogin(), client2.createAndLogin(), client3.createAndLogin()))
				.compose(v -> {
					client1.matchmake(queueId);
					return waitForTicket(vertx, ticketsDao, client1.getUserEntity().getId(), true,
							System.nanoTime() + TimeUnit.SECONDS.toNanos(10));
				})
				.onSuccess(ticket -> testContext.verify(() -> assertNotNull(ticket, "player 1 ticket should initially be in queue")))
				.compose(v -> {
					// Simulate Player 1 force-closing their app without calling cancelMatchmaking()
					client1.close();
					// Immediately enqueue Player 2 and Player 3 right as Player 1 disconnects
					return all(client2.matchmake(queueId), client3.matchmake(queueId));
				})
				.compose(cf -> {
					var res2 = cf.<com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutResponse>resultAt(0);
					var res3 = cf.<com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutResponse>resultAt(1);
					testContext.verify(() -> {
						assertNotNull(res2);
						assertNotNull(res3);
						assertEquals(res2.getUnityConnection().getGameId(), res3.getUnityConnection().getGameId(),
								"live players 2 and 3 must be matched into the exact same game");
					});
					return all(client2.playUntilGameOver(), client3.playUntilGameOver());
				})
				.compose(v -> waitForTicket(vertx, ticketsDao, client1.getUserEntity().getId(), false,
						System.nanoTime() + TimeUnit.SECONDS.toNanos(10)))
				.onSuccess(ticket -> testContext.verify(() -> assertNull(ticket, "dead player 1 ticket must be purged")))
				.compose(v -> gameUsersDao.findManyByUserId(List.of(client1.getUserEntity().getId())))
				.onSuccess(gameUsers -> testContext.verify(() -> assertTrue(gameUsers.isEmpty(), "dead player 1 must not be in any game")))
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.compose(v -> awaitCheckpoints(gameCreated))
				.compose(client2::close)
				.compose(client3::close)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 90, timeUnit = TimeUnit.SECONDS)
	public void testStaleOrphanedTicketInDatabasePurgedAndLivePlayersMatch(Vertx vertx, VertxTestContext testContext) {
		var deadClient = new Client(vertx);
		var liveClient1 = new Client(vertx);
		var liveClient2 = new Client(vertx);
		var gameCreated = checkpoint(1);
		var queueId = UUID.randomUUID().toString();
		var ticketsDao = new MatchmakingTicketsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
		var gameUsersDao = new GameUsersDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());

		var matchmakingQueue = new Matchmaking() {
			@Override
			public Future<String> createGame(MatchmakingQueues configuration, MatchmakingTickets... tickets) {
				testContext.verify(() -> {
					assertEquals(2, tickets.length);
					assertFalse(Arrays.stream(tickets).anyMatch(t -> t.getUserId().equals(deadClient.getUserEntity().getId())),
							"orphaned ticket must never be matched into a game");
					assertTrue(Arrays.stream(tickets).anyMatch(t -> t.getUserId().equals(liveClient1.getUserEntity().getId())));
					assertTrue(Arrays.stream(tickets).anyMatch(t -> t.getUserId().equals(liveClient2.getUserEntity().getId())));
					gameCreated.flag();
				});
				return super.createGame(configuration, tickets);
			}
		};

		startServices(vertx, matchmakingQueue)
				.compose(v -> Matchmaking.createQueue(createMultiplayerQueue(queueId)))
				.compose(v -> join(deadClient.createAndLogin(), liveClient1.createAndLogin(), liveClient2.createAndLogin()))
				.compose(v -> deadClient.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deadDeckId = decks.getDecks(0).getCollection().getId();
					// Close deadClient completely before inserting a stale/orphaned ticket into DB
					return deadClient.closeFut()
							.compose(v -> ticketsDao.insert(new MatchmakingTickets()
									.setUserId(deadClient.getUserEntity().getId())
									.setQueueId(queueId)
									.setDeckId(deadDeckId)));
				})
				.compose(v -> {
					// Now liveClient1 enters the queue; claimCompleteLobbies will claim (deadClient, liveClient1),
					// detect deadClient is not connected, purge deadClient, and keep liveClient1 queued!
					var matchFut1 = liveClient1.matchmake(queueId);
					return waitForTicket(vertx, ticketsDao, deadClient.getUserEntity().getId(), false,
							System.nanoTime() + TimeUnit.SECONDS.toNanos(10))
							.onSuccess(deadTicket -> testContext.verify(() ->
									assertNull(deadTicket, "stale ticket for deadClient should be purged by liveness check")))
							.compose(ignored -> waitForTicket(vertx, ticketsDao, liveClient1.getUserEntity().getId(), true,
									System.nanoTime() + TimeUnit.SECONDS.toNanos(10)))
							.onSuccess(liveTicket -> testContext.verify(() -> {
								assertNotNull(liveTicket, "liveClient1 should be re-queued and remain waiting");
								assertFalse(matchFut1.isComplete(), "liveClient1 must not have been matched with deadClient");
							}))
							.compose(ignored -> all(matchFut1, liveClient2.matchmake(queueId)));
				})
				.compose(cf -> {
					var res1 = cf.<com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutResponse>resultAt(0);
					var res2 = cf.<com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutResponse>resultAt(1);
					testContext.verify(() -> {
						assertNotNull(res1);
						assertNotNull(res2);
						assertEquals(res1.getUnityConnection().getGameId(), res2.getUnityConnection().getGameId(),
								"liveClient1 and liveClient2 must match together after deadClient was purged");
					});
					return all(liveClient1.playUntilGameOver(), liveClient2.playUntilGameOver());
				})
				.compose(v -> gameUsersDao.findManyByUserId(List.of(deadClient.getUserEntity().getId())))
				.onSuccess(gameUsers -> testContext.verify(() -> assertTrue(gameUsers.isEmpty(), "deadClient must not appear in game_users")))
				.compose(v -> Matchmaking.deleteQueue(queueId))
				.compose(v -> awaitCheckpoints(gameCreated))
				.compose(liveClient1::close)
				.compose(liveClient2::close)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 180, timeUnit = TimeUnit.SECONDS)
	public void testChaosUngracefulDisconnectsAcrossClusteredInstances(VertxTestContext testContext) {
		int serverVertices = 2;
		int waves = 4; // Each wave has 1 doomed client that ungracefully kills its connection + 2 live clients = 8 live clients + 4 doomed clients
		var configuration = Environment.getConfiguration();
		var queueId = UUID.randomUUID().toString();
		var clientVertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(CpuCoreSensor.availableProcessors()));

		System.setProperty(GameLogic.GAMES_TURN_TIME_MILLIS, "6000");
		var defaultConfiguration = Hiddenswitch.ServerConfiguration.newBuilder(Environment.getConfiguration()).build();
		var originalPort = configuration.getGrpcConfiguration().getPort();

		var vertexFuts = join(IntStream.range(0, serverVertices)
				.mapToObj(v -> {
					var thisConfiguration = Hiddenswitch.ServerConfiguration.newBuilder(defaultConfiguration);
					var infinispanPort = 0;
					thisConfiguration.setVertx(Hiddenswitch.ServerConfiguration.VertxConfiguration.newBuilder()
							.setUseInfinispanClusterManager(true)
							.setInfinspanPort(infinispanPort)
							.build());
					Environment.setConfiguration(thisConfiguration.build());
					var cacheManager = Clustered.infinispanClusterManagerUdp(infinispanPort);
					return Vertx.builder()
							.with(Environment.vertxOptions())
							.withClusterManager(new InfinispanClusterManager(cacheManager))
							.buildClustered();
				}).toList());

		var liveClients = new ArrayList<Client>();
		var doomedClients = new ArrayList<Client>();

		vertexFuts
				.compose(vertices -> join(vertices.<Vertx>list().stream().map(vertx -> vt(vertx, () -> {
					var clusterManager = (InfinispanClusterManager) ((VertxInternal) vertx).clusterManager();
					while (clusterManager.getNodes().size() != serverVertices) {
						await(Environment.sleep(vertx, 500));
					}
					return vertx;
				})).toList()).compose(v -> vertices))
				.compose(vertices -> join(IntStream.range(0, vertices.size()).mapToObj(i -> {
					var vertx = vertices.<Vertx>resultAt(i);
					return startGateway(vertx, originalPort + i)
							.compose(v -> {
								var createQueue = Promise.<Closeable>promise();
								vertx.runOnContext(v1 -> Matchmaking.createQueue(createMultiplayerQueue(queueId)).onComplete(createQueue));
								return createQueue.future();
							})
							.compose(v -> vertx.deployVerticle(Matchmaking.class, new DeploymentOptions()
									.setThreadingModel(ThreadingModel.VIRTUAL_THREAD)
									.setInstances(1)))
							.compose(v -> vertx.deployVerticle(ClusteredGames.class, new DeploymentOptions()
									.setThreadingModel(ThreadingModel.VIRTUAL_THREAD)
									.setInstances(CpuCoreSensor.availableProcessors())))
							.map(vertx);
				}).toList()))
				.compose(vertices -> {
					var createLogins = new ArrayList<Future<?>>();
					for (int w = 0; w < waves; w++) {
						var doomed = new ClientWithPort(clientVertx, originalPort + (w % serverVertices));
						doomedClients.add(doomed);
						createLogins.add(doomed.createAndLogin());

						var liveA = new ClientWithPort(clientVertx, originalPort + (w % serverVertices));
						var liveB = new ClientWithPort(clientVertx, originalPort + ((w + 1) % serverVertices));
						liveClients.add(liveA);
						liveClients.add(liveB);
						createLogins.add(liveA.createAndLogin());
						createLogins.add(liveB.createAndLogin());
					}
					return all(createLogins);
				})
				.compose(v -> {
					var ticketsDao = new MatchmakingTicketsDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
					Future<Void> chain = Future.succeededFuture();
					for (int w = 0; w < waves; w++) {
						final int waveIndex = w;
						chain = chain.compose(ignored -> {
							var doomed = doomedClients.get(waveIndex);
							var liveA = liveClients.get(waveIndex * 2);
							var liveB = liveClients.get(waveIndex * 2 + 1);
							// Doomed client enters matchmaking then ungracefully closes its connection right as live clients join
							doomed.matchmake(queueId);
							return waitForTicket(clientVertx, ticketsDao, doomed.getUserEntity().getId(), true,
									System.nanoTime() + TimeUnit.SECONDS.toNanos(10))
									.compose(t -> {
										doomed.close();
										return all(
												liveA.matchmake(queueId).compose(res -> liveA.playUntilGameOver()),
												liveB.matchmake(queueId).compose(res -> liveB.playUntilGameOver())
										).mapEmpty();
									});
						});
					}
					return chain;
				})
				.compose(v -> {
					var liveUserIds = liveClients.stream().map(c -> c.getUserEntity().getId()).sorted().toArray(String[]::new);
					var doomedUserIds = doomedClients.stream().map(c -> c.getUserEntity().getId()).toList();
					var gameUsersDao = new GameUsersDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
					return gameUsersDao.findManyByUserId(Arrays.asList(liveUserIds))
							.compose(liveGameUsers -> {
								testContext.verify(() -> {
									assertEquals(liveUserIds.length, liveGameUsers.size(), "every live client should be in exactly one game");
									assertArrayEquals(liveUserIds, liveGameUsers.stream().map(GameUsers::getUserId).sorted().toArray(String[]::new));
								});
								return gameUsersDao.findManyByUserId(doomedUserIds);
							})
							.onSuccess(doomedGameUsers -> testContext.verify(() ->
									assertTrue(doomedGameUsers.isEmpty(), "zero ungracefully disconnected clients should ever be matched into a game")));
				})
				.eventually(() -> {
					for (var c : liveClients) {
						c.close();
					}
					for (var c : doomedClients) {
						c.close();
					}
					return Matchmaking.deleteQueue(queueId).otherwiseEmpty();
				})
				.eventually(clientVertx::close)
				.eventually(() -> {
					var closed = new ArrayList<Future<Void>>();
					if (vertexFuts.succeeded()) {
						for (var vertx : vertexFuts.<Vertx>list()) {
							var clusterManager = (InfinispanClusterManager) ((VertxInternal) vertx).clusterManager();
							var closeFut = vertx.close();
							if (clusterManager != null) {
								var cacheManager = ((DefaultCacheManager) clusterManager.getCacheContainer());
								if (cacheManager != null) {
									closeFut = closeFut.andThen(w -> {
										try {
											cacheManager.close();
										} catch (IOException ignored) {
										}
									});
								}
							}
							closed.add(closeFut);
						}
					}
					System.getProperties().remove(GameLogic.GAMES_TURN_TIME_MILLIS);
					Environment.setConfiguration(defaultConfiguration);
					return join(closed);
				})
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

	private static class ClientWithPort extends Client {
		protected int port;

		public ClientWithPort(Vertx vertx, int port) {
			super(vertx);
			this.port = port;
		}

		@Override
		protected int port() {
			return port;
		}
	}

	private static class TestClientVerticle extends AbstractVerticle {
		private final String queueId;
		private final int port;
		private Client client;

		public TestClientVerticle(String queueId, int port) {
			this.queueId = queueId;
			this.port = port;
		}

		public Client getClient() {
			return client;
		}

		@Override
		public void start(Promise<Void> startPromise) {
			this.client = new ClientWithPort(this.vertx, port);
			client.createAndLogin()
					.compose(v -> client.matchmake(queueId))
					.compose(v -> client.playUntilGameOver())
					.map((Void) null)
					.onComplete(startPromise);
		}

		@Override
		public void stop(Promise<Void> stopPromise) throws Exception {
			client.close();
			stopPromise.complete();
		}
	}
}
