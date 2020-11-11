package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.core.JsonParseException;
import com.google.common.base.Throwables;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableFunction;
import com.hiddenswitch.spellsource.net.impl.UnityClientBehaviour;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.impl.util.ServerGameContext;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Closeable;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.desc.CardDesc;
import net.demilich.metastone.game.spells.SpellUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.fromJson;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static io.vertx.ext.sync.Sync.await;

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
		Connection.connected("Editor/handleConnections", (connection, fut) -> {
			// Notify the user of all their existing editable cards
			if (!Fiber.isCurrentFiber()) {
				throw new RuntimeException("not fiber");
			}
			var userId = connection.userId();
			for (var existingCard : mongo().find(EDITABLE_CARDS, json("ownerUserId", userId), EditableCard.class)) {
				connection.write(new Envelope().added(new EnvelopeAdded().editableCard(existingCard)));
			}
			connection.handler(io.vertx.ext.sync.Sync.fiber(env -> {
				var tracer = GlobalTracer.get();
				// Process a delete card request
				if (env.getMethod() != null && env.getMethod().getDeleteCard() != null) {
					var deleteCard = env.getMethod().getDeleteCard();
					var editableCardId = deleteCard.getEditableCardId();
					var span = tracer.buildSpan("Editor/deleteCard")
							.withTag("userId", userId)
							.withTag("editableCardId", editableCardId)
							.start();
					try (var scope = tracer.activateSpan(span)) {
						var res = mongo().removeDocument(EDITABLE_CARDS,
								json("_id", editableCardId, "ownerUserId", userId));
						if (res.getRemovedCount() > 0L) {
							connection.write(new Envelope().removed(new EnvelopeRemoved().editableCardId(editableCardId)));
						}
					} catch (RuntimeException runtimeException) {
						Tracing.error(runtimeException, span, true);
						throw runtimeException;
					} finally {
						span.finish();
					}
				}

				// Upserts a card and optionally draws it into a live game, if the player has one
				if (env.getMethod() != null && env.getMethod().getPutCard() != null) {
					connection.write(putCard(env.getMethod().getPutCard(), userId));
				}
			}));
			fut.handle(Future.succeededFuture());
		});
	}

	static Envelope errorResult(String message) {
		return new Envelope().result(new EnvelopeResult().putCard(new EnvelopeResultPutCard().cardScriptErrors(Collections.singletonList(message))));
	}

	/**
	 * Saves and optionally draws a card in a live game.
	 *
	 * @param putCard
	 * @param userId
	 * @return
	 */
	@Suspendable
	static Envelope putCard(EnvelopeMethodPutCard putCard, String userId) {
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("Editor/putCard")
				.withTag("userId", userId)
				.withTag("editableCardId", putCard.getEditableCardId())
				.withTag("source", putCard.getSource())
				.withTag("draw", Objects.equals(putCard.isDraw(), true))
				.start();
		try (var scope = tracer.activateSpan(span)) {
			var putResult = new EnvelopeResultPutCard();
			var recordId = putCard.getEditableCardId();
			// We have to find the game context somewhere on the event bus
			// We will communicate with it via messages on the vus, not through a proxy object or anything like that
			// When the ClusteredGames verticle that has the game context is hosted by the same vertx instance that is
			// running the connections handler, we'll find the address in the "local" event bus map
			var eventBus = Vertx.currentContext().owner().eventBus();

			// generate a record id (null means create a new one and return it to me)
			if (recordId == null) {
				recordId = mongo().createId();
			}

			var source = putCard.getSource();
			if (source == null) {
				// No source specified, cannot continue
				return errorResult("No source code specified.");
			}
			if (source.length() > 3200) {
				// Too long
				return errorResult("You attempted to save a source that was too long.");
			}
			if (mongo().count(EDITABLE_CARDS, json("ownerUserId", userId)) > 1000L) {
				return errorResult("You've exceeded your card limit.");
			}
			var updateResult = mongo().updateCollectionWithOptions(EDITABLE_CARDS,
					json("_id", recordId),
					json("$set",
							json("source", source, "ownerUserId", userId)),
					new UpdateOptions().setUpsert(true));
			var editableCard = new EditableCard()
					.id(recordId)
					.source(source)
					.ownerUserId(userId);

			var envelope = new Envelope();

			var hasSource = putCard.getSource() != null && !putCard.getSource().isEmpty();
			if (hasSource) {
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

			if (hasSource && putCard.isDraw() != null && putCard.isDraw()) {
				// Get the game context the player is currently in.
				var gameId = Games.getGameId(new UserId(userId));
				Message<JsonObject> resultJson = io.vertx.ext.sync.Sync.await(h -> eventBus.request(getPutCardAddress(gameId.toString()),
						json(
								"userId", userId,
								"putCard", json(putCard)
						), h));
				var drawJson = fromJson(resultJson.body(), EnvelopeResultPutCard.class);
				putResult.cardId(drawJson.getCardId());
				if (drawJson.getCardScriptErrors() != null) {
					for (var error : drawJson.getCardScriptErrors()) {
						putResult.addCardScriptErrorsItem(error);
					}
				}
			}

			envelope
					.result(new EnvelopeResult()
							.putCard(putResult));

			if (updateResult.getDocUpsertedId() != null) {
				envelope.added(new EnvelopeAdded().editableCard(editableCard));
				putResult.editableCardId(recordId);
			} else if (updateResult.getDocModified() > 0L) {
				envelope.changed(new EnvelopeChanged().editableCard(editableCard));
			}
			return envelope;
		} catch (Throwable throwable) {
			// Passes along the issue
			Tracing.error(throwable, span, true);
			throw new RuntimeException(throwable);
		} finally {
			span.finish();
		}
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
		var registration = eventBus.<JsonObject>consumer(getPutCardAddress(gameId), Sync.fiber(msg -> {
			var userId = msg.body().getString("userId");
			Objects.requireNonNull(userId);
			var putCard = fromJson(msg.body().getJsonObject("putCard"), EnvelopeMethodPutCard.class);
			Objects.requireNonNull(putCard, "putCard");
			Objects.requireNonNull(putCard.getSource(), "source");
			var result = drawCard(gameContext, userId, putCard.getSource());
			msg.reply(json(result));
		}));

		// The game context is responsible for unregistering this event bus handler.
		return registration::unregister;
	}

	/**
	 * Draws the card specified in the request into the supplied game context.
	 *
	 * @param gameContext
	 * @param userId
	 * @return
	 */
	@Suspendable
	static @NotNull EnvelopeResultPutCard drawCard(@NotNull ServerGameContext gameContext, @NotNull String userId, @NotNull String source) {
		var tracer = GlobalTracer.get();
		var span = tracer.buildSpan("Editor/drawCard")
				.withTag("gameId", gameContext.getGameId())
				.withTag("userId", userId)
				.withTag("source", source)
				.asChildOf(gameContext.getSpanContext())
				.start();
		var scope = tracer.activateSpan(span);
		var result = new EnvelopeResultPutCard();

		// Collecting pieces of data during recoverable phases of drawing a card for editing
		CardDesc cardDesc;
		Behaviour behaviour;
		Player player;
		Card card;

		// This method checks if drawing the card's side effects do not cause a crash.
		SuspendableFunction<GameContext, Card> mutateGameContext;

		// This is a safe bail-out point
		try {
			// You can't do this stuff on a game that isn't running
			if (!gameContext.isRunning()) {
				throw new RuntimeException("Game is not yet running.");
			}

			// Find the player with the specified user ID
			player = gameContext.getPlayers()
					.stream()
					.filter(p -> Objects.equals(userId, p.getUserId()))
					.findFirst()
					.orElseThrow(() -> new NullPointerException(userId));
			behaviour = gameContext.getBehaviours().get(player.getId());

			// This JSON decoding step raises decode exceptions
			// Eventually it will need to evolve into all sorts of validation and helpful error messages
			cardDesc = Json.decodeValue(source, CardDesc.class);
			// Decode exceptions will have cleared by here
			if (cardDesc.getId() == null || cardDesc.getId().isEmpty()) {
				cardDesc.setId(gameContext.getLogic().generateCardId());
			}

			card = cardDesc.create();
			// Try to get a valid entity from this to make sure it has no visualization issues
			var testEntity = Games.getEntity(gameContext, card, player.getId());

			// Check for side effects
			mutateGameContext = (GameContext context) -> {
				// Add the card to the game context
				if (context.getTempCards().containsCard(cardDesc.getId())) {
					context.getTempCards().removeIf(c -> Objects.equals(cardDesc.getId(), c.getCardId()));
				}
				context.addTempCard(card);

				// May raise events
				var clone = card.clone();
				context.getLogic().receiveCard(player.getId(), clone);
				return clone;
			};


			// We will now call this with a time limit to get a sense if it is a safe call
			var clonedContext = gameContext.clone();
			var fiber = new Fiber<>(() -> {
				try {
					Card clonedCard = mutateGameContext.apply(clonedContext);

					// In addition to mutating the game context, we'll do a handful of checks
					// Check that we can get actions
					var actions = clonedContext.getValidActions();

					// Perform the action that corresponds to the card?
					SpellUtils.playCardRandomly(clonedContext, clonedContext.getActivePlayer(), clonedCard, clonedCard,
							true,
							true,
							false,
							true,
							true);
					return clonedCard;
				} catch (InvocationTargetException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			});

			var clonedCard = fiber.start().get(100L, TimeUnit.MILLISECONDS);
		} catch (DecodeException decodeException) {
			// We can recover exceptions at this point, this was already caught elsewhere
			return result;
		} catch (RuntimeException | ExecutionException | TimeoutException | InterruptedException exceptions) {
			// Any other errors should be communicated back to the client for now
			var message = Throwables.getRootCause(exceptions).getMessage();
			if (message == null) {
				if (exceptions instanceof TimeoutException) {
					message = "Your card is so incomplete it hangs the game when played, or it takes too long to play.";
				} else {
					message = "Your card is incomplete or causes issues when played inside a match. It cannot be drawn at this time.";
				}
			}
			result.addCardScriptErrorsItem(message);
			return result;
		}

		// Whenever we mutate the game context this way, we're doing something like a "transaction," it needs to be locked
		// so that simultaneous edits do not occur (e.g., by performing a game action while we are mutating the hand)
		// Any changes we make at this point are not safe, we will have to end the game
		gameContext.getLock().lock();
		try {
			// We now actually mutate the game context
			Card resultingCard = mutateGameContext.apply(gameContext);
			// The actions the player can take have changed so send a new request
			// This replaces existing requests
			if (gameContext.getActivePlayerId() == player.getId() && behaviour instanceof UnityClientBehaviour) {
				((UnityClientBehaviour) behaviour).updateActions(gameContext, gameContext.getValidActions());
				// TODO: What happens if we give the card to the bot? It could have nasty side effects!
			}
			// Tell the client what the card ID was
			result.cardId(resultingCard.getSourceCard().getCardId());

		} catch (Throwable ex) {
			// Any other exceptions are not recoverable, so the game MUST end
			Tracing.error(ex, span, true);
			// Lock is reentrant here
			gameContext.loseBothPlayers();
		} finally {
			gameContext.getLock().unlock();
			span.finish();
			scope.close();
		}

		return result;
	}

	@NotNull
	static String getPutCardAddress(String gameId) {
		return "Editor." + gameId + ".putCard";
	}

	/**
	 * Indicates whether the specified context should be editable.
	 * <p>
	 * All matchmaking games in Spellsource are currently editable.
	 *
	 * @param serverGameContext
	 * @return
	 */
	static boolean isEditable(ServerGameContext serverGameContext) {
		return serverGameContext.getGameId() != null;
	}
}
