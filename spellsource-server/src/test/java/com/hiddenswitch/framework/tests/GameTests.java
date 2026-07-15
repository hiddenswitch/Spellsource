package com.hiddenswitch.framework.tests;

import com.google.protobuf.Empty;
import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Environment;
import com.hiddenswitch.framework.impl.ClusteredGames;
import com.hiddenswitch.framework.impl.Configuration;
import com.hiddenswitch.framework.impl.ConfigurationRequest;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.impl.ModelConversions;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.spellsource.rpc.Spellsource.MessageTypeMessage.MessageType;
import com.hiddenswitch.spellsource.rpc.Spellsource.*;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.Promise;
import io.vertx.core.streams.WriteStream;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hiddenswitch.framework.schema.spellsource.Tables.GAMES;
import static com.hiddenswitch.framework.schema.spellsource.Tables.GAME_USERS;
import static org.junit.jupiter.api.Assertions.*;

public class GameTests extends FrameworkTestBase {
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
							.compose(ignored -> Environment.sleep(vertx, 250L))
							.compose(ignored -> Environment.query(dsl -> dsl.select(GAMES.TRACE).from(GAMES).where(GAMES.ID.eq(gameId))))
							.map(rows -> rows.iterator().next().getJsonObject("trace"));
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

	private Future<Void> playThroughOneAction(Client client) {
		var writer = Promise.<WriteStream<ClientToServerMessage>>promise();
		var actionSent = new AtomicBoolean();
		var completed = Promise.<Void>promise();
		client.legacy().subscribeGame(writer::tryComplete).onSuccess(reader -> {
			reader.exceptionHandler(completed::tryFail);
			reader.handler(message -> {
				var stream = writer.future().result();
				switch (message.getMessageType()) {
					case ON_MULLIGAN -> stream.write(ClientToServerMessage.newBuilder()
							.setMessageType(MessageType.UPDATE_MULLIGAN).setRepliesTo(message.getId()).build()).onFailure(completed::tryFail);
					case ON_REQUEST_ACTION -> {
						if (actionSent.compareAndSet(false, true)) {
							stream.write(ClientToServerMessage.newBuilder().setMessageType(MessageType.UPDATE_ACTION)
									.setRepliesTo(message.getId()).setActionIndex(0).build())
									.onSuccess(ignored -> completed.tryComplete()).onFailure(completed::tryFail);
						}
					}
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
}
