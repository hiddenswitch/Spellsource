package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.SuspendableMap;
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.QuickJson.json;

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
	static RequestActionResponse requestAction(RequestActionRequest request) throws InterruptedException {
		RequestActionResponse response = new RequestActionResponse();
		// Use execute blocking to yield here
		LOGGER.debug("requestAction: Requesting action from behaviour.");
		final Behaviour behaviour = getBehaviour().get();

		final GameContext context = new GameContext();
		context.setLogic(new GameLogic());
		context.setDeckFormat(request.format);
		context.setGameState(request.gameState);
		context.setActivePlayerId(request.playerId);

		try {
			final GameAction result = behaviour.requestAction(context, context.getPlayer(request.playerId), request.validActions);
			LOGGER.debug("requestAction: Bot successfully chose action");
			response.gameAction = result;

		} catch (Throwable t) {
			LOGGER.error("requestAction: Bot failed to choose an action due to an exception", t);
			throw t;
		}

		return response;
	}

	static String pollBotId() throws SuspendExecution, InterruptedException {
		List<String> bots = Mongo.mongo().findWithOptions(Accounts.USERS, json("bot", true), new FindOptions().setFields(json("_id", 1)))
				.stream()
				.map(jo -> jo.getString("_id"))
				.collect(Collectors.toList());

		Collections.shuffle(bots);
		SuspendableMap<UserId, GameId> games = Games.getGames();

		for (String id : bots) {
			if (!games.containsKey(new UserId(id))) {
				return id;
			}
		}

		CreateAccountResponse response = Accounts.createAccount(new CreateAccountRequest()
				.withName("Botcharles")
				.withEmailAddress("botid" + RandomStringUtils.randomAlphanumeric(32) + "@hiddenswitch.com")
				.withPassword("securebotpassword")
				.withBot(true));

		Logic.initializeUser(InitializeUserRequest.create(response.getUserId()));
		return response.getUserId();
	}

	static String getRandomDeck(UserRecord bot) {
		// TODO: Prevent the bot from choosing a tavern brawl configuration here.
		return bot.getDecks().get(RandomUtils.nextInt(0, bot.getDecks().size()));
	}

	static Supplier<? extends Behaviour> getBehaviour() {
		return BEHAVIOUR.get();
	}
}
