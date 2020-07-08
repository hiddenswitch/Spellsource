package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.fasterxml.jackson.core.type.TypeReference;
import co.paralleluniverse.strands.Strand;
import com.hiddenswitch.spellsource.common.Tracing;
import io.vertx.core.Promise;
import io.vertx.core.json.jackson.JacksonCodec;
import net.demilich.metastone.game.decks.DeckCreateRequest;
import com.hiddenswitch.spellsource.net.impl.GameId;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import com.hiddenswitch.spellsource.net.models.*;
import com.hiddenswitch.spellsource.net.impl.Mongo;
import com.hiddenswitch.spellsource.net.concurrent.SuspendableMap;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.behaviour.GameStateValueBehaviour;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * A service that processes bot actions, mulligans and conveniently creates bot games.
 */
public interface Bots {
	// Return a non-parallelized GSVB instance
	AtomicReference<Supplier<? extends Behaviour>> BEHAVIOUR = new AtomicReference<>(() ->
			new GameStateValueBehaviour()
					.setParallel(false)
					.setMaxDepth(2)
					.setTimeout(3500L)
					.setLethalTimeout(11000L)
					.setThrowsExceptions(false));
	TypeReference<List<Integer>> LIST_INTEGER_TYPE = new TypeReference<>() {
	};
	String BOTS_INDEX_PLANS = "Bots.indexPlans";

	/**
	 * Decide which cards to mulligan given a starting hand.
	 *
	 * @param request A request containing the cards to choose from.
	 * @return A response that specifies which cards to mulligan.
	 */
	@Suspendable
	static MulliganResponse mulligan(BotMulliganRequest request) {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Bots/mulligan").start();
		// Reject cards that cost more than 3
		MulliganResponse response = new MulliganResponse();
		response.discardedCards = request.cards.stream().filter(c -> c.getBaseManaCost() > 3).collect(Collectors.toList());
		span.finish();
		return response;
	}

	/**
	 * Decides which action to perform given a list of possibilities and the current game state.
	 *
	 * @param request The game state and options for an action.
	 * @return The selected action.
	 */
	@Suspendable
	static RequestActionResponse requestAction(RequestActionRequest request) {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Bots/requestAction")
				.withTag("gameId", request.gameId.toString())
				.asChildOf(request.spanContext)
				.start();
		Scope scope = tracer.activateSpan(span);
		RequestActionResponse response;

		try {
			// Use execute blocking to yield here
			final Behaviour behaviour = getBehaviour().get();
			// See if there's a cache of bot plans for this action
			if (behaviour instanceof GameStateValueBehaviour) {
				GameStateValueBehaviour gsvb = (GameStateValueBehaviour) behaviour;
				SuspendableMap<GameId, Buffer> map = SuspendableMap.getOrCreate(BOTS_INDEX_PLANS);
				GameId gameId = request.gameId;
				Buffer buf = map.get(gameId);
				if (buf != null) {
					List<Integer> indexPlan = JacksonCodec.decodeValue(buf, LIST_INTEGER_TYPE);
					gsvb.setIndexPlan(new ArrayDeque<>(indexPlan));
					span.setTag("indexPlan", indexPlan.size());
				}
				response = delegateRequestAction(request, gsvb);
				span.log("responseReceived");

				// Save the new index plan
				Deque<Integer> indexPlan = gsvb.getIndexPlan();
				if (indexPlan != null) {
					map.put(gameId, Json.encodeToBuffer(new ArrayList<>(indexPlan)));
				} else {
					map.remove(gameId);
				}
			} else {
				response = delegateRequestAction(request, behaviour);
			}
		} finally {
			span.finish();
			scope.close();
		}

		return response;
	}

	@Suspendable
	static RequestActionResponse delegateRequestAction(RequestActionRequest request, Behaviour behaviour) {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.activeSpan();
		RequestActionResponse response = new RequestActionResponse();
		final GameContext context = new GameContext();
		context.setLogic(new GameLogic());
		context.setDeckFormat(request.format);
		context.setGameState(request.gameState);
		int playerId;
		if (context.getActivePlayerId() != request.playerId) {
			span.setTag("botRequestedPlayedId", request.playerId);
			span.setTag("activePlayerId", request.gameState.getActivePlayerId());
			Tracing.error(new IllegalArgumentException("A bot request action was done for a botId that did not match the active player in the context. The botId should be 1, the active player was " + context.getActivePlayerId() + " and the requested player was " + request.playerId), span, false);
			playerId = 1;
		} else {
			playerId = request.playerId;
		}
		context.setActivePlayerId(playerId);
		try {
			GameAction result = awaitResult(res -> Vertx.currentContext().executeBlocking(fut -> {
				// TODO: We shouldn't really tie up a general blocking executor for this computation.
				try {
					long startTime = System.currentTimeMillis();
					final GameAction res1 = behaviour.requestAction(context, context.getPlayer(request.playerId), request.validActions);
					long endTime = System.currentTimeMillis();
					long thinkingDelay = getDefaultBotThinkingDelay();
					long waitTime = Math.max(thinkingDelay - endTime + startTime, 0);
					if (waitTime > 0L) {
						Strand.sleep(waitTime);
					}
					fut.complete(res1);
				} catch (Throwable t) {
					fut.fail(t);
				}
			}, false, res));

			response.gameAction = result;
		} catch (Throwable throwable) {
			Tracing.error(throwable, span);
			response.gameAction = request.validActions.get(0);
		}
		return response;
	}

	static UserId pollBotId() throws SuspendExecution, InterruptedException {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Bots/pollBotId")
				.start();
		try {
			List<String> bots = getBotIds();

			Collections.shuffle(bots);
			SuspendableMap<UserId, GameId> games = Games.getUsersInGames();

			for (String id : bots) {
				UserId key = new UserId(id);
				if (!games.containsKey(key)) {
					return key;
				}
			}

			CreateAccountResponse response = Accounts.createAccount(new CreateAccountRequest()
					.withName("Botcharles")
					.withEmailAddress("botid" + RandomStringUtils.randomAlphanumeric(32) + "@hiddenswitch.com")
					.withPassword("securebotpassword")
					.withBot(true));

			Logic.initializeUser(InitializeUserRequest.create(response.getUserId()));
			return new UserId(response.getUserId());
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
		}
	}

	static List<String> getBotIds() throws SuspendExecution, InterruptedException {
		return Mongo.mongo().findWithOptions(Accounts.USERS, json("bot", true), new FindOptions().setFields(json("_id", 1)))
				.stream()
				.map(jo -> jo.getString("_id"))
				.collect(Collectors.toList());
	}

	static String getRandomDeck(UserRecord bot) {
		// TODO: Prevent the bot from choosing a tavern brawl configuration here.
		return bot.getDecks().get(RandomUtils.nextInt(0, bot.getDecks().size()));
	}

	/**
	 * Removes the index (cached bot computation) for the specified game asynchronously.
	 *
	 * @param gameId The game to remove the index for.
	 * @return A promise with the index.
	 */
	static Promise<Buffer> removeIndex(GameId gameId) {
		Promise<Buffer> promise = Promise.promise();
		SuspendableMap.<GameId, Buffer>getOrCreate(BOTS_INDEX_PLANS, res -> {
			if (res.succeeded()) {
				res.result().remove(gameId, promise);
			} else {
				promise.fail(res.cause());
			}
		});
		return promise;
	}

	static Supplier<? extends Behaviour> getBehaviour() {
		return BEHAVIOUR.get();
	}

	/**
	 * Retreieves a value in milliseconds that the bot should sleep before sending its computed response to the server.
	 * <p>
	 * This value is helpful for debugging or simulating a real player.
	 *
	 * @return A delay in milliseconds
	 */
	static long getDefaultBotThinkingDelay() {
		return Long.parseLong(System.getenv().getOrDefault("SPELLSOURCE_BOT_THINKING_DELAY", System.getProperty("spellsource.bots.thinking_delay", "0")));
	}

	/**
	 * Updates the bot's decks to be the latest in the standard decks directory.
	 *
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	static void updateBotDeckList() throws SuspendExecution, InterruptedException {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Bots/updateBotDeckList")
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			// Refresh the bot decks
			List<JsonObject> bots = mongo().findWithOptions(Accounts.USERS, json("bot", true), new FindOptions().setFields(json("_id", 1, "decks", 1)));
			for (JsonObject bot : bots) {
				for (Object obj : bot.getJsonArray("decks")) {
					String deckId = (String) obj;
					Decks.deleteDeck(DeckDeleteRequest.create(deckId));
				}
				for (DeckCreateRequest req : Spellsource.spellsource().getStandardDecks()) {
					Decks.createDeck(req.withUserId(bot.getString("_id")));
				}
			}
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}
	}
}
