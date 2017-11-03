package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Sets;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.client.models.CreateAccountRequest;
import com.hiddenswitch.spellsource.client.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.client.models.LoginRequest;
import com.hiddenswitch.spellsource.impl.auth.TokenAuthProvider;
import com.hiddenswitch.spellsource.impl.util.DraftRecord;
import com.hiddenswitch.spellsource.impl.util.FriendRecord;
import com.hiddenswitch.spellsource.impl.util.HandlerFactory;
import com.hiddenswitch.spellsource.impl.util.MessageRecord;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.client.models.LoginResponse;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;

/**
 * An implementation of an <a href="https://www.linkedin.com/pulse/api-gateway-pattern-subhash-chandran">API gateway</a>
 * for user-accessible services in Spellsource.
 *
 * @see Gateway for a detailed description on how to add methods to the API gateway.
 */
public class GatewayImpl extends AbstractService<GatewayImpl> implements Gateway {
	public static final String version = "/v2";
	static Logger logger = LoggerFactory.getLogger(GatewayImpl.class);
	HttpServer server;

	@Override
	@Suspendable
	public void start() throws RuntimeException, SuspendExecution {
		super.start();
		server = vertx.createHttpServer(new HttpServerOptions()
				.setHost("0.0.0.0")
				.setPort(8080));
		Router router = Router.router(vertx);

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

		router.route(version + "/accounts/:targetUserId")
				.handler(authHandler);
		router.route(version + "/accounts/:targetUserId")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("targetUserId", this::getAccount));

		router.route(version + "/accounts")
				.handler(bodyHandler);
		router.route(version + "/accounts")
				.method(HttpMethod.POST)
				.handler(HandlerFactory.handler(LoginRequest.class, this::login));
		router.route(version + "/accounts")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(CreateAccountRequest.class, this::createAccount));
		router.route(version + "/accounts")
				.method(HttpMethod.GET)
				.handler(authHandler);
		router.route(version + "/accounts")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(GetAccountsRequest.class, this::getAccounts));


		router.route(version + "/decks")
				.handler(bodyHandler);
		router.route(version + "/decks")
				.handler(authHandler);
		router.route(version + "/decks")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(DecksPutRequest.class, this::decksPut));
		router.route(version + "/decks")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(this::decksGetAll));

		router.route(version + "/decks/:deckId")
				.handler(bodyHandler);
		router.route(version + "/decks/:deckId")
				.handler(authHandler);
		router.route(version + "/decks/:deckId")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("deckId", this::decksGet));

		router.route(version + "/decks/:deckId")
				.method(HttpMethod.POST)
				.handler(HandlerFactory.handler(DecksUpdateCommand.class, "deckId", this::decksUpdate));

		router.route(version + "/decks/:deckId")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("deckId", this::decksDelete));

		router.route(version + "/matchmaking/:queueId")
				.handler(bodyHandler);
		router.route(version + "/matchmaking/:queueId")
				.handler(authHandler);
		router.route(version + "/matchmaking/:queueId")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("queueId", this::matchmakingConstructedGet));
		router.route(version + "/matchmaking/constructed")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("queueId", this::matchmakingConstructedDelete));

		router.route(version + "/matchmaking/:queueId")
				.handler(bodyHandler);
		router.route(version + "/matchmaking/:queueId")
				.handler(authHandler);
		router.route(version + "/matchmaking/:queueId")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(MatchmakingQueuePutRequest.class, "queueId",
						this::matchmakingConstructedQueuePut));

		router.route(version + "/matchmaking/:queueId")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("queueId", this::matchmakingConstructedQueueDelete));

		router.route(version + "/friends")
				.handler(bodyHandler);
		router.route(version + "/friends")
				.handler(authHandler);
		router.route(version + "/friends")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(FriendPutRequest.class, this::putFriend));

		router.route(version + "/friends/:friendId")
				.handler(bodyHandler);
		router.route(version + "/friends/:friendId")
				.handler(authHandler);
		router.route(version + "/friends/:friendId")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("friendId", this::unFriend));

		router.route(version + "/drafts")
				.handler(bodyHandler);
		router.route(version + "/drafts")
				.handler(authHandler);
		router.route(version + "/drafts")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(this::draftsGet));

		router.route(version + "/drafts")
				.method(HttpMethod.POST)
				.handler(HandlerFactory.handler(DraftsPostRequest.class, this::draftsPost));

		router.route(version + "/drafts/hero")
				.handler(bodyHandler);
		router.route(version + "/drafts/hero")
				.handler(authHandler);
		router.route(version + "/drafts/hero")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(DraftsChooseHeroRequest.class, this::draftsChooseHero));

		router.route(version + "/drafts/cards")
				.handler(bodyHandler);
		router.route(version + "/drafts/cards")
				.handler(authHandler);
		router.route(version + "/drafts/cards")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(DraftsChooseCardRequest.class, this::draftsChooseCard));

		router.route(version + "/friends/:friendId/conversation")
				.handler(bodyHandler);
		router.route(version + "/friends/:friendId/conversation")
				.handler(authHandler);
		router.route(version + "/friends/:friendId/conversation")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(SendMessageRequest.class, "friendId",
						this::sendFriendMessage));
		router.route(version + "/friends/:friendId/conversation")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("friendId", this::getFriendConversation));

		logger.info("Router configured.");
		server = awaitResult(done -> server.requestHandler(router::accept).listen(done));
		logger.info("Listening on port " + Integer.toString(server.actualPort()));
	}

	@Override
	public WebResult<GetAccountsResponse> getAccount(RoutingContext context, String userId, String targetUserId) throws SuspendExecution, InterruptedException {
		// TODO: If it's an ally, send all the information
		if (userId.equals(targetUserId)) {
			final Account account = getAccount(userId);
			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(account)));
		} else {
			UserRecord record = getAccounts().get(userId);
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
		com.hiddenswitch.spellsource.models.CreateAccountResponse internalResponse = getAccounts()
				.createAccount(new com.hiddenswitch.spellsource.models.CreateAccountRequest()
						.withEmailAddress(request.getEmail())
						.withPassword(request.getPassword())
						.withName(request.getName()));

		if (internalResponse.isInvalidEmailAddress()) {
			return WebResult.failed(new RuntimeException("Invalid email address."));
		} else if (internalResponse.isInvalidPassword()) {
			return WebResult.failed(new RuntimeException("Invalid password."));
		}

		// Initialize the collection
		final String userId = internalResponse.getUserId();
		getLogic().initializeUser(new InitializeUserRequest().withUserId(userId));

		final Account account = getAccount(userId);
		return WebResult.succeeded(new CreateAccountResponse()
				.loginToken(internalResponse.getLoginToken().getToken())
				.account(account));
	}

	@Override
	public WebResult<LoginResponse> login(RoutingContext context, LoginRequest request) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.spellsource.models.LoginResponse internalResponse = getAccounts().login(
				new com.hiddenswitch.spellsource.models.LoginRequest().withEmail(request.getEmail())
						.withPassword(request.getPassword()));

		if (internalResponse.isBadPassword()) {
			return WebResult.failed(new RuntimeException("Invalid password."));
		} else if (internalResponse.isBadEmail()) {
			return WebResult.failed(new RuntimeException("Invalid email address."));
		}

		return WebResult.succeeded(new LoginResponse()
				.account(getAccount(internalResponse.getToken().getAccessKey()))
				.loginToken(internalResponse.getToken().getToken()));
	}

	@Override
	public WebResult<DecksPutResponse> decksPut(RoutingContext context, String userId, DecksPutRequest request) throws SuspendExecution, InterruptedException {
		final HeroClass heroClass = HeroClass.valueOf(request.getHeroClass());

		DeckCreateResponse internalResponse = getDecks().createDeck(new DeckCreateRequest()
				.withName(request.getName())
				.withInventoryIds(request.getInventoryIds())
				.withHeroClass(heroClass)
				.withUserId(userId));

		return WebResult.succeeded(new DecksPutResponse().deckId(internalResponse.getDeckId()));
	}

	private WebResult<DecksGetResponse> getDeck(String userId, String deckId) throws SuspendExecution, InterruptedException {
		GetCollectionResponse updatedCollection = getInventory().getCollection(new GetCollectionRequest()
				.withUserId(userId)
				.withDeckId(deckId));

		return WebResult.succeeded(new DecksGetResponse()
				.inventoryIdsSize(updatedCollection.getInventoryRecords().size())
				.collection(updatedCollection.asInventoryCollection()));
	}

	@Override
	public WebResult<DecksGetResponse> decksUpdate(RoutingContext context, String userId, String deckId, DecksUpdateCommand updateCommand) throws SuspendExecution, InterruptedException {
		getDecks().updateDeck(new DeckUpdateRequest(userId, deckId, updateCommand));

		// Get the updated collection
		return getDeck(userId, deckId);
	}

	@Override
	public WebResult<DecksGetResponse> decksGet(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException {
		return getDeck(userId, deckId);
	}

	@Override
	public WebResult<DecksGetAllResponse> decksGetAll(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		List<String> decks = getAccounts().get(userId).getDecks();

		List<DecksGetResponse> responses = new ArrayList<>();
		for (String deck : decks) {
			responses.add(getDeck(userId, deck).result());
		}

		return WebResult.succeeded(new DecksGetAllResponse().decks(responses));
	}

	@Override
	public WebResult<DeckDeleteResponse> decksDelete(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException {
		return WebResult.succeeded(getDecks().deleteDeck(new DeckDeleteRequest(deckId)));
	}

	@Override
	public WebResult<MatchmakingQueuePutResponse> matchmakingConstructedQueuePut(RoutingContext routingContext, String userId, String queueId, MatchmakingQueuePutRequest request) throws SuspendExecution, InterruptedException {
		MatchmakingRequest internalRequest = new MatchmakingRequest(request, userId).withBotMatch(request.getCasual());
		MatchmakingResponse internalResponse = getMatchmaking().matchmakeAndJoin(internalRequest);

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
			userResponse.unityConnection(internalResponse.getUnityConnection());
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
	public WebResult<com.hiddenswitch.spellsource.client.models.MatchCancelResponse> matchmakingConstructedQueueDelete(RoutingContext context, String userId, String queueId) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.spellsource.models.MatchCancelResponse internalResponse = getMatchmaking().cancel(new MatchCancelRequest(userId));

		com.hiddenswitch.spellsource.client.models.MatchCancelResponse response =
				new com.hiddenswitch.spellsource.client.models.MatchCancelResponse()
						.isCanceled(internalResponse.getCanceled());

		return WebResult.succeeded(response);
	}

	@Override
	public WebResult<MatchConcedeResponse> matchmakingConstructedDelete(RoutingContext context, String userId, String queueId) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.spellsource.models.MatchCancelResponse response = getMatchmaking().cancel(new MatchCancelRequest(userId));
		if (response == null) {
			return WebResult.failed(new RuntimeException());
		}
		getGames().concedeGameSession(new ConcedeGameSessionRequest(response.getGameId(), response.getPlayerId()));
		return WebResult.succeeded(new MatchConcedeResponse().isConceded(true));
	}

	@Override
	public WebResult<GameState> matchmakingConstructedGet(RoutingContext context, String userId, String queueId) throws SuspendExecution, InterruptedException {
		if (vertx.isClustered()) {
			return WebResult.failed(400, new RuntimeException("Cannot retrieve a JSON game state this way in a clustered environment."));
		}

		CurrentMatchResponse response = getMatchmaking().getCurrentMatch(new CurrentMatchRequest(userId));
		if (response.getGameId() == null) {
			return WebResult.failed(404, new NullPointerException("Game not found."));
		}

		return WebResult.succeeded(getGames().getClientGameState(response.getGameId(), userId));
	}

	@Override
	public WebResult<MatchmakingQueuesResponse> matchmakingGet(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		return WebResult.succeeded(
				new MatchmakingQueuesResponse()
						.addQueuesItem(new MatchmakingQueueItem()
								.name("Constructed")
								.description("An unranked constructed with meta decks in Wild and Standard from Hearthstone.")
								.tooltip("Play online unranked in Standard!")
								.queueId("constructed")
								.requires(new MatchmakingQueueItemRequires()
										.deckIdChoices(getAccounts().get(userId).getDecks()))));
	}

	@Override
	public WebResult<FriendPutResponse> putFriend(RoutingContext context, String userId, FriendPutRequest req)
			throws SuspendExecution, InterruptedException {
		String friendId = req.getFriendId();

		//lookup friend user record
		UserRecord friendAccount = getAccounts().get(req.getFriendId());

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
		UserRecord friendAccount = getAccounts().get(friendId);

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
		DraftRecord record = getDrafts().get(new GetDraftRequest().withUserId(userId));
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
								getDrafts().doDraftAction(new DraftActionRequest().withUserId(userId))
										.getPublicDraftState()));
			} catch (NullPointerException unexpectedRequest) {
				return WebResult.failed(400, unexpectedRequest);
			}
		} else if (null != request.getRetireEarly()
				&& request.getRetireEarly()) {
			return WebResult.succeeded(
					Draft.toDraftState(
							getDrafts().retireDraftEarly(new RetireDraftRequest().withUserId(userId))
									.getRecord()
									.getPublicDraftState()));

		}

		return WebResult.failed(400, new UnsupportedOperationException("You must choose a valid action."));
	}

	@Override
	public WebResult<DraftState> draftsChooseHero(RoutingContext context, String userId, DraftsChooseHeroRequest request) throws SuspendExecution, InterruptedException {
		try {
			DraftRecord record = getDrafts().doDraftAction(new DraftActionRequest()
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
			DraftRecord record = getDrafts().doDraftAction(new DraftActionRequest()
					.withUserId(userId)
					.withCardIndex(request.getCardIndex()));

			return WebResult.succeeded(Draft.toDraftState(record.getPublicDraftState()));
		} catch (NullPointerException unexpectedRequest) {
			return WebResult.failed(400, unexpectedRequest);
		} catch (Exception ignored) {
			return WebResult.failed(400, new UnsupportedOperationException("You must choose a card index, or the draft has been completed."));
		}
	}

	public WebResult<GetConversationResponse> getFriendConversation(
			RoutingContext context, String userId, String friendId) throws SuspendExecution, InterruptedException {
		UserRecord userAccount = (UserRecord) context.user();
		if (!userAccount.isFriend(friendId)) {
			return WebResult.failed(404, new Exception("Friend account not found"));
		}

		GetConversationResponse getConversationResponse = new GetConversationResponse().conversation(
				Conversations.getCreateConversation(getMongo(), userId, friendId).toConversationDto());

		return WebResult.succeeded(getConversationResponse);
	}

	public WebResult<SendMessageResponse> sendFriendMessage(
			RoutingContext context, String userId, String friendId, SendMessageRequest request)
			throws SuspendExecution, InterruptedException {
		UserRecord myAccount = (UserRecord) context.user();
		if (!myAccount.isFriend(friendId)) {
			return WebResult.failed(404, new Exception("Not friends"));
		}

		MessageRecord messageSent = Conversations.insertMessage(Mongo.mongo().client(), userId,
				myAccount.getProfile().getDisplayName(), friendId, request.getText());
		SendMessageResponse response = new SendMessageResponse().message(messageSent.toMessageDto());
		return WebResult.succeeded(response);
	}

	private Account getAccount(String userId) throws SuspendExecution, InterruptedException {
		// Get the personal collection
		UserRecord record = getAccounts().get(userId);
		GetCollectionResponse personalCollection = getInventory().getCollection(GetCollectionRequest.user(record.getId()));

		// Get the decks
		GetCollectionResponse deckCollections = getInventory().getCollection(GetCollectionRequest.decks(record.getDecks()));

		final String displayName = record.getProfile().getDisplayName();
		return new Account()
				.id(record.getId())
				.decks(deckCollections.getResponses().stream()
						.filter(response -> !response.getTrashed()).map(GetCollectionResponse::asInventoryCollection).collect(toList()))
				.personalCollection(personalCollection.asInventoryCollection())
				.email(record.getProfile().getEmailAddress())
				.connection(record.getConnection())
				.name(displayName);
	}

	public Cards getCards() throws InterruptedException, SuspendExecution {
		return Rpc.connect(Cards.class, vertx.eventBus()).sync();
	}

	public Accounts getAccounts() throws InterruptedException, SuspendExecution {
		return Rpc.connect(Accounts.class, vertx.eventBus()).sync();
	}

	public Games getGames() throws InterruptedException, SuspendExecution {
		return Rpc.connect(Games.class, vertx.eventBus()).sync();
	}

	public Matchmaking getMatchmaking() throws InterruptedException, SuspendExecution {
		return Rpc.connect(Matchmaking.class, vertx.eventBus()).sync();
	}

	public Bots getBots() throws InterruptedException, SuspendExecution {
		return Rpc.connect(Bots.class, vertx.eventBus()).sync();
	}

	public Logic getLogic() throws InterruptedException, SuspendExecution {
		return Rpc.connect(Logic.class, vertx.eventBus()).sync();
	}

	public Decks getDecks() throws InterruptedException, SuspendExecution {
		return Rpc.connect(Decks.class, vertx.eventBus()).sync();
	}

	public Inventory getInventory() throws InterruptedException, SuspendExecution {
		return Rpc.connect(Inventory.class, vertx.eventBus()).sync();
	}

	public Draft getDrafts() throws InterruptedException, SuspendExecution {
		return Rpc.connect(Draft.class, vertx.eventBus()).sync();
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		Void t = awaitResult(h -> server.close(h));
	}
}
