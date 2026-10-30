package com.hiddenswitch.framework.tests;

import com.google.protobuf.Empty;
import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.impl.Clustered;
import com.hiddenswitch.framework.impl.ClusteredGames;
import com.hiddenswitch.framework.impl.Configuration;
import com.hiddenswitch.framework.impl.ConfigurationRequest;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.impl.ModelConversions;
import com.hiddenswitch.framework.rpc.Hiddenswitch;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.framework.tests.impl.ToxiClient;
import com.hiddenswitch.spellsource.rpc.Spellsource.MessageTypeMessage.MessageType;
import com.hiddenswitch.spellsource.rpc.Spellsource.*;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.Promise;
import io.vertx.core.impl.cpu.CpuCoreSensor;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.cluster.infinispan.InfinispanClusterManager;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.logic.GameLogic;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.hiddenswitch.framework.schema.spellsource.Tables.GAMES;
import static com.hiddenswitch.framework.schema.spellsource.Tables.GAME_USERS;
import static io.vertx.await.Async.await;
import static io.vertx.await.Async.vt;
import static io.vertx.core.Future.all;
import static io.vertx.core.Future.join;
import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.SAME_THREAD)
public class GameTests extends FrameworkTestBase {
	private final List<Future<Vertx>> ownedVertices = new ArrayList<>();
	private Hiddenswitch.ServerConfiguration originalClusterTestConfiguration;

	@AfterEach
	public void cleanupGameTests() throws Exception {
		try {
			var containerProxy = toxicGrpcProxy();
			if (containerProxy != null) {
				for (var toxic : new ArrayList<>(containerProxy.toxics().getAll())) {
					try {
						toxic.remove();
					} catch (IOException ignored) {
					}
				}
			}
			var closes = ownedVertices.stream()
					.map(created -> created.compose(vertx -> {
						var clusterManager = ((VertxInternal) vertx).clusterManager();
						var closeFut = vertx.close();
						if (clusterManager instanceof InfinispanClusterManager infinispan) {
							var cacheManager = (DefaultCacheManager) infinispan.getCacheContainer();
							if (cacheManager != null) {
								closeFut = closeFut.andThen(w -> {
									try {
										cacheManager.close();
									} catch (IOException ignored) {
									}
								});
							}
						}
						return closeFut;
					})).toList();
			if (!closes.isEmpty()) {
				join(closes).toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
			}
		} finally {
			ownedVertices.clear();
			System.getProperties().remove(GameLogic.GAMES_TURN_TIME_MILLIS);
			System.getProperties().remove(GameLogic.GAMES_MULLIGAN_TIME_MILLIS);
			System.setProperty(Games.GAMES_DEFAULT_NO_ACTIVITY_TIMEOUT, "225000");
			if (originalClusterTestConfiguration != null) {
				Environment.setConfiguration(originalClusterTestConfiguration);
				originalClusterTestConfiguration = null;
			}
		}
	}
	@Test
	public void testConcededPersistedBotMatchCannotResume(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var gameId = new AtomicReference<Long>();
		var clusteredGamesDeployment = new AtomicReference<String>();

		startGateway(vertx)
				.compose(ignored -> client.createAndLogin())
				.compose(ignored -> vertx.deployVerticle(new ClusteredGames(),
						new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.onSuccess(clusteredGamesDeployment::set)
				.compose(ignored -> client.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> Environment.query(dsl -> dsl.insertInto(GAMES).defaultValues().returning(GAMES.ID))
						.compose(rows -> {
							var id = rows.iterator().next().getLong(GAMES.ID.getName());
							gameId.set(id);
							var deckId = decks.getDecks(0).getCollection().getId();
							return Environment.withDslContext(dsl -> dsl.insertInto(GAME_USERS)
											.set(GAME_USERS.GAME_ID, id)
											.set(GAME_USERS.USER_ID, client.getUserEntity().getId())
											.set(GAME_USERS.DECK_ID, deckId)
											.set(GAME_USERS.PLAYER_INDEX, (short) 0))
									.map(ignored -> deckId);
						}))
				.compose(deckId -> Games.createGame(ConfigurationRequest.botMatch(
						gameId.get().toString(),
						client.getUserEntity().getId(),
						client.getUserEntity().getId(),
						deckId,
						deckId)))
				.compose(ignored -> playThroughOneAction(client))
				.compose(ignored -> Environment.sleep(vertx, 250L))
				.compose(ignored -> Games.concedeGame(client.getUserEntity().getId()))
				.onSuccess(conceded -> testContext.verify(() -> assertTrue(conceded)))
				.compose(ignored -> Games.getGameId(client.getUserEntity().getId()))
				.onSuccess(activeGameId -> testContext.verify(() ->
						assertNull(activeGameId, "conceded game must stop being active before concession is acknowledged")))
				.compose(ignored -> Environment.query(dsl -> dsl.select(GAMES.STATUS, GAME_USERS.VICTORY_STATUS)
						.from(GAMES)
						.join(GAME_USERS).on(GAME_USERS.GAME_ID.eq(GAMES.ID))
						.where(GAMES.ID.eq(gameId.get()))))
				.onSuccess(rows -> testContext.verify(() -> {
					var row = rows.iterator().next();
					assertEquals("FINISHED", row.getString(GAMES.STATUS.getName()));
					assertNotEquals("UNKNOWN", row.getString(GAME_USERS.VICTORY_STATUS.getName()));
				}))
				.compose(ignored -> vertx.undeploy(clusteredGamesDeployment.get()))
				.compose(ignored -> vertx.deployVerticle(new ClusteredGames(),
						new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(ignored -> Games.getGameId(client.getUserEntity().getId()))
				.onSuccess(restoredGameId -> testContext.verify(() ->
						assertNull(restoredGameId, "conceded game must not resume after the game host restarts")))
				.eventually(client::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	public void testBotMatchCheckpointsTraceAfterMulliganAndAction(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		startGateway(vertx)
				.compose(ignored -> client.createAndLogin())
				.compose(ignored -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(ignored -> client.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> Environment.query(dsl -> dsl.insertInto(GAMES).defaultValues().returning(GAMES.ID))
						.compose(rows -> {
							var gameId = rows.iterator().next().getLong("id");
							var deckId = decks.getDecks(0).getCollection().getId();
							return Environment.withDslContext(dsl -> dsl.insertInto(GAME_USERS)
									.set(GAME_USERS.GAME_ID, gameId)
									.set(GAME_USERS.USER_ID, client.getUserEntity().getId())
									.set(GAME_USERS.DECK_ID, deckId)
									.set(GAME_USERS.PLAYER_INDEX, (short) 0))
									.map(ignored -> new Object[]{gameId, deckId});
						}))
				.compose(game -> {
					var gameId = (Long) game[0];
					var deckId = (String) game[1];
					return Games.createGame(ConfigurationRequest.botMatch(gameId.toString(), client.getUserEntity().getId(), client.getUserEntity().getId(), deckId, deckId))
							.compose(ignored -> playThroughOneAction(client))
							.compose(ignored -> awaitActionCheckpoint(vertx, gameId,
									System.nanoTime() + java.util.concurrent.TimeUnit.SECONDS.toNanos(10)));
				})
				.onSuccess(trace -> testContext.verify(() -> {
					assertNotNull(trace, "bot match should checkpoint its trace");
					var persisted = net.demilich.metastone.game.logic.Trace.load(trace.encode());
					assertNotNull(persisted.getMulligans(), "mulligan completion should be checkpointed");
					assertFalse(persisted.getActions().isEmpty(), "the resolved player action should be checkpointed");
				}))
				.eventually(client::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	private Future<io.vertx.core.json.JsonObject> awaitActionCheckpoint(Vertx vertx, long gameId, long deadline) {
		return Environment.query(dsl -> dsl.select(GAMES.TRACE).from(GAMES).where(GAMES.ID.eq(gameId)))
				.compose(rows -> {
					var trace = rows.iterator().next().getJsonObject("trace");
					if (trace != null && !net.demilich.metastone.game.logic.Trace.load(trace.encode()).getActions().isEmpty()
							|| System.nanoTime() >= deadline) return Future.succeededFuture(trace);
					return Environment.sleep(vertx, 100).compose(ignored -> awaitActionCheckpoint(vertx, gameId, deadline));
				});
	}

	private Future<Void> playThroughOneAction(Client client) {
		var writer = Promise.<WriteStream<ClientToServerMessage>>promise();
		var actionRequestId = new AtomicReference<String>();
		var completed = Promise.<Void>promise();
		client.legacy().subscribeGame(writer::tryComplete).onSuccess(reader -> {
			reader.exceptionHandler(completed::tryFail);
			reader.handler(message -> {
				var stream = writer.future().result();
				switch (message.getMessageType()) {
					case ON_MULLIGAN -> stream.write(ClientToServerMessage.newBuilder()
							.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(message.getId()).build()).onFailure(completed::tryFail);
					case ON_REQUEST_ACTION -> {
						if (actionRequestId.compareAndSet(null, message.getId())) {
							var endTurn = message.getActions().getAllList().stream()
									.filter(action -> action.getActionType() == ActionTypeMessage.ActionType.END_TURN)
									.findFirst();
							if (endTurn.isEmpty()) {
								completed.tryFail("Expected an end-turn action after mulligan");
								return;
							}
							stream.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.UPDATE_ACTION)
									.setRepliesTo(message.getId()).setActionIndex(endTurn.get().getAction()).build())
									.onFailure(completed::tryFail);
						} else if (!message.getId().equals(actionRequestId.get())) {
							// A new request can arrive while persistence is completing;
							// the caller also waits for the database checkpoint itself.
							completed.tryComplete();
						}
					}
					case ON_GAME_END -> completed.tryComplete();
				}
			});
		}).onFailure(completed::tryFail);
		return writer.future().compose(stream -> stream.write(ClientToServerMessage.newBuilder()
				.setMessageType(MessageType.FIRST_MESSAGE).build()).mapEmpty()).compose(ignored -> completed.future());
	}

	@Test
	public void testCreatesGame(Vertx vertx, VertxTestContext vertxTestContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		startGateway(vertx)
				.compose(v -> client1.createAndLogin())
				.compose(v -> client2.createAndLogin())
				.compose(v -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(v -> client1.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks1 -> {
					Games.createGame(new ConfigurationRequest()
							.setGameId("1")
							// for now, don't time out. just create the game
							.setConfigurations(Arrays.asList(new Configuration()
											.setBot(false)
											.setDeck(Deck.forId(decks1.getDecksList().get(0).getCollection().getId()))
											.setName("test")
											.setPlayerId(0)
											.setUserId(client1.getUserEntity().getId()),
									new Configuration()
											.setBot(false)
											.setDeck(Deck.forId(decks1.getDecksList().get(0).getCollection().getId()))
											.setName("test2")
											.setPlayerId(1)
											.setUserId(client2.getUserEntity().getId()))));

					return Environment.sleep(vertx, 1000);
				})
				.onFailure(vertxTestContext::failNow)
				.compose(res -> Future.all(client1.connectToGame(), client2.connectToGame()).map(fut -> fut.<ServerToClientMessage>resultAt(0)))
				.compose(msg -> {
					vertxTestContext.verify(() -> {
						var validMessageTypes = EnumSet.of(MessageType.ON_UPDATE, MessageType.TIMER);
						assertTrue(validMessageTypes.contains(msg.getMessageType()), "expected to get a valid message when connected, got %s".formatted(msg.getMessageType()));
					});
					return Future.succeededFuture();
				})
				.onComplete(v -> client1.close())
				.onComplete(v -> client2.close())
				.onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testAllDecksHaveValidModels(Vertx vertx, VertxTestContext vertxTestContext) {
		var client = new Client(vertx);
		startGateway(vertx)
				.compose(v -> client.createAndLogin())
				.compose(v -> client.legacy().decksGetAll(Empty.getDefaultInstance()))
				.onSuccess(decksGetAllResponse -> vertxTestContext.verify(() -> {
					assertTrue(decksGetAllResponse.getDecksList().size() > 1);
					for (var deckCollection : decksGetAllResponse.getDecksList()) {
						var gameDeck = ModelConversions.getGameDeck(client.getUserEntity().getId(), deckCollection, ClasspathCardCatalogue.INSTANCE);
						assertEquals(30, gameDeck.getCards().size());
						assertNotNull(gameDeck.getDeckId());
						assertNotNull(gameDeck.getFormat());
						assertNotNull(gameDeck.getHeroClass());

						// test creating the attributes
						var playerAttributes = new AttributeMap();
						for (var tuple : deckCollection.getCollection().getPlayerEntityAttributesList()) {
							playerAttributes.put(Attribute.valueOf(tuple.getAttribute().name()), tuple.getStringValue());
						}
					}
				}))
				.compose(v -> client.closeFut())
				.onComplete(vertxTestContext.succeedingThenComplete());
	}

	private Future<Long> insertMultiplayerGameRecord(String userId1, String deckId1, String userId2, String deckId2) {
		return Environment.query(dsl -> dsl.insertInto(GAMES).defaultValues().returning(GAMES.ID))
				.compose(rows -> {
					var gameId = rows.iterator().next().getLong(GAMES.ID.getName());
					return Environment.withDslContext(dsl -> dsl.insertInto(GAME_USERS)
									.set(GAME_USERS.GAME_ID, gameId)
									.set(GAME_USERS.USER_ID, userId1)
									.set(GAME_USERS.DECK_ID, deckId1)
									.set(GAME_USERS.PLAYER_INDEX, (short) 0))
							.compose(ignored -> Environment.withDslContext(dsl -> dsl.insertInto(GAME_USERS)
									.set(GAME_USERS.GAME_ID, gameId)
									.set(GAME_USERS.USER_ID, userId2)
									.set(GAME_USERS.DECK_ID, deckId2)
									.set(GAME_USERS.PLAYER_INDEX, (short) 1)))
							.map(gameId);
				});
	}

	private ConfigurationRequest multiplayerConfigurationRequest(String gameId, String userId1, String deckId1, String userId2, String deckId2, long noActivityTimeout) {
		return new ConfigurationRequest()
				.setGameId(gameId)
				.setConfigurations(Arrays.asList(
						new Configuration()
								.setBot(false)
								.setDeck(Deck.forId(deckId1))
								.setName("player1")
								.setPlayerId(0)
								.setUserId(userId1)
								.setNoActivityTimeout(noActivityTimeout),
						new Configuration()
								.setBot(false)
								.setDeck(Deck.forId(deckId2))
								.setName("player2")
								.setPlayerId(1)
								.setUserId(userId2)
								.setNoActivityTimeout(noActivityTimeout)));
	}

	@Test
	@Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
	public void testSubscribeGameBeforeCreateGameBuffersFirstMessageViaRetryProducer(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var deckIdRef = new AtomicReference<String>();
		var gameIdRef = new AtomicReference<Long>();

		startGateway(vertx)
				.compose(v -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(v -> join(client1.createAndLogin(), client2.createAndLogin()))
				.compose(v -> client1.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					deckIdRef.set(deckId);
					return insertMultiplayerGameRecord(
							client1.getUserEntity().getId(), deckId,
							client2.getUserEntity().getId(), deckId);
				})
				.compose(gameId -> {
					gameIdRef.set(gameId);
					// Both clients subscribe and send FIRST_MESSAGE BEFORE Games.createGame is called!
					// RetryMessageProducer must retry NO_HANDLERS until ServerGameContext deploys userVerticles.
					var play1 = client1.playUntilGameOver();
					var play2 = client2.playUntilGameOver();
					return Environment.sleep(vertx, 450L)
							.compose(ignored -> Games.createGame(multiplayerConfigurationRequest(
									gameId.toString(),
									client1.getUserEntity().getId(), deckIdRef.get(),
									client2.getUserEntity().getId(), deckIdRef.get(),
									0L)))
							.compose(ignored -> all(play1, play2));
				})
				.onSuccess(cf -> testContext.verify(() -> {
					var end1 = cf.<ServerToClientMessage>resultAt(0);
					var end2 = cf.<ServerToClientMessage>resultAt(1);
					assertEquals(MessageType.ON_GAME_END, end1.getMessageType());
					assertEquals(MessageType.ON_GAME_END, end2.getMessageType());
					assertNotEquals(end1.getGameOver().getLocalPlayerWon(), end2.getGameOver().getLocalPlayerWon(),
							"exactly one player must win the completed game");
				}))
				.eventually(client1::closeFut)
				.eventually(client2::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 90, timeUnit = TimeUnit.SECONDS)
	public void testToxiproxyMidMulliganDisconnectAndReconnectBeforeAndAfterSubmittingMulligan(Vertx vertx, VertxTestContext testContext) {
		var toxiClient1 = new ToxiClient(vertx);
		var client2 = new Client(vertx);
		var writer1Initial = Promise.<WriteStream<ClientToServerMessage>>promise();
		var writer2 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var firstMulligan1 = Promise.<ServerToClientMessage>promise();
		var firstMulligan2 = Promise.<ServerToClientMessage>promise();
		var reconnectedMulligan1 = Promise.<ServerToClientMessage>promise();
		var writer1Reconnected = Promise.<WriteStream<ClientToServerMessage>>promise();
		var writer1PostMulligan = Promise.<WriteStream<ClientToServerMessage>>promise();
		var postMulliganUpdate1 = Promise.<ServerToClientMessage>promise();
		var reconnectedReceivedDuplicateMulligan = new AtomicBoolean(false);
		var turn1Started = Promise.<ServerToClientMessage>promise();

		startGateway(vertx)
				.compose(v -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(v -> join(toxiClient1.createAndLogin(), client2.createAndLogin()))
				.compose(v -> client2.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					return insertMultiplayerGameRecord(
							toxiClient1.getUserEntity().getId(), deckId,
							client2.getUserEntity().getId(), deckId)
							.compose(gameId -> Games.createGame(multiplayerConfigurationRequest(
									gameId.toString(),
									toxiClient1.getUserEntity().getId(), deckId,
									client2.getUserEntity().getId(), deckId,
									0L)));
				})
				.compose(ignored -> {
					toxiClient1.legacy().subscribeGame(writer1Initial::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								firstMulligan1.tryComplete(msg);
							}
						});
					}).onFailure(firstMulligan1::tryFail);

					client2.legacy().subscribeGame(writer2::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								firstMulligan2.tryComplete(msg);
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								turn1Started.tryComplete(msg);
							}
						});
					}).onFailure(firstMulligan2::tryFail);

					return all(
							writer1Initial.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build())),
							writer2.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
					).compose(v -> all(firstMulligan1.future(), firstMulligan2.future()));
				})
				.compose(v -> {
					// Cut toxiClient1 via Toxiproxy BEFORE submitting UPDATE_MULLIGAN
					try {
						toxicGrpcProxy().toxics().resetPeer("mulligan-reset-down", ToxicDirection.DOWNSTREAM, 1);
						toxicGrpcProxy().toxics().resetPeer("mulligan-reset-up", ToxicDirection.UPSTREAM, 1);
					} catch (IOException e) {
						return Future.failedFuture(e);
					}
					return Environment.sleep(vertx, 300L);
				})
				.compose(v -> {
					try {
						for (var name : List.of("mulligan-reset-down", "mulligan-reset-up")) {
							var t = toxicGrpcProxy().toxics().get(name);
							if (t != null) {
								t.remove();
							}
						}
					} catch (IOException e) {
						return Future.failedFuture(e);
					}
					// Reconnect toxiClient1 and send FIRST_MESSAGE; verify ON_MULLIGAN is re-sent
					toxiClient1.legacy().subscribeGame(writer1Reconnected::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								reconnectedMulligan1.tryComplete(msg);
							}
						});
					}).onFailure(reconnectedMulligan1::tryFail);
					return writer1Reconnected.future()
							.compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
							.compose(ignored -> reconnectedMulligan1.future());
				})
				.compose(resentMulligan -> {
					testContext.verify(() -> {
						assertEquals(firstMulligan1.future().result().getId(), resentMulligan.getId(),
								"reconnected player before mulligan submission must receive the pending ON_MULLIGAN with matching ID");
						assertEquals(firstMulligan1.future().result().getStartingCardsCount(), resentMulligan.getStartingCardsCount());
					});
					// Now Player 1 submits UPDATE_MULLIGAN (discarding index 0), then disconnects AGAIN while Player 2 still hasn't mulliganed
					return writer1Reconnected.future()
							.compose(w -> w.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_MULLIGAN)
									.setRepliesTo(resentMulligan.getId())
									.addDiscardedCardIndices(0)
									.build()))
							.compose(ignored -> Environment.sleep(vertx, 250L));
				})
				.compose(v -> {
					// Reconnect Player 1 a second time after Player 1 already submitted mulligan
					toxiClient1.legacy().subscribeGame(writer1PostMulligan::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								reconnectedReceivedDuplicateMulligan.set(true);
							} else if (msg.getMessageType() == MessageType.ON_UPDATE) {
								postMulliganUpdate1.tryComplete(msg);
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								turn1Started.tryComplete(msg);
							}
						});
					}).onFailure(postMulliganUpdate1::tryFail);
					return writer1PostMulligan.future()
							.compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
							.compose(ignored -> postMulliganUpdate1.future())
							.compose(ignored -> Environment.sleep(vertx, 250L));
				})
				.compose(v -> {
					testContext.verify(() -> assertFalse(reconnectedReceivedDuplicateMulligan.get(),
							"player who already submitted UPDATE_MULLIGAN must not receive ON_MULLIGAN again on reconnect"));
					// Finally Player 2 submits UPDATE_MULLIGAN, which should start Turn 1
					return writer2.future().compose(w -> w.write(ClientToServerMessage.newBuilder()
							.setMessageType(MessageType.UPDATE_MULLIGAN)
							.setRepliesTo(firstMulligan2.future().result().getId())
							.build()))
							.compose(ignored -> turn1Started.future());
				})
				.onSuccess(turn1Req -> testContext.verify(() -> {
					assertEquals(MessageType.ON_REQUEST_ACTION, turn1Req.getMessageType());
					assertFalse(turn1Req.getActions().getAllList().isEmpty());
				}))
				.compose(v -> Games.concedeGame(toxiClient1.getUserEntity().getId()))
				.eventually(toxiClient1::closeFut)
				.eventually(client2::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 90, timeUnit = TimeUnit.SECONDS)
	public void testToxiproxyMidTurnActiveAndNonActivePlayerDisconnectAndReconnect(Vertx vertx, VertxTestContext testContext) {
		var toxiClient1 = new ToxiClient(vertx);
		var client2 = new Client(vertx);
		var writer1 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var writer2 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var activeOnTurn2 = Promise.<ServerToClientMessage>promise();
		var activeIsPlayer1 = new AtomicBoolean(false);

		startGateway(vertx)
				.compose(v -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(v -> join(toxiClient1.createAndLogin(), client2.createAndLogin()))
				.compose(v -> client2.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					return insertMultiplayerGameRecord(
							toxiClient1.getUserEntity().getId(), deckId,
							client2.getUserEntity().getId(), deckId)
							.compose(gameId -> Games.createGame(multiplayerConfigurationRequest(
									gameId.toString(),
									toxiClient1.getUserEntity().getId(), deckId,
									client2.getUserEntity().getId(), deckId,
									0L)));
				})
				.compose(ignored -> {
					var turn1Ended = new AtomicBoolean(false);
					toxiClient1.legacy().subscribeGame(writer1::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							var w = writer1.future().result();
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								if (turn1Ended.compareAndSet(false, true)) {
									var endTurn = msg.getActions().getAllList().stream()
											.filter(a -> a.getActionType() == ActionTypeMessage.ActionType.END_TURN)
											.findFirst().orElseThrow();
									w.write(ClientToServerMessage.newBuilder()
											.setMessageType(MessageType.UPDATE_ACTION)
											.setRepliesTo(msg.getId())
											.setActionIndex(endTurn.getAction())
											.build());
								} else {
									activeIsPlayer1.set(true);
									activeOnTurn2.tryComplete(msg);
								}
							}
						});
					}).onFailure(activeOnTurn2::tryFail);

					client2.legacy().subscribeGame(writer2::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							var w = writer2.future().result();
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								if (turn1Ended.compareAndSet(false, true)) {
									var endTurn = msg.getActions().getAllList().stream()
											.filter(a -> a.getActionType() == ActionTypeMessage.ActionType.END_TURN)
											.findFirst().orElseThrow();
									w.write(ClientToServerMessage.newBuilder()
											.setMessageType(MessageType.UPDATE_ACTION)
											.setRepliesTo(msg.getId())
											.setActionIndex(endTurn.getAction())
											.build());
								} else {
									activeIsPlayer1.set(false);
									activeOnTurn2.tryComplete(msg);
								}
							}
						});
					}).onFailure(activeOnTurn2::tryFail);

					return all(
							writer1.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build())),
							writer2.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
					).compose(v -> activeOnTurn2.future());
				})
				.compose(pendingTurn2Req -> {
					// Sever toxiClient1 via Toxiproxy mid-turn
					try {
						toxicGrpcProxy().toxics().resetPeer("midturn-reset-down", ToxicDirection.DOWNSTREAM, 1);
						toxicGrpcProxy().toxics().resetPeer("midturn-reset-up", ToxicDirection.UPSTREAM, 1);
					} catch (IOException e) {
						return Future.failedFuture(e);
					}
					return Environment.sleep(vertx, 300L).map(pendingTurn2Req);
				})
				.compose(pendingTurn2Req -> {
					try {
						for (var name : List.of("midturn-reset-down", "midturn-reset-up")) {
							var t = toxicGrpcProxy().toxics().get(name);
							if (t != null) {
								t.remove();
							}
						}
					} catch (IOException e) {
						return Future.failedFuture(e);
					}
					// Both active and non-active players reconnect mid-turn and verify powerHistory + action retry
					var reconnectedUpdate1 = Promise.<ServerToClientMessage>promise();
					var reconnectedUpdate2 = Promise.<ServerToClientMessage>promise();
					var reconnectedActionReq = Promise.<ServerToClientMessage>promise();
					var nonActiveReceivedActionReq = new AtomicBoolean(false);
					var newWriter1 = Promise.<WriteStream<ClientToServerMessage>>promise();
					var newWriter2 = Promise.<WriteStream<ClientToServerMessage>>promise();

					toxiClient1.legacy().subscribeGame(newWriter1::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_UPDATE && msg.getGameState().getHasPowerHistory()) {
								reconnectedUpdate1.tryComplete(msg);
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								if (activeIsPlayer1.get()) {
									reconnectedActionReq.tryComplete(msg);
								} else {
									nonActiveReceivedActionReq.set(true);
								}
							}
						});
					}).onFailure(reconnectedUpdate1::tryFail);

					client2.legacy().subscribeGame(newWriter2::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_UPDATE && msg.getGameState().getHasPowerHistory()) {
								reconnectedUpdate2.tryComplete(msg);
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								if (!activeIsPlayer1.get()) {
									reconnectedActionReq.tryComplete(msg);
								} else {
									nonActiveReceivedActionReq.set(true);
								}
							}
						});
					}).onFailure(reconnectedUpdate2::tryFail);

					return all(
							newWriter1.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build())),
							newWriter2.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
					).compose(v -> all(reconnectedUpdate1.future(), reconnectedUpdate2.future(), reconnectedActionReq.future()))
							.compose(cf -> {
								testContext.verify(() -> {
									var u1 = cf.<ServerToClientMessage>resultAt(0);
									var u2 = cf.<ServerToClientMessage>resultAt(1);
									var req = cf.<ServerToClientMessage>resultAt(2);
									assertTrue(u1.getGameState().getHasPowerHistory(), "reconnecting player 1 must receive powerHistory");
									assertFalse(u1.getGameState().getPowerHistoryList().isEmpty(), "powerHistory must contain prior turn events");
									assertTrue(u2.getGameState().getHasPowerHistory(), "reconnecting player 2 must receive powerHistory");
									assertEquals(pendingTurn2Req.getId(), req.getId(), "active player must receive re-sent ON_REQUEST_ACTION with identical ID");
									assertFalse(nonActiveReceivedActionReq.get(), "non-active player must not receive ON_REQUEST_ACTION on reconnect");
								});
								// Now complete the game
								return all(toxiClient1.playUntilGameOver(), client2.playUntilGameOver());
							});
				})
				.onSuccess(cf -> testContext.verify(() -> {
					var end1 = cf.<ServerToClientMessage>resultAt(0);
					var end2 = cf.<ServerToClientMessage>resultAt(1);
					assertEquals(MessageType.ON_GAME_END, end1.getMessageType());
					assertEquals(MessageType.ON_GAME_END, end2.getMessageType());
				}))
				.eventually(toxiClient1::closeFut)
				.eventually(client2::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 90, timeUnit = TimeUnit.SECONDS)
	public void testStaleDuplicateAndMalformedMulliganAndActionIndicesIgnoredGracefully(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var writer1 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var writer2 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var mulligan1 = Promise.<ServerToClientMessage>promise();
		var mulligan2 = Promise.<ServerToClientMessage>promise();
		var turn1Req = Promise.<ServerToClientMessage>promise();
		var turn2Req = Promise.<ServerToClientMessage>promise();
		var activeWriterRef = new AtomicReference<WriteStream<ClientToServerMessage>>();

		startGateway(vertx)
				.compose(v -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(v -> join(client1.createAndLogin(), client2.createAndLogin()))
				.compose(v -> client1.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					return insertMultiplayerGameRecord(
							client1.getUserEntity().getId(), deckId,
							client2.getUserEntity().getId(), deckId)
							.compose(gameId -> Games.createGame(multiplayerConfigurationRequest(
									gameId.toString(),
									client1.getUserEntity().getId(), deckId,
									client2.getUserEntity().getId(), deckId,
									0L)));
				})
				.compose(ignored -> {
					client1.legacy().subscribeGame(writer1::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								mulligan1.tryComplete(msg);
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								if (!turn1Req.future().isComplete()) {
									activeWriterRef.set(writer1.future().result());
									turn1Req.tryComplete(msg);
								} else {
									turn2Req.tryComplete(msg);
								}
							}
						});
					}).onFailure(turn1Req::tryFail);

					client2.legacy().subscribeGame(writer2::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								mulligan2.tryComplete(msg);
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								if (!turn1Req.future().isComplete()) {
									activeWriterRef.set(writer2.future().result());
									turn1Req.tryComplete(msg);
								} else {
									turn2Req.tryComplete(msg);
								}
							}
						});
					}).onFailure(turn1Req::tryFail);

					return all(
							writer1.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build())),
							writer2.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
					).compose(v -> all(mulligan1.future(), mulligan2.future()));
				})
				.compose(v -> {
					// Send UPDATE_MULLIGAN with negative, out-of-bounds, and duplicate indices [-1, 0, 0, 99], followed by a duplicate UPDATE_MULLIGAN
					var w1 = writer1.future().result();
					var w2 = writer2.future().result();
					return w1.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_MULLIGAN)
									.setRepliesTo(mulligan1.future().result().getId())
									.addAllDiscardedCardIndices(List.of(-1, 0, 0, 99))
									.build())
							.compose(ignored -> w1.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_MULLIGAN)
									.setRepliesTo(mulligan1.future().result().getId())
									.addAllDiscardedCardIndices(List.of(1, 2))
									.build()))
							.compose(ignored -> w2.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_MULLIGAN)
									.setRepliesTo(mulligan2.future().result().getId())
									.build()))
							.compose(ignored -> turn1Req.future());
				})
				.compose(req -> {
					var activeWriter = activeWriterRef.get();
					var endTurn = req.getActions().getAllList().stream()
							.filter(a -> a.getActionType() == ActionTypeMessage.ActionType.END_TURN)
							.findFirst().orElseThrow();
					// 1) Stale/bogus repliesTo ID
					// 2) Negative actionIndex (-1) with valid repliesTo ID
					// 3) Out-of-bounds actionIndex (9999) with valid repliesTo ID
					// 4) Valid END_TURN actionIndex with valid repliesTo ID
					return activeWriter.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_ACTION)
									.setRepliesTo("stale-nonexistent-id")
									.setActionIndex(0)
									.build())
							.compose(ignored -> activeWriter.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_ACTION)
									.setRepliesTo(req.getId())
									.setActionIndex(-1)
									.build()))
							.compose(ignored -> activeWriter.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_ACTION)
									.setRepliesTo(req.getId())
									.setActionIndex(9999)
									.build()))
							.compose(ignored -> Environment.sleep(vertx, 150L))
							.compose(ignored -> activeWriter.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_ACTION)
									.setRepliesTo(req.getId())
									.setActionIndex(endTurn.getAction())
									.build()))
							.compose(ignored -> turn2Req.future());
				})
				.onSuccess(req2 -> testContext.verify(() -> {
					assertEquals(MessageType.ON_REQUEST_ACTION, req2.getMessageType(),
							"valid UPDATE_ACTION after malformed attempts must advance the game to Turn 2");
					assertEquals(2, req2.getGameState().getTurnNumber());
				}))
				.compose(v -> Games.concedeGame(client1.getUserEntity().getId()))
				.eventually(client1::closeFut)
				.eventually(client2::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
	public void testTurnTimerExpirationExecutesEndTurnInsteadOfRandomActions(Vertx vertx, VertxTestContext testContext) {
		System.setProperty(GameLogic.GAMES_TURN_TIME_MILLIS, "1500");
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var writer1 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var writer2 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var turn1Req = Promise.<ServerToClientMessage>promise();
		var turn2Req = Promise.<ServerToClientMessage>promise();
		var performedActionsOnTurn1 = new CopyOnWriteArrayList<ActionTypeMessage.ActionType>();

		startGateway(vertx)
				.compose(v -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(v -> join(client1.createAndLogin(), client2.createAndLogin()))
				.compose(v -> client1.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					return insertMultiplayerGameRecord(
							client1.getUserEntity().getId(), deckId,
							client2.getUserEntity().getId(), deckId)
							.compose(gameId -> Games.createGame(multiplayerConfigurationRequest(
									gameId.toString(),
									client1.getUserEntity().getId(), deckId,
									client2.getUserEntity().getId(), deckId,
									0L)));
				})
				.compose(ignored -> {
					client1.legacy().subscribeGame(writer1::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								writer1.future().result().write(ClientToServerMessage.newBuilder()
										.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
							} else if (msg.getMessageType() == MessageType.ON_GAME_EVENT
									&& msg.getEvent().getEventType() == GameEventTypeMessage.GameEventType.PERFORMED_GAME_ACTION
									&& !turn2Req.future().isComplete()) {
								performedActionsOnTurn1.add(msg.getEvent().getPerformedGameAction().getActionType());
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								if (msg.getGameState().getTurnNumber() == 1) {
									// Active player intentionally does NOT reply on Turn 1; let turn timer expire!
									turn1Req.tryComplete(msg);
								} else if (msg.getGameState().getTurnNumber() >= 2) {
									turn2Req.tryComplete(msg);
								}
							}
						});
					}).onFailure(turn2Req::tryFail);

					client2.legacy().subscribeGame(writer2::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								writer2.future().result().write(ClientToServerMessage.newBuilder()
										.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								if (msg.getGameState().getTurnNumber() == 1) {
									turn1Req.tryComplete(msg);
								} else if (msg.getGameState().getTurnNumber() >= 2) {
									turn2Req.tryComplete(msg);
								}
							}
						});
					}).onFailure(turn2Req::tryFail);

					return all(
							writer1.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build())),
							writer2.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
					).compose(v -> turn1Req.future()).compose(v -> turn2Req.future());
				})
				.onSuccess(req2 -> testContext.verify(() -> {
					assertEquals(2, req2.getGameState().getTurnNumber(), "turn timer expiration must advance to Turn 2");
					assertEquals(List.of(ActionTypeMessage.ActionType.END_TURN), performedActionsOnTurn1,
							"elapsed turn must immediately execute END_TURN rather than playing random cards from hand");
				}))
				.compose(v -> Games.concedeGame(client1.getUserEntity().getId()))
				.eventually(client1::closeFut)
				.eventually(client2::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
	public void testInactivityTimeoutConcedesDisconnectedPlayerAndNotifiesSurvivor(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var writer1 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var writer2 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var gameEnd2 = Promise.<ServerToClientMessage>promise();
		var gameIdRef = new AtomicReference<Long>();
		var pingTimerId = new AtomicReference<Long>();

		startGateway(vertx)
				.compose(v -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(v -> join(client1.createAndLogin(), client2.createAndLogin()))
				.compose(v -> client1.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					return insertMultiplayerGameRecord(
							client1.getUserEntity().getId(), deckId,
							client2.getUserEntity().getId(), deckId)
							.compose(gameId -> {
								gameIdRef.set(gameId);
								// Set a 2000ms noActivityTimeout on both players
								return Games.createGame(multiplayerConfigurationRequest(
										gameId.toString(),
										client1.getUserEntity().getId(), deckId,
										client2.getUserEntity().getId(), deckId,
										2000L));
							});
				})
				.compose(ignored -> {
					client1.legacy().subscribeGame(writer1::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								writer1.future().result().write(ClientToServerMessage.newBuilder()
										.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
								// After mulligan, client1 goes completely silent (simulating dead connection)
							}
						});
					}).onFailure(gameEnd2::tryFail);

					client2.legacy().subscribeGame(writer2::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								writer2.future().result().write(ClientToServerMessage.newBuilder()
										.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
							} else if (msg.getMessageType() == MessageType.ON_GAME_END) {
								gameEnd2.tryComplete(msg);
							}
						});
					}).onFailure(gameEnd2::tryFail);

					return all(
							writer1.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build())),
							writer2.future().compose(w -> {
								// Client 2 stays active by sending PINGPONG every 400ms
								pingTimerId.set(vertx.setPeriodic(400L, id -> w.write(ClientToServerMessage.newBuilder()
										.setMessageType(MessageType.PINGPONG).setRepliesTo("keepalive").build())));
								return w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build());
							})
					).compose(v -> gameEnd2.future());
				})
				.onSuccess(endMsg -> testContext.verify(() -> {
					assertEquals(MessageType.ON_GAME_END, endMsg.getMessageType());
					assertTrue(endMsg.getGameOver().getLocalPlayerWon(),
							"surviving active player 2 must win when inactive player 1 times out");
				}))
				.eventually(() -> {
					if (pingTimerId.get() != null) {
						vertx.cancelTimer(pingTimerId.get());
					}
					return Future.succeededFuture();
				})
				.eventually(client1::closeFut)
				.eventually(client2::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 60, timeUnit = TimeUnit.SECONDS)
	public void testEmoteTouchPingPongAndInBandConcedeProtocol(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var writer1 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var writer2 = Promise.<WriteStream<ClientToServerMessage>>promise();
		var turn1Ready = Promise.<ServerToClientMessage>promise();
		var pongReceived1 = Promise.<ServerToClientMessage>promise();
		var emoteReceived1 = Promise.<ServerToClientMessage>promise();
		var emoteReceived2 = Promise.<ServerToClientMessage>promise();
		var touchReceived2 = Promise.<ServerToClientMessage>promise();
		var untouchReceived2 = Promise.<ServerToClientMessage>promise();
		var touchIncorrectlyEchoedTo1 = new AtomicBoolean(false);
		var gameEnd1 = Promise.<ServerToClientMessage>promise();
		var gameEnd2 = Promise.<ServerToClientMessage>promise();

		startGateway(vertx)
				.compose(v -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(v -> join(client1.createAndLogin(), client2.createAndLogin()))
				.compose(v -> client1.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					return insertMultiplayerGameRecord(
							client1.getUserEntity().getId(), deckId,
							client2.getUserEntity().getId(), deckId)
							.compose(gameId -> Games.createGame(multiplayerConfigurationRequest(
									gameId.toString(),
									client1.getUserEntity().getId(), deckId,
									client2.getUserEntity().getId(), deckId,
									0L)));
				})
				.compose(ignored -> {
					client1.legacy().subscribeGame(writer1::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							switch (msg.getMessageType()) {
								case ON_MULLIGAN -> writer1.future().result().write(ClientToServerMessage.newBuilder()
										.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
								case ON_REQUEST_ACTION -> turn1Ready.tryComplete(msg);
								case PINGPONG -> pongReceived1.tryComplete(msg);
								case EMOTE -> emoteReceived1.tryComplete(msg);
								case TOUCH -> touchIncorrectlyEchoedTo1.set(true);
								case ON_GAME_END -> gameEnd1.tryComplete(msg);
							}
						});
					}).onFailure(gameEnd1::tryFail);

					client2.legacy().subscribeGame(writer2::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							switch (msg.getMessageType()) {
								case ON_MULLIGAN -> writer2.future().result().write(ClientToServerMessage.newBuilder()
										.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
								case ON_REQUEST_ACTION -> turn1Ready.tryComplete(msg);
								case EMOTE -> emoteReceived2.tryComplete(msg);
								case TOUCH -> {
									if (msg.getEvent().getEventType() == GameEventTypeMessage.GameEventType.ENTITY_TOUCHED) {
										touchReceived2.tryComplete(msg);
									} else if (msg.getEvent().getEventType() == GameEventTypeMessage.GameEventType.ENTITY_UNTOUCHED) {
										untouchReceived2.tryComplete(msg);
									}
								}
								case ON_GAME_END -> gameEnd2.tryComplete(msg);
							}
						});
					}).onFailure(gameEnd2::tryFail);

					return all(
							writer1.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build())),
							writer2.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
					).compose(v -> turn1Ready.future());
				})
				.compose(v -> {
					var w1 = writer1.future().result();
					// Send PINGPONG, EMOTE (WELL_PLAYED), TOUCH, and UNTOUCH from Player 1
					return w1.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.PINGPONG)
									.setRepliesTo("ping-42")
									.build())
							.compose(ignored -> w1.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.EMOTE)
									.setEmote(Emote.newBuilder()
											.setEntityId(0)
											.setMessage(Emote.EmoteMessage.WELL_PLAYED))
									.build()))
							.compose(ignored -> w1.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.TOUCH)
									.setEntityTouch(7)
									.build()))
							.compose(ignored -> w1.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.TOUCH)
									.setEntityUntouch(7)
									.build()))
							.compose(ignored -> all(
									pongReceived1.future(),
									emoteReceived1.future(),
									emoteReceived2.future(),
									touchReceived2.future(),
									untouchReceived2.future()));
				})
				.compose(cf -> {
					testContext.verify(() -> {
						assertEquals("ping-42", cf.<ServerToClientMessage>resultAt(0).getId());
						assertEquals(Emote.EmoteMessage.WELL_PLAYED, cf.<ServerToClientMessage>resultAt(1).getEmote().getMessage(),
								"EMOTE must preserve the exact EmoteMessage sent by the player");
						assertEquals(Emote.EmoteMessage.WELL_PLAYED, cf.<ServerToClientMessage>resultAt(2).getEmote().getMessage(),
								"opponent must receive the exact EmoteMessage sent by the player");
						assertEquals(7, cf.<ServerToClientMessage>resultAt(3).getEvent().getEntityTouched());
						assertEquals(7, cf.<ServerToClientMessage>resultAt(4).getEvent().getEntityUntouched());
						assertFalse(touchIncorrectlyEchoedTo1.get(), "TOUCH notifications must only be sent to the opponent");
					});
					// Send in-band CONCEDE twice to test idempotency
					var w1 = writer1.future().result();
					return w1.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.CONCEDE).build())
							.compose(ignored -> w1.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.CONCEDE).build()))
							.compose(ignored -> all(gameEnd1.future(), gameEnd2.future()));
				})
				.onSuccess(cf -> testContext.verify(() -> {
					var end1 = cf.<ServerToClientMessage>resultAt(0);
					var end2 = cf.<ServerToClientMessage>resultAt(1);
					assertFalse(end1.getGameOver().getLocalPlayerWon(), "conceding player 1 must lose");
					assertTrue(end2.getGameOver().getLocalPlayerWon(), "opponent player 2 must win");
				}))
				.eventually(client1::closeFut)
				.eventually(client2::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 90, timeUnit = TimeUnit.SECONDS)
	public void testSequentialGamesBySameUsersCleanUpUserVerticlesWithoutLeaks(Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var vertxInternal = (VertxInternal) vertx;
		var baselineDeployments = new AtomicInteger();

		startGateway(vertx)
				.compose(v -> vertx.deployVerticle(new ClusteredGames(), new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(v -> join(client1.createAndLogin(), client2.createAndLogin()))
				.compose(v -> client1.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					baselineDeployments.set(vertxInternal.deploymentIDs().size());
					Future<Void> chain = Future.succeededFuture();
					for (int round = 0; round < 3; round++) {
						chain = chain.compose(ignored -> insertMultiplayerGameRecord(
										client1.getUserEntity().getId(), deckId,
										client2.getUserEntity().getId(), deckId))
								.compose(gameId -> Games.createGame(multiplayerConfigurationRequest(
										gameId.toString(),
										client1.getUserEntity().getId(), deckId,
										client2.getUserEntity().getId(), deckId,
										0L)))
								.compose(ignored -> all(client1.playUntilGameOver(), client2.playUntilGameOver()))
								.onSuccess(cf -> testContext.verify(() -> {
									var end1 = cf.<ServerToClientMessage>resultAt(0);
									var end2 = cf.<ServerToClientMessage>resultAt(1);
									assertEquals(MessageType.ON_GAME_END, end1.getMessageType());
									assertEquals(MessageType.ON_GAME_END, end2.getMessageType());
								}))
								.compose(ignored -> Environment.sleep(vertx, 300L));
					}
					return chain;
				})
				.onSuccess(ignored -> testContext.verify(() -> {
					int afterDeployments = vertxInternal.deploymentIDs().size();
					assertEquals(baselineDeployments.get(), afterDeployments,
							"all per-game ServerGameContext and userVerticle deployments must be undeployed after games end");
					var threadBean = ManagementFactory.getThreadMXBean();
					var threadInfos = threadBean.dumpAllThreads(false, false);
					long lingeringPlayThreads = Arrays.stream(threadInfos)
							.filter(ti -> ti.getThreadName().startsWith("ServerGameContext/play{"))
							.count();
					assertEquals(0L, lingeringPlayThreads, "zero ServerGameContext/play virtual threads may linger after all games finish");
				}))
				.eventually(client1::closeFut)
				.eventually(client2::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 90, timeUnit = TimeUnit.SECONDS)
	public void testMidTurnRestoreFromTraceDoesNotDoubleStartTurn(Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx);
		var clusteredGamesDeployment = new AtomicReference<String>();
		var gameIdRef = new AtomicReference<Long>();
		var preRestartTurnNumber = new AtomicInteger();

		startGateway(vertx)
				.compose(ignored -> client.createAndLogin())
				.compose(ignored -> vertx.deployVerticle(new ClusteredGames(),
						new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.onSuccess(clusteredGamesDeployment::set)
				.compose(ignored -> client.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> Environment.query(dsl -> dsl.insertInto(GAMES).defaultValues().returning(GAMES.ID))
						.compose(rows -> {
							var id = rows.iterator().next().getLong(GAMES.ID.getName());
							gameIdRef.set(id);
							var deckId = decks.getDecks(0).getCollection().getId();
							return Environment.withDslContext(dsl -> dsl.insertInto(GAME_USERS)
											.set(GAME_USERS.GAME_ID, id)
											.set(GAME_USERS.USER_ID, client.getUserEntity().getId())
											.set(GAME_USERS.DECK_ID, deckId)
											.set(GAME_USERS.PLAYER_INDEX, (short) 0))
									.map(deckId);
						}))
				.compose(deckId -> Games.createGame(ConfigurationRequest.botMatch(
						gameIdRef.get().toString(),
						client.getUserEntity().getId(),
						client.getUserEntity().getId(),
						deckId,
						deckId)))
				.compose(ignored -> {
					// Play until the human player has a mid-turn non-END_TURN action (e.g. Coin or 1-drop or Hero Power on turn 2),
					// perform that single mid-turn action, and stop on the next ON_REQUEST_ACTION in the SAME turn!
					var writer = Promise.<WriteStream<ClientToServerMessage>>promise();
					var midTurnDone = Promise.<Void>promise();
					var playedMidTurnAction = new AtomicBoolean(false);
					client.legacy().subscribeGame(writer::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							var w = writer.future().result();
							if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
								w.write(ClientToServerMessage.newBuilder()
										.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
							} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								if (!playedMidTurnAction.get()) {
									var nonEndTurn = msg.getActions().getAllList().stream()
											.filter(a -> a.getActionType() != ActionTypeMessage.ActionType.END_TURN)
											.findFirst();
									if (nonEndTurn.isPresent()) {
										playedMidTurnAction.set(true);
										preRestartTurnNumber.set(msg.getGameState().getTurnNumber());
										w.write(ClientToServerMessage.newBuilder()
												.setMessageType(MessageType.UPDATE_ACTION)
												.setRepliesTo(msg.getId())
												.setActionIndex(nonEndTurn.get().getAction())
												.build());
									} else {
										var endTurn = msg.getActions().getAllList().stream()
												.filter(a -> a.getActionType() == ActionTypeMessage.ActionType.END_TURN)
												.findFirst().orElseThrow();
										w.write(ClientToServerMessage.newBuilder()
												.setMessageType(MessageType.UPDATE_ACTION)
												.setRepliesTo(msg.getId())
												.setActionIndex(endTurn.getAction())
												.build());
									}
								} else {
									w.end();
									midTurnDone.tryComplete();
								}
							}
						});
					}).onFailure(midTurnDone::tryFail);
					return writer.future()
							.compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
							.compose(ignored2 -> midTurnDone.future());
				})
				.compose(ignored -> awaitActionCheckpoint(vertx, gameIdRef.get(), System.nanoTime() + TimeUnit.SECONDS.toNanos(10)))
				.compose(ignored -> vertx.undeploy(clusteredGamesDeployment.get()))
				.compose(ignored -> vertx.deployVerticle(new ClusteredGames(),
						new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.onSuccess(clusteredGamesDeployment::set)
				.compose(ignored -> Games.getGameId(client.getUserEntity().getId()))
				.onSuccess(restoredGameId -> testContext.verify(() ->
						assertEquals(gameIdRef.get().toString(), restoredGameId, "persisted mid-turn bot match should restore")))
				.compose(ignored -> {
					// Reconnect to the restored session and verify turnNumber was NOT incremented a second time
					var writer = Promise.<WriteStream<ClientToServerMessage>>promise();
					var restoredReq = Promise.<ServerToClientMessage>promise();
					client.legacy().subscribeGame(writer::tryComplete).onSuccess(reader -> {
						reader.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
								restoredReq.tryComplete(msg);
							}
						});
					}).onFailure(restoredReq::tryFail);
					return writer.future()
							.compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
							.compose(ignored2 -> restoredReq.future());
				})
				.onSuccess(restoredMsg -> testContext.verify(() -> {
					assertEquals(preRestartTurnNumber.get(), restoredMsg.getGameState().getTurnNumber(),
							"mid-turn restoreFromTrace must resume the in-progress turn without calling startTurn a second time");
				}))
				.compose(ignored -> Games.concedeGame(client.getUserEntity().getId()))
				.eventually(client::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 120, timeUnit = TimeUnit.SECONDS)
	public void testClusteredCrossNodeMidGameReconnect(VertxTestContext testContext) {
		int serverVertices = 2;
		var configuration = Environment.getConfiguration();
		originalClusterTestConfiguration = configuration;
		var defaultConfiguration = Hiddenswitch.ServerConfiguration.newBuilder(configuration).build();
		var originalPort = configuration.getGrpcConfiguration().getPort();
		var clientVertx = Vertx.vertx();
		ownedVertices.add(Future.succeededFuture(clientVertx));

		var vertexFuts = join(IntStream.range(0, serverVertices)
				.mapToObj(v -> {
					var thisConfiguration = Hiddenswitch.ServerConfiguration.newBuilder(defaultConfiguration);
					thisConfiguration.setVertx(Hiddenswitch.ServerConfiguration.VertxConfiguration.newBuilder()
							.setUseInfinispanClusterManager(true)
							.setInfinspanPort(0)
							.build());
					Environment.setConfiguration(thisConfiguration.build());
					var cacheManager = Clustered.infinispanClusterManagerUdp(0);
					return Vertx.builder()
							.with(Environment.vertxOptions())
							.withClusterManager(new InfinispanClusterManager(cacheManager))
							.buildClustered();
				}).peek(ownedVertices::add).toList());

		var client1Node0 = new ClientWithPort(clientVertx, originalPort);
		var client2Node1 = new ClientWithPort(clientVertx, originalPort + 1);
		var client1Node1 = new AtomicReference<ClientWithPort>();

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
							.compose(v -> vertx.deployVerticle(ClusteredGames.class, new DeploymentOptions()
									.setThreadingModel(ThreadingModel.VIRTUAL_THREAD)
									.setInstances(1)))
							.map(vertx);
				}).toList()))
				.compose(v -> join(client1Node0.createAndLogin(), client2Node1.createAndLogin()))
				.compose(v -> client1Node0.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					return insertMultiplayerGameRecord(
							client1Node0.getUserEntity().getId(), deckId,
							client2Node1.getUserEntity().getId(), deckId)
							.compose(gameId -> {
								var node0 = vertexFuts.<Vertx>resultAt(0);
								var createProm = Promise.<Void>promise();
								node0.runOnContext(v -> Games.createGame(multiplayerConfigurationRequest(
										gameId.toString(),
										client1Node0.getUserEntity().getId(), deckId,
										client2Node1.getUserEntity().getId(), deckId,
										0L)).<Void>mapEmpty().onComplete(createProm));
								return createProm.future();
							});
				})
				.compose(ignored -> {
					// Both clients connect on their respective nodes (Node 0 and Node 1) and complete mulligan
					var w1 = Promise.<WriteStream<ClientToServerMessage>>promise();
					var w2 = Promise.<WriteStream<ClientToServerMessage>>promise();
					var turn1Reached = Promise.<Void>promise();
					client1Node0.legacy().subscribeGame(w1::tryComplete).onSuccess(r -> r.handler(msg -> {
						if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
							w1.future().result().write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
						} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
							turn1Reached.tryComplete();
						}
					})).onFailure(turn1Reached::tryFail);

					client2Node1.legacy().subscribeGame(w2::tryComplete).onSuccess(r -> r.handler(msg -> {
						if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
							w2.future().result().write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
						} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
							turn1Reached.tryComplete();
						}
					})).onFailure(turn1Reached::tryFail);

					return all(
							w1.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build())),
							w2.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
					).compose(v -> turn1Reached.future());
				})
				.compose(v -> {
					// Now Player 1 disconnects from Node 0 and reconnects to Node 1 (cross-node failover!)
					return client1Node0.closeFut().compose(ignored -> {
						var migrated = new ClientWithPort(clientVertx, originalPort + 1);
						migrated.setAccessToken(client1Node0.getAccessToken())
								.setUserEntity(client1Node0.getUserEntity());
						client1Node1.set(migrated);
						return all(migrated.playUntilGameOver(), client2Node1.playUntilGameOver());
					});
				})
				.onSuccess(cf -> testContext.verify(() -> {
					var end1 = cf.<ServerToClientMessage>resultAt(0);
					var end2 = cf.<ServerToClientMessage>resultAt(1);
					assertEquals(MessageType.ON_GAME_END, end1.getMessageType());
					assertEquals(MessageType.ON_GAME_END, end2.getMessageType());
					assertNotEquals(end1.getGameOver().getLocalPlayerWon(), end2.getGameOver().getLocalPlayerWon());
				}))
				.eventually(() -> client1Node1.get() != null ? client1Node1.get().closeFut() : Future.succeededFuture())
				.eventually(client2Node1::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
	public void testInitialConnectionTimeoutAwardsVictoryToConnectingPlayer(Vertx vertx, VertxTestContext testContext) {
		System.setProperty(Games.GAMES_DEFAULT_NO_ACTIVITY_TIMEOUT, "600");
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var gameIdRef = new AtomicReference<Long>();
		var client1GameEnd = Promise.<ServerToClientMessage>promise();

		startGateway(vertx)
				.compose(ignored -> join(client1.createAndLogin(), client2.createAndLogin()))
				.compose(ignored -> vertx.deployVerticle(new ClusteredGames(),
						new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(ignored -> client1.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					return insertMultiplayerGameRecord(
							client1.getUserEntity().getId(), deckId,
							client2.getUserEntity().getId(), deckId)
							.compose(gameId -> {
								gameIdRef.set(gameId);
								return Games.createGame(multiplayerConfigurationRequest(
										gameId.toString(),
										client1.getUserEntity().getId(), deckId,
										client2.getUserEntity().getId(), deckId,
										60000L));
							});
				})
				.compose(ignored -> {
					// Player 1 connects and sends FIRST_MESSAGE; Player 2 never connects!
					var writer1 = Promise.<WriteStream<ClientToServerMessage>>promise();
					client1.legacy().subscribeGame(writer1::tryComplete).onSuccess(reader1 -> {
						reader1.handler(msg -> {
							if (msg.getMessageType() == MessageType.ON_GAME_END) {
								client1GameEnd.tryComplete(msg);
							}
						});
					}).onFailure(client1GameEnd::tryFail);
					return writer1.future()
							.compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
							.compose(ignored2 -> client1GameEnd.future());
				})
				.onSuccess(gameEndMsg -> testContext.verify(() -> {
					assertEquals(MessageType.ON_GAME_END, gameEndMsg.getMessageType());
					assertTrue(gameEndMsg.getGameOver().getLocalPlayerWon(),
							"Connecting player should win by forfeit when opponent fails to connect within initial connection timeout");
				}))
				.compose(ignored -> client1.games().isInMatch(Empty.getDefaultInstance()))
				.onSuccess(inMatch -> testContext.verify(() ->
						assertEquals("", inMatch.getValue(), "isInMatch RPC must return empty string after initial connection timeout ends game")))
				.compose(ignored -> Environment.query(dsl -> dsl.select(GAMES.STATUS, GAME_USERS.USER_ID, GAME_USERS.VICTORY_STATUS)
						.from(GAMES)
						.join(GAME_USERS).on(GAME_USERS.GAME_ID.eq(GAMES.ID))
						.where(GAMES.ID.eq(gameIdRef.get()))))
				.onSuccess(rows -> testContext.verify(() -> {
					var count = 0;
					for (var row : rows) {
						count++;
						assertEquals("FINISHED", row.getString(GAMES.STATUS.getName()));
						var uid = row.getString(GAME_USERS.USER_ID.getName());
						var victory = row.getString(GAME_USERS.VICTORY_STATUS.getName());
						if (uid.equals(client1.getUserEntity().getId())) {
							assertEquals("WON", victory);
						} else {
							assertEquals("LOST", victory);
						}
					}
					assertEquals(2, count);
				}))
				.eventually(client1::closeFut)
				.eventually(client2::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
	public void testMulliganTimerExpirationAutoCompletesMulligansAndStartsTurnOne(Vertx vertx, VertxTestContext testContext) {
		System.setProperty(GameLogic.GAMES_MULLIGAN_TIME_MILLIS, "500");
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		var gameIdRef = new AtomicReference<Long>();
		var turn1ActionRequested = Promise.<ServerToClientMessage>promise();

		startGateway(vertx)
				.compose(ignored -> join(client1.createAndLogin(), client2.createAndLogin()))
				.compose(ignored -> vertx.deployVerticle(new ClusteredGames(),
						new DeploymentOptions().setThreadingModel(ThreadingModel.VIRTUAL_THREAD)))
				.compose(ignored -> client1.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks -> {
					var deckId = decks.getDecks(0).getCollection().getId();
					return insertMultiplayerGameRecord(
							client1.getUserEntity().getId(), deckId,
							client2.getUserEntity().getId(), deckId)
							.compose(gameId -> {
								gameIdRef.set(gameId);
								return Games.createGame(multiplayerConfigurationRequest(
										gameId.toString(),
										client1.getUserEntity().getId(), deckId,
										client2.getUserEntity().getId(), deckId,
										0L));
							});
				})
				.compose(ignored -> {
					// Both connect; Player 1 submits mulligan, Player 2 never submits mulligan and lets mulligan timer elapse!
					var w1 = Promise.<WriteStream<ClientToServerMessage>>promise();
					var w2 = Promise.<WriteStream<ClientToServerMessage>>promise();
					client1.legacy().subscribeGame(w1::tryComplete).onSuccess(r1 -> r1.handler(msg -> {
						if (msg.getMessageType() == MessageType.ON_MULLIGAN) {
							w1.future().result().write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(msg.getId()).build());
						} else if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
							turn1ActionRequested.tryComplete(msg);
						}
					})).onFailure(turn1ActionRequested::tryFail);

					client2.legacy().subscribeGame(w2::tryComplete).onSuccess(r2 -> r2.handler(msg -> {
						// Intentionally do NOT reply to ON_MULLIGAN on client2!
						if (msg.getMessageType() == MessageType.ON_REQUEST_ACTION) {
							turn1ActionRequested.tryComplete(msg);
						}
					})).onFailure(turn1ActionRequested::tryFail);

					return all(
							w1.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build())),
							w2.future().compose(w -> w.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.FIRST_MESSAGE).build()))
					).compose(v -> turn1ActionRequested.future());
				})
				.onSuccess(reqMsg -> testContext.verify(() -> {
					assertEquals(MessageType.ON_REQUEST_ACTION, reqMsg.getMessageType());
					assertTrue(reqMsg.getGameState().getTurnNumber() >= 1, "Mulligan timer expiration should advance game to turn 1");
				}))
				.compose(ignored -> client1.games().isInMatch(Empty.getDefaultInstance()))
				.onSuccess(inMatch -> testContext.verify(() ->
						assertEquals(gameIdRef.get().toString(), inMatch.getValue(), "isInMatch RPC must return the active game ID")))
				.compose(ignored -> Games.concedeGame(client1.getUserEntity().getId()))
				.eventually(client1::closeFut)
				.eventually(client2::closeFut)
				.onComplete(testContext.succeedingThenComplete());
	}

	@Test
	@Timeout(value = 30, timeUnit = TimeUnit.SECONDS)
	public void testRetryMessageProducerExhaustionDisconnectCancellationAndTrim(Vertx vertx, VertxTestContext testContext) {
		vertx.runOnContext(v -> {
			var eb = vertx.eventBus();
			var address1 = "test:retry:exhaust:" + java.util.UUID.randomUUID();
			var producer1 = new com.hiddenswitch.framework.impl.RetryMessageProducer<>(
					eb.<String>publisher(address1), 2, 30, s -> s.startsWith("RETRY"));

			// Verify address() and deliveryOptions()
			testContext.verify(() -> {
				assertEquals(address1, producer1.address());
				assertNotNull(producer1.deliveryOptions(new io.vertx.core.eventbus.DeliveryOptions().setSendTimeout(100)));
			});

			// 1. Write a message that retries twice on NO_HANDLERS and queue a second message behind it;
			// register a consumer right after the first message exhausts its retries so the second queued message succeeds!
			var f1 = producer1.write("RETRY_1", new io.vertx.core.eventbus.DeliveryOptions());
			var f2 = producer1.write("RETRY_2");
			var received2 = Promise.<String>promise();
			f1.onFailure(err -> {
				var c = eb.<String>consumer(address1, msg -> received2.tryComplete(msg.body()));
				c.completion();
			});

			// 2. Test trim() failing queued items and close() cancelling an active retry timer + failing pending items
			var address2 = "test:retry:cancel:" + java.util.UUID.randomUUID();
			var producer2 = new com.hiddenswitch.framework.impl.RetryMessageProducer<>(
					eb.<String>publisher(address2), 20, 50);
			var c1 = producer2.write("IN_FLIGHT_RETRYING");
			var c2 = producer2.write("WAITING_TO_BE_TRIMMED");
			producer2.trim();
			var c3 = producer2.write("WAITING_TO_BE_CLOSED");

			Environment.sleep(vertx, 20)
					.compose(ignored -> producer2.close())
					.compose(ignored -> producer2.write("AFTER_CLOSE").otherwiseEmpty())
					.compose(ignored -> received2.future())
					.compose(val2 -> f2.map(val2))
					.onSuccess(val2 -> testContext.verify(() -> {
						assertTrue(f1.failed(), "First message should fail after exhausting retries on NO_HANDLERS");
						assertEquals("RETRY_2", val2, "Second queued message should be drained and delivered after first fails");
						assertTrue(c2.failed(), "Trimmed waiting message must fail its promise");
						assertTrue(c1.failed(), "In-flight retrying message must fail when producer is closed");
						assertTrue(c3.failed(), "Waiting message must fail when producer is closed");
					}))
					.eventually(producer1::close)
					.onComplete(testContext.succeedingThenComplete());
		});
	}

	@Test
	@Timeout(value = 15, timeUnit = TimeUnit.SECONDS)
	public void testActivityMonitorAndGameplayRequestContract(Vertx vertx, VertxTestContext testContext) {
		vertx.runOnContext(v -> {
			testContext.verify(() -> {
				assertThrows(IllegalArgumentException.class,
						() -> new com.hiddenswitch.framework.impl.ActivityMonitor(vertx, -1L, m -> {}),
						"Negative noActivityTimeout must be rejected");
				var disabledMonitor = new com.hiddenswitch.framework.impl.ActivityMonitor(vertx, 0L, m -> {});
				disabledMonitor.activity();
				disabledMonitor.cancel();

				var mulliganResult = new AtomicReference<java.util.List<net.demilich.metastone.game.cards.Card>>();
				var mulliganReq = new com.hiddenswitch.framework.impl.GameplayRequest.Mulligan(
						"m1", java.util.Collections.emptyList(), mulliganResult::set);
				assertEquals("m1", mulliganReq.getCallbackId());
				assertTrue(mulliganReq.getStarterCards().isEmpty());
				mulliganReq.resolveIndices(java.util.Arrays.asList(-1, 5, null));
				assertNotNull(mulliganResult.get());
				assertTrue(mulliganResult.get().isEmpty());
				mulliganReq.elapse(null);
				assertTrue(mulliganResult.get().isEmpty());

				var endTurn = new net.demilich.metastone.game.actions.EndTurnAction(0);
				var chosenAction = new AtomicReference<net.demilich.metastone.game.actions.GameAction>();
				var actionReq = new com.hiddenswitch.framework.impl.GameplayRequest.Action(
						"a1", java.util.Collections.singletonList(endTurn), chosenAction::set);
				assertEquals("a1", actionReq.getCallbackId());
				assertFalse(actionReq.isValidIndex(-1));
				assertFalse(actionReq.isValidIndex(1));
				assertTrue(actionReq.isValidIndex(0));
				actionReq.elapse(null);
				assertEquals(endTurn, chosenAction.get());
			});

			var timeoutFired = Promise.<Void>promise();
			var activeMonitor = new com.hiddenswitch.framework.impl.ActivityMonitor(
					vertx, 40L, m -> timeoutFired.tryComplete());
			activeMonitor.activity();
			activeMonitor.activity(); // resets timer cleanly

			var ucb = new com.hiddenswitch.framework.impl.UnityClientBehaviour(null, "u1", 0, 0L);
			testContext.verify(() -> {
				assertEquals("Unity client behaviour", ucb.getName());
				assertNotNull(ucb.clone());
				assertTrue(ucb.toString().contains("u1"));
			});

			timeoutFired.future()
					.onSuccess(ignored -> activeMonitor.cancel())
					.onComplete(testContext.succeedingThenComplete());
		});
	}

	private static class ClientWithPort extends Client {
		private final int port;

		public ClientWithPort(Vertx vertx, int port) {
			super(vertx);
			this.port = port;
		}

		@Override
		protected int port() {
			return port;
		}
	}
}
