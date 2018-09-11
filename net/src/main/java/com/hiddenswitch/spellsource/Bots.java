package com.hiddenswitch.spellsource;

import com.github.fromage.quasi.fibers.SuspendExecution;
import com.github.fromage.quasi.fibers.Suspendable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.github.fromage.quasi.strands.Strand;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.concurrent.SuspendableMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.ext.mongo.FindOptions;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.logic.GameLogic;
import net.demilich.metastone.game.shared.threat.GameStateValueBehaviour;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * A service that processes bot actions, mulligans and conveniently creates bot games.
 */
public interface Bots {
	Logger LOGGER = LoggerFactory.getLogger(Bots.class);
	AtomicReference<Supplier<? extends Behaviour>> BEHAVIOUR = new AtomicReference<>(GameStateValueBehaviour::new);

	/**
	 * Decide which cards to mulligan given a starting hand.
	 *
	 * @param request A request containing the cards to choose from.
	 * @return A response that specifies which cards to mulligan.
	 */
	@Suspendable
	static MulliganResponse mulligan(MulliganRequest request) {
		// Reject cards that cost more than 3
		MulliganResponse response = new MulliganResponse();
		response.discardedCards = request.cards.stream().filter(c -> c.getBaseManaCost() > 3).collect(Collectors.toList());
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
		RequestActionResponse response = new RequestActionResponse();
		// Use execute blocking to yield here
		LOGGER.debug("requestAction: Requesting action from behaviour.");
		final Behaviour behaviour = getBehaviour().get();
		// See if there's a cache of bot plans for this action
		if (behaviour instanceof GameStateValueBehaviour) {
			GameStateValueBehaviour gsvb = (GameStateValueBehaviour) behaviour;
			SuspendableMap<GameId, Buffer> map = SuspendableMap.getOrCreate("Bots::indexPlans");
			GameId gameId = request.gameId;
			Buffer buf = map.get(gameId);
			if (buf != null) {
				List<Integer> indexPlan = Json.decodeValue(buf, new TypeReference<List<Integer>>() {
				});
				gsvb.setIndexPlan(new ArrayDeque<>(indexPlan));
			}

			delegateRequestAction(request, response, gsvb);

			// Save the new index plan
			Deque<Integer> indexPlan = gsvb.getIndexPlan();
			if (indexPlan != null) {
				map.put(gameId, Json.encodeToBuffer(new ArrayList<>(indexPlan)));
			} else {
				map.remove(gameId);
			}
		} else {
			delegateRequestAction(request, response, behaviour);
		}

		return response;
	}

	@Suspendable
	static void delegateRequestAction(RequestActionRequest request, RequestActionResponse response, Behaviour behaviour) {
		final GameContext context = new GameContext();
		context.setLogic(new GameLogic());
		context.setDeckFormat(request.format);
		context.setGameState(request.gameState);
		context.setActivePlayerId(request.playerId);

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

			LOGGER.debug("requestAction: Bot successfully chose action");
			response.gameAction = result;

		} catch (Throwable t) {
			LOGGER.error("requestAction: Bot failed to choose an action due to an exception", t);
			throw t;
		}
	}

	static UserId pollBotId() throws SuspendExecution, InterruptedException {
		// TODO: Contains synchronization issues, but the worst that will happen is that a single bot plays multiple games
		List<String> bots = getBotIds();

		Collections.shuffle(bots);
		SuspendableMap<UserId, GameId> games = Games.getGames();

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
}
