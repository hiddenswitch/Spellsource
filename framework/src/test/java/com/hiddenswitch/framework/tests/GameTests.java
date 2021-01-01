package com.hiddenswitch.framework.tests;

import com.google.protobuf.Empty;
import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.impl.ClusteredGames;
import com.hiddenswitch.framework.impl.Configuration;
import com.hiddenswitch.framework.impl.ConfigurationRequest;
import com.hiddenswitch.framework.Games;
import com.hiddenswitch.framework.impl.ModelConversions;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.spellsource.rpc.MessageType;
import com.hiddenswitch.spellsource.rpc.ServerToClientMessage;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.AttributeMap;
import net.demilich.metastone.game.decks.Deck;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static io.vertx.core.impl.future.CompositeFutureImpl.all;
import static io.vertx.reactivex.ObservableHelper.toObservable;
import static io.vertx.reactivex.SingleHelper.toFuture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GameTests extends FrameworkTestBase {

	@Test
	public void testCreatesGame(Vertx vertx, VertxTestContext vertxTestContext) {
		var client1 = new Client(vertx);
		var client2 = new Client(vertx);
		startGateway(vertx)
				.compose(v -> client1.createAndLogin())
				.compose(v -> client2.createAndLogin())
				.compose(v -> vertx.deployVerticle(new ClusteredGames()))
				.compose(v -> client1.legacy().decksGetAll(Empty.getDefaultInstance()))
				.compose(decks1 -> Games.createGame(new ConfigurationRequest()
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
										.setUserId(client2.getUserEntity().getId())))))
				.onFailure(vertxTestContext::failNow)
				.compose(res -> all(client1.connectToGame(), client2.connectToGame()).map(fut -> fut.<ServerToClientMessage>resultAt(0)))
				.compose(msg -> {
					vertxTestContext.verify(() -> {
						assertEquals(MessageType.MESSAGE_TYPE_ON_UPDATE, msg.getMessageType());
					});
					return Future.succeededFuture();
				})
				.onComplete(vertxTestContext.succeedingThenComplete());
	}

	@Test
	public void testAllDecksHaveValidModels(Vertx vertx, VertxTestContext vertxTestContext) {
		var client = new Client(vertx);
		startGateway(vertx)
				.compose(v -> client.createAndLogin())
				.compose(v -> client.legacy().decksGetAll(Empty.getDefaultInstance()))
				.onSuccess(decksGetAllResponse -> {
					vertxTestContext.verify(() -> {
						for (var deckCollection : decksGetAllResponse.getDecksList()) {
							var gameDeck = ModelConversions.getGameDeck(client.getUserEntity().getId(), deckCollection);
							assertEquals(30, gameDeck.getCards().size());
							assertNotNull(gameDeck.getDeckId());
							assertNotNull(gameDeck.getFormat());
							assertNotNull(gameDeck.getHeroClass());
							assertNotNull(gameDeck.getHeroCard());

							// test creating the attributes
							var playerAttributes = new AttributeMap();
							for (var tuple : deckCollection.getCollection().getPlayerEntityAttributesList()) {
								playerAttributes.put(Attribute.valueOf(tuple.getAttribute().name()), tuple.getStringValue());
							}
						}
					});
				})
				.compose(v -> client.closeFut())
				.onComplete(vertxTestContext.succeedingThenComplete());
	}
}
