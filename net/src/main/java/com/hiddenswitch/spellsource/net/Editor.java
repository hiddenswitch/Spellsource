package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.models.Envelope;
import com.hiddenswitch.spellsource.client.models.EnvelopeMethodPutCard;
import com.hiddenswitch.spellsource.client.models.EnvelopeResult;
import com.hiddenswitch.spellsource.client.models.EnvelopeResultPutCard;
import com.hiddenswitch.spellsource.net.impl.UnityClientBehaviour;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.impl.util.ServerGameContext;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.hiddenswitch.spellsource.net.impl.QuickJson.fromJson;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static com.hiddenswitch.spellsource.net.impl.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Allows players to edit content, including in a live game.
 */
public interface Editor {
	/**
	 * Enables the editor.
	 */
	@Suspendable
	static void handleConnections() {
		Connection.connected((connection, fut) -> {
			connection.handler(suspendableHandler(env -> {
				if (env.getMethod() != null && env.getMethod().getPutCard() != null) {
					var putCard = env.getMethod().getPutCard();
					// Get the game context the player is currently in.
					var gameId = Games.getUsersInGames().get(new UserId(connection.userId()));
					if (gameId == null) {
						return;
					}
					var eventBus = Vertx.currentContext().owner().eventBus();
					try {
						Message<JsonObject> resultJson = awaitResult(h -> eventBus.request(getPutCardAddress(gameId.toString()),
								json(
										"userId", connection.userId(),
										"putCard", json(putCard)
								), h));

						var result = fromJson(resultJson.body(), EnvelopeResultPutCard.class);
						connection.write(new Envelope()
								.result(new EnvelopeResult()
										.putCard(result)));
					} catch (VertxException exception) {
						// TODO: Special errors here
					}
				}
			}));
			fut.handle(Future.succeededFuture());
		});
	}

	/**
	 * Enables editing on the specified game context.
	 *
	 * @param gameContext
	 */
	@Suspendable
	static Closeable enableEditing(ServerGameContext gameContext) {
		var eventBus = Vertx.currentContext().owner().eventBus();

		String gameId = gameContext.getGameId();
		var registration = eventBus.<JsonObject>consumer(getPutCardAddress(gameId), suspendableHandler(msg -> {
			var putCard = fromJson(msg.body().getJsonObject("putCard"), EnvelopeMethodPutCard.class);

			var result = new EnvelopeResultPutCard();
			var userId = msg.body().getString("userId");

			gameContext.getLock().lock();
			try {
				if (!gameContext.isRunning()) {
					throw new RuntimeException("Game is not yet running.");
				}

				var cardDesc = Json.decodeValue(putCard.getCardScript(), CardDesc.class);
				if (cardDesc.getId() == null || cardDesc.getId().isEmpty()) {
					cardDesc.setId(gameContext.getLogic().generateCardId());
				}
				var card = cardDesc.create();
				if (gameContext.getTempCards().containsCard(cardDesc.getId())) {
					gameContext.getTempCards().removeIf(c -> Objects.equals(cardDesc.getId(), c.getCardId()));
				}
				gameContext.addTempCard(card);
				var player = gameContext.getPlayers().stream().filter(p -> Objects.equals(userId, p.getUserId())).findFirst().orElseThrow(() -> new NullPointerException(userId));
				var behaviour = gameContext.getBehaviours().get(player.getId());
				// May raise events
				gameContext.getLogic().receiveCard(player.getId(), card.clone());
				// The actions the player can take have changed so send a new request
				// This replaces existing requests
				if (gameContext.getActivePlayerId() == player.getId() && behaviour instanceof UnityClientBehaviour) {
					((UnityClientBehaviour) behaviour).updateActions(gameContext, player, gameContext.getValidActions());
				}
				// Tell the client what the card ID was
				result.cardId(cardDesc.getId());
			} catch (RuntimeException ex) {
				result.addCardScriptErrorsItem(ex.getMessage());
			} finally {
				gameContext.getLock().unlock();
			}
			msg.reply(json(result));
		}));


		return registration::unregister;
	}

	@NotNull
	static String getPutCardAddress(String gameId) {
		return "Editor/" + gameId + "/putCard";
	}
}
