package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Sets;
import com.hiddenswitch.proto3.net.Accounts;
import com.hiddenswitch.proto3.net.Draft;
import com.hiddenswitch.proto3.net.Server;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.client.models.CreateAccountRequest;
import com.hiddenswitch.proto3.net.client.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.client.models.LoginRequest;
import com.hiddenswitch.proto3.net.impl.auth.TokenAuthProvider;
import com.hiddenswitch.proto3.net.impl.util.DraftRecord;
import com.hiddenswitch.proto3.net.impl.util.FriendRecord;
import com.hiddenswitch.proto3.net.impl.util.HandlerFactory;
import com.hiddenswitch.proto3.net.impl.util.UserRecord;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.models.MatchCancelResponse;
import com.hiddenswitch.proto3.net.util.ApiKeyAuthHandler;
import com.hiddenswitch.proto3.net.util.Mongo;
import com.hiddenswitch.proto3.net.util.Serialization;
import com.hiddenswitch.proto3.net.util.WebResult;
import io.vertx.core.Verticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hiddenswitch.proto3.net.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;

/**
 * An implementation of an <a href="https://www.linkedin.com/pulse/api-gateway-pattern-subhash-chandran">API gateway</a>
 * for user-accessible services in Minionate.
 *
 * @see Server for a detailed description on how to add methods to the API gateway.
 */
public class ServerImpl extends AbstractService<ServerImpl> implements Server {
	static Logger logger = LoggerFactory.getLogger(ServerImpl.class);
	CardsImpl cards = new CardsImpl();
	AccountsImpl accounts = new AccountsImpl();
	GamesImpl games = new GamesImpl();
	MatchmakingImpl matchmaking = new MatchmakingImpl();
	BotsImpl bots = new BotsImpl();
	LogicImpl logic = new LogicImpl();
	DecksImpl decks = new DecksImpl();
	InventoryImpl inventory = new InventoryImpl();
	DraftImpl drafts = new DraftImpl();
	List<String> deployments = new ArrayList<>();
	HttpServer server;

	@Override
	@Suspendable
	public void start() throws RuntimeException, SuspendExecution {
		super.start();
		server = vertx.createHttpServer(new HttpServerOptions()
				.setHost("0.0.0.0")
				.setPort(8080));
		Router router = Router.router(vertx);

		configureMongo();

		try {
			for (Verticle verticle : Arrays.asList(cards, accounts, games, matchmaking, bots, logic, decks, inventory, drafts)) {
				final String name = verticle.getClass().getName();
				logger.info("Deploying " + name + "...");
				String deploymentId = Sync.awaitResult(done -> vertx.deployVerticle(verticle, done));
				deployments.add(deploymentId);
				logger.info("Deployed " + name + " with ID " + deploymentId);
			}

			logger.info("Configuring router...");

			final TokenAuthProvider authProvider = new TokenAuthProvider(vertx);
			final ApiKeyAuthHandler authHandler = ApiKeyAuthHandler.create(authProvider, "X-Auth-Token");
			final BodyHandler bodyHandler = BodyHandler.create();

			// All routes need logging.
			router.route().handler(LoggerHandler.create());

			// CORS
			router.route().handler(CorsHandler.create("*")
					.allowedHeader("Content-Type")
					.allowedHeader("X-Auth-Token")
					.exposedHeader("Content-Type")
					.allowCredentials(true)
					.allowedMethods(Sets.newHashSet(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS)));

			//add "content-type=application/json" to all responses
			router.route().handler(context -> {
				context.response().putHeader("Content-Type", "application/json");
				context.next();
			});

			router.route("/v1/accounts/:targetUserId")
					.handler(authHandler);
			router.route("/v1/accounts/:targetUserId")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler("targetUserId", this::getAccount));

			router.route("/v1/accounts")
					.handler(bodyHandler);
			router.route("/v1/accounts")
					.method(HttpMethod.POST)
					.handler(HandlerFactory.handler(LoginRequest.class, this::login));
			router.route("/v1/accounts")
					.method(HttpMethod.PUT)
					.handler(HandlerFactory.handler(CreateAccountRequest.class, this::createAccount));
			router.route("/v1/accounts")
					.method(HttpMethod.GET)
					.handler(authHandler);
			router.route("/v1/accounts")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler(GetAccountsRequest.class, this::getAccounts));


			router.route("/v1/decks")
					.handler(bodyHandler);
			router.route("/v1/decks")
					.handler(authHandler);
			router.route("/v1/decks")
					.method(HttpMethod.PUT)
					.handler(HandlerFactory.handler(DecksPutRequest.class, this::decksPut));
			router.route("/v1/decks")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler(this::decksGetAll));

			router.route("/v1/decks/:deckId")
					.handler(bodyHandler);
			router.route("/v1/decks/:deckId")
					.handler(authHandler);

			router.route("/v1/decks/:deckId")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler("deckId", this::decksGet));

			router.route("/v1/decks/:deckId")
					.method(HttpMethod.POST)
					.handler(HandlerFactory.handler(DecksUpdateCommand.class, "deckId", this::decksUpdate));

			router.route("/v1/decks/:deckId")
					.method(HttpMethod.DELETE)
					.handler(HandlerFactory.handler("deckId", this::decksDelete));

			router.route("/v1/matchmaking/constructed")
					.handler(bodyHandler);
			router.route("/v1/matchmaking/constructed")
					.handler(authHandler);
			router.route("/v1/matchmaking/constructed")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler(this::matchmakingConstructedGet));
			router.route("/v1/matchmaking/constructed")
					.method(HttpMethod.DELETE)
					.handler(HandlerFactory.handler(this::matchmakingConstructedDelete));

			router.route("/v1/matchmaking/constructed/queue")
					.handler(bodyHandler);
			router.route("/v1/matchmaking/constructed/queue")
					.handler(authHandler);
			router.route("/v1/matchmaking/constructed/queue")
					.method(HttpMethod.PUT)
					.handler(HandlerFactory.handler(MatchmakingQueuePutRequest.class, this::matchmakingConstructedQueuePut));

			router.route("/v1/matchmaking/constructed/queue")
					.method(HttpMethod.DELETE)
					.handler(HandlerFactory.handler(this::matchmakingConstructedQueueDelete));

			router.route("/v1/friends")
					.handler(bodyHandler);
			router.route("/v1/friends")
					.handler(authHandler);
			router.route("/v1/friends")
					.method(HttpMethod.PUT)
					.handler(HandlerFactory.handler(FriendPutRequest.class, this::putFriend));

			router.route("/v1/friends/:friendId")
					.handler(bodyHandler);
			router.route("/v1/friends/:friendId")
					.handler(authHandler);
			router.route("/v1/friends/:friendId")
					.method(HttpMethod.DELETE)
					.handler(HandlerFactory.handler("friendId", this::unFriend));

			router.route("/v1/drafts")
					.handler(bodyHandler);
			router.route("/v1/drafts")
					.handler(authHandler);
			router.route("/v1/drafts")
					.method(HttpMethod.GET)
					.handler(HandlerFactory.handler(this::draftsGet));

			router.route("/v1/drafts")
					.method(HttpMethod.POST)
					.handler(HandlerFactory.handler(DraftsPostRequest.class, this::draftsPost));

			router.route("/v1/drafts/hero")
					.handler(bodyHandler);
			router.route("/v1/drafts/hero")
					.handler(authHandler);
			router.route("/v1/drafts/hero")
					.method(HttpMethod.PUT)
					.handler(HandlerFactory.handler(DraftsChooseHeroRequest.class, this::draftsChooseHero));

			router.route("/v1/drafts/cards")
					.handler(bodyHandler);
			router.route("/v1/drafts/cards")
					.handler(authHandler);
			router.route("/v1/drafts/cards")
					.method(HttpMethod.PUT)
					.handler(HandlerFactory.handler(DraftsChooseCardRequest.class, this::draftsChooseCard));


			logger.info("Router configured.");
			HttpServer listening = awaitResult(done -> server.requestHandler(router::accept).listen(done));
			logger.info("Listening on port " + Integer.toString(server.actualPort()));
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}

	@Override
	public WebResult<GetAccountsResponse> getAccount(RoutingContext context, String userId, String targetUserId) throws SuspendExecution, InterruptedException {
		// TODO: If it's an ally, send all the information
		if (userId.equals(targetUserId)) {
			final Account account = getAccount(userId);
			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(account)));
		} else {
			UserRecord record = accounts.get(userId);
			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(new Account()
					.name(record.getProfile().getDisplayName())
					.id(targetUserId))));
		}
	}


	@Override
	public WebResult<GetAccountsResponse> getAccounts(RoutingContext context, String userId, GetAccountsRequest request) throws SuspendExecution, InterruptedException {
		return null;
	}

	@Override
	public WebResult<CreateAccountResponse> createAccount(RoutingContext context, CreateAccountRequest request) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.proto3.net.models.CreateAccountResponse internalResponse = accounts.createAccount(request.getEmail(), request.getPassword(), request.getName());

		if (internalResponse.isInvalidEmailAddress()) {
			return WebResult.failed(new RuntimeException("Invalid email address."));
		} else if (internalResponse.isInvalidPassword()) {
			return WebResult.failed(new RuntimeException("Invalid password."));
		}

		// Initialize the collection
		final String userId = internalResponse.getUserId();
		logic.initializeUser(new InitializeUserRequest().withUserId(userId));

		final Account account = getAccount(userId);
		return WebResult.succeeded(new CreateAccountResponse()
				.loginToken(internalResponse.getLoginToken().getToken())
				.account(account));
	}

	@Override
	public WebResult<com.hiddenswitch.proto3.net.client.models.LoginResponse> login(RoutingContext context, com.hiddenswitch.proto3.net.client.models.LoginRequest request) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.proto3.net.models.LoginResponse internalResponse = accounts.login(request.getEmail(), request.getPassword());

		if (internalResponse.isBadPassword()) {
			return WebResult.failed(new RuntimeException("Invalid password."));
		} else if (internalResponse.isBadEmail()) {
			return WebResult.failed(new RuntimeException("Invalid email address."));
		}

		return WebResult.succeeded(new com.hiddenswitch.proto3.net.client.models.LoginResponse()
				.account(getAccount(internalResponse.getToken().getAccessKey()))
				.loginToken(internalResponse.getToken().getToken()));
	}

	@Override
	public WebResult<DecksPutResponse> decksPut(RoutingContext context, String userId, DecksPutRequest request) throws SuspendExecution, InterruptedException {
		final HeroClass heroClass = HeroClass.valueOf(request.getHeroClass());

		DeckCreateResponse internalResponse = decks.createDeck(new DeckCreateRequest()
				.withName(request.getName())
				.withInventoryIds(request.getInventoryIds())
				.withHeroClass(heroClass)
				.withUserId(userId));

		return WebResult.succeeded(new DecksPutResponse().deckId(internalResponse.getDeckId()));
	}

	private WebResult<DecksGetResponse> getDeck(String userId, String deckId) throws SuspendExecution, InterruptedException {
		GetCollectionResponse updatedCollection = inventory.getCollection(new GetCollectionRequest()
				.withUserId(userId)
				.withDeckId(deckId));

		return WebResult.succeeded(new DecksGetResponse()
				.inventoryIdsSize(updatedCollection.getInventoryRecords().size())
				.collection(updatedCollection.asInventoryCollection()));
	}

	@Override
	public WebResult<DecksGetResponse> decksUpdate(RoutingContext context, String userId, String deckId, DecksUpdateCommand updateCommand) throws SuspendExecution, InterruptedException {
		decks.updateDeck(new DeckUpdateRequest(userId, deckId, updateCommand));

		// Get the updated collection
		return getDeck(userId, deckId);
	}

	@Override
	public WebResult<DecksGetResponse> decksGet(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException {
		return getDeck(userId, deckId);
	}

	@Override
	public WebResult<DecksGetAllResponse> decksGetAll(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		List<String> decks = accounts.get(userId).getDecks();

		List<DecksGetResponse> responses = new ArrayList<>();
		for (String deck : decks) {
			responses.add(getDeck(userId, deck).result());
		}

		return WebResult.succeeded(new DecksGetAllResponse().decks(responses));
	}

	@Override
	public WebResult<DeckDeleteResponse> decksDelete(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException {
		return WebResult.succeeded(decks.deleteDeck(new DeckDeleteRequest(deckId)));
	}

	@Override
	public WebResult<MatchmakingQueuePutResponse> matchmakingConstructedQueuePut(RoutingContext routingContext, String userId, MatchmakingQueuePutRequest request) throws SuspendExecution, InterruptedException {
		MatchmakingRequest internalRequest = new MatchmakingRequest(request, userId).withBotMatch(request.getCasual());
		MatchmakingResponse internalResponse = matchmaking.matchmakeAndJoin(internalRequest);

		// Compute the appropriate response
		MatchmakingQueuePutResponse userResponse = new MatchmakingQueuePutResponse();
		if (internalResponse.getConnection() != null) {
			final JavaSerializationObject connection;
			try {
				connection = new JavaSerializationObject()
						.javaSerialized(Serialization.serializeBase64(internalResponse.getConnection()));
			} catch (IOException e) {
				routingContext.fail(e);

				return WebResult.failed(e);
			}
			userResponse.connection(connection);
			userResponse.unityConnection(new MatchmakingQueuePutResponseUnityConnection()
					.url(internalResponse.getConnection().getUrl())
					.firstMessage(new ClientToServerMessage()
							.messageType(MessageType.FIRST_MESSAGE)
							.firstMessage(new ClientToServerMessageFirstMessage()
									.playerKey(internalResponse.getConnection().getPlayerKey())
									.playerSecret(internalResponse.getConnection().getPlayerSecret()))));
		}

		// Determine status code
		int statusCode = 200;
		if (internalResponse.getRetry() != null) {
			userResponse.retry(new MatchmakingQueuePutRequest()
					.deckId(internalResponse.getRetry().getDeckId()));
			statusCode = 202;
		}

		return WebResult.succeeded(statusCode, userResponse);
	}

	@Override
	public WebResult<com.hiddenswitch.proto3.net.client.models.MatchCancelResponse> matchmakingConstructedQueueDelete(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		MatchCancelResponse internalResponse = matchmaking.cancel(new MatchCancelRequest(userId));

		com.hiddenswitch.proto3.net.client.models.MatchCancelResponse response =
				new com.hiddenswitch.proto3.net.client.models.MatchCancelResponse()
						.isCanceled(internalResponse.getCanceled());

		return WebResult.succeeded(response);
	}

	@Override
	public WebResult<MatchConcedeResponse> matchmakingConstructedDelete(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		MatchCancelResponse response = matchmaking.cancel(new MatchCancelRequest(userId));
		if (response == null) {
			return WebResult.failed(new RuntimeException());
		}
		games.concedeGameSession(new ConcedeGameSessionRequest(response.getGameId(), response.getPlayerId()));
		return WebResult.succeeded(new MatchConcedeResponse().isConceded(true));
	}

	@Override
	public WebResult<GameState> matchmakingConstructedGet(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		CurrentMatchResponse response = matchmaking.getCurrentMatch(new CurrentMatchRequest(userId));
		if (response.getGameId() == null) {
			return WebResult.failed(404, new NullPointerException("Game not found."));
		}

		return WebResult.succeeded(games.getClientGameState(response.getGameId(), userId));
	}

	@Override
	public WebResult<FriendPutResponse> putFriend(RoutingContext context, String userId, FriendPutRequest req)
			throws SuspendExecution, InterruptedException {
		String friendId = req.getFriendId();

		//lookup friend user record
		UserRecord friendAccount = accounts.get(req.getFriendId());

		//if no friend, return 404
		if (friendAccount == null) {
			return WebResult.failed(404, new Exception("Friend account not found"));
		}

		//lookup own user account
		UserRecord myAccount = (UserRecord) context.user();


		//check if already friends
		if (myAccount.isFriend(friendId)) {
			return WebResult.failed(409, new Exception("Friend already friend"));
		}

		long startOfFriendship = System.currentTimeMillis();

		FriendRecord friendRecord = new FriendRecord().setFriendId(friendId).setSince(startOfFriendship)
				.setDisplayName(friendAccount.getProfile().getDisplayName());
		FriendRecord friendOfFriendRecord = new FriendRecord().setFriendId(userId).setSince(startOfFriendship)
				.setDisplayName(friendAccount.getProfile().getDisplayName());

		//update both sides
		Accounts.update(getMongo(), userId, json("$push", json("friends", json(friendRecord))));
		Accounts.update(getMongo(), friendId, json("$push", json("friends",
				json(friendOfFriendRecord))));


		FriendPutResponse response = new FriendPutResponse().friend(friendRecord.toFriendDto());
		return WebResult.succeeded(response);
	}

	@Override
	public WebResult<UnfriendResponse> unFriend(RoutingContext context, String userId, String friendId)
			throws SuspendExecution, InterruptedException {
		UserRecord myAccount = (UserRecord) context.user();

		//lookup friend user record
		UserRecord friendAccount = accounts.get(friendId);

		//doesn't exist?
		if (friendAccount == null) {
			return WebResult.failed(404, new Exception("Friend account not found"));
		}

		//friends?
		FriendRecord friendRecord = myAccount.getFriendById(friendId);
		if (friendRecord == null) {
			return WebResult.failed(404, new Exception("Not friends"));
		}

		//Oops
		FriendRecord friendOfFriendRecord = friendAccount.getFriendById(userId);
		if (friendOfFriendRecord == null) {
			return WebResult.failed(418, new Exception("Friends not balanced. OOPS"));
		}

		//delete from both sides
		Accounts.update(getMongo(), userId, json("$pull",
				json("friends", json("friendId", friendId))));
		Accounts.update(getMongo(), friendId, json("$pull",
				json("friends", json("friendId", userId))));

		UnfriendResponse response = new UnfriendResponse().deletedFriend(friendRecord.toFriendDto());
		return WebResult.succeeded(response);
	}

	@Override
	public WebResult<DraftState> draftsGet(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		DraftRecord record = drafts.get(new GetDraftRequest().withUserId(userId));
		if (record == null) {
			return WebResult.failed(404, new NullPointerException("You have not started a draft. Start one first."));
		}

		return WebResult.succeeded(Draft.toDraftState(record.getPublicDraftState()));
	}

	@Override
	public WebResult<DraftState> draftsPost(RoutingContext context, String userId, DraftsPostRequest request) throws SuspendExecution, InterruptedException {
		if (null != request.getStartDraft()
				&& request.getStartDraft()) {
			try {
				return WebResult.succeeded(
						Draft.toDraftState(
								drafts.doDraftAction(new DraftActionRequest().withUserId(userId))
										.getPublicDraftState()));
			} catch (NullPointerException unexpectedRequest) {
				return WebResult.failed(400, unexpectedRequest);
			}
		} else if (null != request.getRetireEarly()
				&& request.getRetireEarly()) {
			return WebResult.succeeded(
					Draft.toDraftState(
							drafts.retireDraftEarly(new RetireDraftRequest().withUserId(userId))
									.getRecord()
									.getPublicDraftState()));

		}

		return WebResult.failed(400, new UnsupportedOperationException("You must choose a valid action."));
	}

	@Override
	public WebResult<DraftState> draftsChooseHero(RoutingContext context, String userId, DraftsChooseHeroRequest request) throws SuspendExecution, InterruptedException {
		try {
			DraftRecord record = drafts.doDraftAction(new DraftActionRequest()
					.withUserId(userId)
					.withHeroIndex(request.getHeroIndex()));

			return WebResult.succeeded(Draft.toDraftState(record.getPublicDraftState()));
		} catch (NullPointerException unexpectedRequest) {
			return WebResult.failed(400, unexpectedRequest);
		} catch (Exception ignored) {
			return WebResult.failed(400, new UnsupportedOperationException("You must choose a hero index."));
		}
	}

	@Override
	public WebResult<DraftState> draftsChooseCard(RoutingContext context, String userId, DraftsChooseCardRequest request) throws SuspendExecution, InterruptedException {
		try {
			DraftRecord record = drafts.doDraftAction(new DraftActionRequest()
					.withUserId(userId)
					.withCardIndex(request.getCardIndex()));

			return WebResult.succeeded(Draft.toDraftState(record.getPublicDraftState()));
		} catch (NullPointerException unexpectedRequest) {
			return WebResult.failed(400, unexpectedRequest);
		} catch (Exception ignored) {
			return WebResult.failed(400, new UnsupportedOperationException("You must choose a card index, or the draft has been completed."));
		}
	}

	private Account getAccount(String userId) throws SuspendExecution, InterruptedException {
		// Get the personal collection
		UserRecord record = accounts.get(userId);
		GetCollectionResponse personalCollection = inventory.getCollection(GetCollectionRequest.user(record.getId()));

		// Get the decks
		GetCollectionResponse deckCollections = inventory.getCollection(GetCollectionRequest.decks(record.getDecks()));

		final String displayName = record.getProfile().getDisplayName();
		return new Account()
				.id(record.getId())
				.decks(deckCollections.getResponses().stream().map(GetCollectionResponse::asInventoryCollection).collect(toList()))
				.personalCollection(personalCollection.asInventoryCollection())
				.email(record.getProfile().getEmailAddress())
				.name(displayName);
	}


	private void configureMongo() {
		// Get the Mongo URL
		if (System.getProperties().containsKey("mongo.url")
				|| System.getenv().containsKey("MONGO_URL")) {
			String mongoUrl = System.getProperties().getProperty("mongo.url", System.getenv().getOrDefault("MONGO_URL", "mongodb://localhost:27017/local"));
			Mongo.mongo().connect(vertx, mongoUrl);
		} else {
			Mongo.mongo().startEmbedded().connect(vertx);
		}

	}
}
