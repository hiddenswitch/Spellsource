package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.util.*;
import com.hiddenswitch.spellsource.models.*;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.shared.threat.GameStateValueBehaviour;
import net.demilich.metastone.game.logic.GameLogic;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Created by bberman on 12/7/16.
 */
public class BotsImpl extends AbstractService<BotsImpl> implements Bots {
	private static Logger logger = LoggerFactory.getLogger(Bots.class);
	private RpcClient<Accounts> accounts;
	private RpcClient<Logic> logic;
	private RpcClient<Matchmaking> matchmaking;
	private List<UserRecord> bots = new ArrayList<>();
	private Queue<UserRecord> unusedBots = new ConcurrentLinkedQueue<>();
	private SuspendableMap<GameId, UserId> botGames;
	private Supplier<? extends Behaviour> botBehaviour = GameStateValueBehaviour::new;
	private Registration registration;

	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		super.start();
		botGames = SharedData.getClusterWideMap("BotsImpl/gametoBot", vertx);
		accounts = Rpc.connect(Accounts.class, vertx.eventBus());
		logic = Rpc.connect(Logic.class, vertx.eventBus());
		matchmaking = Rpc.connect(Matchmaking.class, vertx.eventBus());
		registration = Rpc.register(this, Bots.class, vertx.eventBus());
	}

	@Override
	@Suspendable
	public MulliganResponse mulligan(MulliganRequest request) {
		// Reject cards that cost more than 3
		MulliganResponse response = new MulliganResponse();
		response.discardedCards = request.cards.stream().filter(c -> c.getBaseManaCost() > 3).collect(Collectors.toList());
		return response;
	}

	@Override
	@Suspendable
	public RequestActionResponse requestAction(RequestActionRequest request) {
		RequestActionResponse response = new RequestActionResponse();

		// Use execute blocking to improve throughput
		response.gameAction = awaitResult(callback -> vertx.executeBlocking(done -> {
			logger.debug("requestAction: Requesting action from behaviour.");
			final Behaviour behaviour = botBehaviour.get();

			final GameContext context = new GameContext();
			context.setLogic(new GameLogic());
			context.setDeckFormat(request.format);
			context.setGameState(request.gameState);
			context.setActivePlayerId(request.playerId);

			try {
				final GameAction result = behaviour.requestAction(context, context.getPlayer(request.playerId), request.validActions);
				logger.debug("requestAction: Bot successfully chose action");
				done.handle(Future.succeededFuture(
						result));

			} catch (Throwable t) {
				logger.error("requestAction: Bot failed to choose an action due to an exception", t);
				done.handle(Future.failedFuture(t));
			}

		}, false, callback));


		return response;
	}

	@Override
	public BotsStartGameResponse startGame(BotsStartGameRequest request) throws SuspendExecution, InterruptedException {
		logger.debug("startGame: Starting a bot game for userId " + request.getUserId());
		// The player has been waiting too long. Match to an AI.
		// Retrieve a bot and use it to play against the opponent
		BotsStartGameResponse response = new BotsStartGameResponse();
		UserRecord bot = pollBot();
		GameId gameId = GameId.create();
		String botDeckId = getRandomDeck(bot);
		botGames.put(gameId, new UserId(bot.getId()));
		MatchCreateResponse matchCreateResponse = matchmaking.sync().createMatch(MatchCreateRequest.botMatch(gameId, new UserId(request.getUserId()), new UserId(bot.getId()), new DeckId(request.getDeckId()), new DeckId(botDeckId)));
		response.setGameId(matchCreateResponse.getCreateGameSessionResponse().gameId);
		response.setBotUserId(bot.getId());
		response.setBotDeckId(botDeckId);
		return response;
	}

	protected String getRandomDeck(UserRecord bot) {
		// TODO: Prevent the bot from choosing a tavern brawl configuration here.
		return bot.getDecks().get(RandomUtils.nextInt(0, bot.getDecks().size()));
	}

	private String getDeckByName(UserRecord bot, String deckName) throws SuspendExecution, InterruptedException {
		GetCollectionResponse deckCollections = Rpc.connect(Inventory.class, vertx.eventBus()).sync()
				.getCollection(GetCollectionRequest.decks(bot.getId(), bot.getDecks()));
		return deckCollections
				.getResponses()
				.stream()
				.map(GetCollectionResponse::asInventoryCollection)
				.filter(ic -> ic.getName().equals(deckName))
				.findFirst()
				.orElseThrow(RuntimeException::new).getId();
	}

	@Override
	public NotifyGameOverResponse notifyGameOver(NotifyGameOverRequest request) throws InterruptedException, SuspendExecution {
		// Return the bot servicing this game to the pool.
		UserId bot = botGames.remove(new GameId(request.getGameId()));
		if (bot == null) {
			return new NotifyGameOverResponse();
		}
		unusedBots.add(Accounts.findOne(bot.toString()));
		return new NotifyGameOverResponse();
	}

	private UserRecord pollBot() throws SuspendExecution, InterruptedException {
		if (bots.size() == 0) {
			// Retrieve existing bot accounts
			List<JsonObject> botIdRecords = Accounts.find(getMongo(), QuickJson.json("bot", true), new FindOptions().setFields(QuickJson.json("_id", 1)));

			List<String> botIds = botIdRecords.stream().map(o -> o.getString("_id")).collect(Collectors.toList());

			for (String id : botIds) {
				bots.add(accounts.sync().get(id));
			}
		}

		if (unusedBots.size() == 0) {
			List<String> newBotIds = new ArrayList<>();
			// At least 2 bots
			final int maxBots = Math.max(bots.size() + 1, 2);
			for (int i = bots.size(); i < maxBots; i++) {
				CreateAccountResponse response = accounts.sync().createAccount(new CreateAccountRequest()
						.withName("Botcharles")
						.withEmailAddress("botid" + Integer.toString(i) + "@hiddenswitch.com")
						.withPassword("securebotpassword")
						.withBot(true));

				logic.sync().initializeUser(new InitializeUserRequest(response.getUserId()));
				newBotIds.add(response.getUserId());
			}

			for (String id : newBotIds) {
				final UserRecord record = accounts.sync().get(id);
				bots.add(record);
				unusedBots.add(record);
			}
		}

		return unusedBots.poll();
	}

	public Supplier<? extends Behaviour> getBotBehaviour() {
		return botBehaviour;
	}

	public void setBotBehaviour(Supplier<? extends Behaviour> botBehaviour) {
		this.botBehaviour = botBehaviour;
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		Rpc.unregister(registration);
	}
}
