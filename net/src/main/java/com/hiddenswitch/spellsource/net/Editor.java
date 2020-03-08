package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.net.impl.UnityClientBehaviour;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.impl.util.ServerGameContext;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxException;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;
import net.demilich.metastone.game.cards.desc.CardDesc;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.fromJson;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static com.hiddenswitch.spellsource.net.impl.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Allows players to edit content, including in a live game.
 */
public interface Editor {
	/**
	 * The name of the editable cards collection in mongo
	 */
	String EDITABLE_CARDS = "editable.cards";

	/**
	 * Enables the editor commands for connected, authorized users.
	 * <p>
	 * In the current version of the editor, players can put in Card Script and it will appear as a drawn card in the
	 * game.
	 */
	@Suspendable
	static void handleConnections() {
		Connection.connected((connection, fut) -> {
			// Notify the user of all their existing editable cards
			if (!Fiber.isCurrentFiber()) {
				throw new RuntimeException("not fiber");
			}
			for (var existingCard : mongo().find(EDITABLE_CARDS, json("ownerUserId", connection.userId()), EditableCard.class)) {
				connection.write(new Envelope().added(new EnvelopeAdded().editableCard(existingCard)));
			}
			connection.handler(suspendableHandler(env -> {
				// Process a delete card request
				if (env.getMethod() != null && env.getMethod().getDeleteCard() != null) {
					var deleteCard = env.getMethod().getDeleteCard();
					var editableCardId = deleteCard.getEditableCardId();
					var res = mongo().removeDocument(EDITABLE_CARDS,
							json("_id", editableCardId, "ownerUserId", connection.userId()));
					if (res.getRemovedCount() > 0L) {
						connection.write(new Envelope().removed(new EnvelopeRemoved().editableCardId(editableCardId)));
					}
				}

				// Upserts a card and optionally draws it into a live game, if the player has one
				if (env.getMethod() != null && env.getMethod().getPutCard() != null) {
					var putResult = new EnvelopeResultPutCard();
					var putCard = env.getMethod().getPutCard();
					var recordId = putCard.getEditableCardId();
					// We have to find the game context somewhere on the event bus
					// We will communicate with it via messages on the vus, not through a proxy object or anything like that
					// When the ClusteredGames verticle that has the game context is hosted by the same vertx instance that is
					// running the connections handler, we'll find the address in the "local" event bus map
					var eventBus = Vertx.currentContext().owner().eventBus();
					try {
						// generate a record id (null means create a new one and return it to me)
						if (recordId == null) {
							recordId = mongo().createId();
							putResult.editableCardId(recordId);
						}

						var source = putCard.getSource();
						if (source == null) {
							// No source specified, cannot continue
							writeErrorForPut(connection, "No source code specified.");
							return;
						}
						if (source.length() > 3200) {
							// Too long
							writeErrorForPut(connection, "You attempted to save a source that was too long.");
							return;
						}
						if (mongo().count(EDITABLE_CARDS, json("ownerUserId", connection.userId())) > 1000L) {
							writeErrorForPut(connection, "You've exceeded your card limit.");
							return;
						}
						var updateResult = mongo().updateCollectionWithOptions(EDITABLE_CARDS,
								json("_id", recordId),
								json("$set",
										json("source", source, "ownerUserId", connection.userId())),
								new UpdateOptions().setUpsert(true));
						var editableCard = new EditableCard()
								.id(recordId)
								.source(source)
								.ownerUserId(connection.userId());

						var envelope = new Envelope();

						if (putCard.isDraw() != null && putCard.isDraw()) {
							// Get the game context the player is currently in.
							var gameId = Games.getUsersInGames().get(new UserId(connection.userId()));
							if (gameId != null) {
								Message<JsonObject> resultJson = awaitResult(h -> eventBus.request(getPutCardAddress(gameId.toString()),
										json(
												"userId", connection.userId(),
												"putCard", json(putCard)
										), h));
								putResult = fromJson(resultJson.body(), EnvelopeResultPutCard.class);
							}
						} else if (putCard.getSource() != null && !putCard.getSource().isEmpty()) {
							// Try parsing here for errors
							try {
								var cardDesc = Json.decodeValue(putCard.getSource(), CardDesc.class);
								// TODO: Get more errors
							} catch (DecodeException decodeException) {
								if (decodeException.getCause() instanceof JsonParseException) {
									var jsonParseException = (JsonParseException) decodeException.getCause();
									if (jsonParseException != null && jsonParseException.getLocation() != null) {
										putResult.addCardScriptErrorsItem(String.format("Line %d: %s", jsonParseException.getLocation().getLineNr(), jsonParseException.getMessage()));
									}
								} else {
									putResult.addCardScriptErrorsItem(decodeException.getMessage());
								}
							} catch (Throwable throwable) {
								putResult.addCardScriptErrorsItem(throwable.getMessage());
							}
						}

						envelope
								.result(new EnvelopeResult()
										.putCard(putResult));

						if (updateResult.getDocUpsertedId() != null) {
							envelope.added(new EnvelopeAdded().editableCard(editableCard));
							putCard.editableCardId(recordId);
						} else if (updateResult.getDocModified() > 0L) {
							envelope.changed(new EnvelopeChanged().editableCard(editableCard));
						}

						connection.write(envelope);
					} catch (VertxException exception) {
						// TODO: Special errors here
					}
				}
			}));
			fut.handle(Future.succeededFuture());
		});
	}

	/**
	 * Writes an error message as a result of a card put request
	 *
	 * @param connection
	 * @param message
	 * @return
	 */
	@Suspendable
	static Connection writeErrorForPut(Connection connection, String message) {
		return connection.write(new Envelope().result(new EnvelopeResult().putCard(new EnvelopeResultPutCard().cardScriptErrors(Collections.singletonList(message)))));
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
			var userId = msg.body().getString("userId");
			var putCard = fromJson(msg.body().getJsonObject("putCard"), EnvelopeMethodPutCard.class);

			var result = new EnvelopeResultPutCard();

			// Whenever we mutate the game context this way, we're doing something like a "transaction," it needs to be locked
			// so that simultaneous edits do not occur (e.g., by performing a game action while we are mutating the hand)
			gameContext.getLock().lock();
			try {
				// You can't do this stuff on a game that isn't running
				if (!gameContext.isRunning()) {
					throw new RuntimeException("Game is not yet running.");
				}

				// Find the player with the specified user ID
				var player = gameContext.getPlayers()
						.stream()
						.filter(p -> Objects.equals(userId, p.getUserId()))
						.findFirst()
						.orElseThrow(() -> new NullPointerException(userId));
				var behaviour = gameContext.getBehaviours().get(player.getId());

				// This JSON decoding step raises decode exceptions
				// Eventually it will need to evolve into all sorts of validation and helpful error messages
				var cardDesc = Json.decodeValue(putCard.getSource(), CardDesc.class);
				if (cardDesc.getId() == null || cardDesc.getId().isEmpty()) {
					cardDesc.setId(gameContext.getLogic().generateCardId());
				}
				var card = cardDesc.create();
				// Try to get a valid entity from this to make sure it has no visualization issues
				var testEntity = Games.getEntity(gameContext, card, player.getId());
				// Add the card to the game context
				if (gameContext.getTempCards().containsCard(cardDesc.getId())) {
					gameContext.getTempCards().removeIf(c -> Objects.equals(cardDesc.getId(), c.getCardId()));
				}
				gameContext.addTempCard(card);

				// May raise events
				gameContext.getLogic().receiveCard(player.getId(), card.clone());
				// The actions the player can take have changed so send a new request
				// This replaces existing requests
				if (gameContext.getActivePlayerId() == player.getId() && behaviour instanceof UnityClientBehaviour) {
					((UnityClientBehaviour) behaviour).updateActions(gameContext, gameContext.getValidActions());
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

		// The game context is responsible for unregistering this event bus handler.
		return registration::unregister;
	}

	@NotNull
	static String getPutCardAddress(String gameId) {
		return "Editor/" + gameId + "/putCard";
	}
}
