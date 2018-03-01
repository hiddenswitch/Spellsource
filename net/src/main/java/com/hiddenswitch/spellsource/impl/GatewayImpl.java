package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Sets;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.client.models.CreateAccountRequest;
import com.hiddenswitch.spellsource.client.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.client.models.LoginRequest;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.impl.server.EventBusWriter;
import com.hiddenswitch.spellsource.impl.util.*;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.client.models.LoginResponse;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.models.ChangePasswordRequest;
import com.hiddenswitch.spellsource.models.ChangePasswordResponse;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.streams.Pump;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.impl.Utils;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;
import static io.vertx.ext.sync.Sync.fiberHandler;
import static java.util.stream.Collectors.toList;

/**
 * An implementation of an <a href="https://www.linkedin.com/pulse/api-gateway-pattern-subhash-chandran">API gateway</a>
 * for user-accessible services in Spellsource.
 *
 * @see Gateway for a detailed description on how to add methods to the API gateway.
 */
public class GatewayImpl extends AbstractService<GatewayImpl> implements Gateway {
	private static Logger logger = LoggerFactory.getLogger(Gateway.class);
	private static final DateFormat dateTimeFormatter = Utils.createRFC1123DateTimeFormatter();


	@Override
	@Suspendable
	public void start() throws RuntimeException, SuspendExecution {
		super.start();
		Router router = Spellsource.spellsource().router(vertx);

		logger.info("start: Configuring router...");

		final AuthHandler authHandler = SpellsourceAuthHandler.create();
		final BodyHandler bodyHandlerInternal = BodyHandler.create();

		Handler<RoutingContext> bodyHandler = context -> {
			// Connection upgrade requests never end and therefore the body handler will never
			// pass through to the subsequent route handlers.
			if ("websocket".equalsIgnoreCase(context.request().getHeader("Upgrade"))) {
				context.next();
			} else {
				bodyHandlerInternal.handle(context);
			}
		};

		router.route("/" + Games.WEBSOCKET_PATH + "-clustered")
				.method(HttpMethod.GET)
				.handler(authHandler);

		router.route("/" + Games.WEBSOCKET_PATH + "-clustered")
				.method(HttpMethod.GET)
				.handler(Games.createWebSocketHandler());

		// Health check comes first
		router.route("/")
				.handler(routingContext -> {
					routingContext.response().setStatusCode(200);
					routingContext.response().end("OK");
				});

		// All routes need logging.
		router.route().handler(LoggerHandler.create());

		// CORS
		router.route().handler(CorsHandler.create(".*")
				.allowedHeader("Content-Type")
				.allowedHeader("X-Auth-Token")
				.allowedHeader("If-None-Match")
				.exposedHeader("Content-Type")
				.exposedHeader("ETag")
				.exposedHeader("Cache-Control")
				.exposedHeader("Last-Modified")
				.exposedHeader("Date")
				.allowCredentials(true)
				.allowedMethods(Sets.newHashSet(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS, HttpMethod.HEAD)));

		// Pass through cookies
		router.route().handler(CookieHandler.create());

		// add "content-type=application/json" to all responses
		router.route().handler(context -> {
			context.response().putHeader("Content-Type", "application/json");
			context.next();
		});

		// Use a standardized failure handler
		router.route().failureHandler(routingContext -> {
			if (!routingContext.failed()) {
				routingContext.next();
				return;
			}

			if (routingContext.statusCode() > 0
					&& routingContext.failure() == null) {
				routingContext.response().setStatusCode(routingContext.statusCode());
			} else if (routingContext.response().getStatusCode() < 400) {
				routingContext.response().setStatusCode(500);
			}

			if (routingContext.failure() != null) {
				logger.error(routingContext.failure());
				routingContext.response().end(Serialization.serialize(new SpellsourceException().message(routingContext.failure().getMessage())));
			} else {
				routingContext.response().end(Serialization.serialize(new SpellsourceException().message("An internal server error occurred. Try again later.")));
			}

		});

		router.route("/accounts/:targetUserId")
				.handler(authHandler);
		router.route("/accounts/:targetUserId")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("targetUserId", this::getAccount));

		router.route("/accounts")
				.handler(bodyHandler);
		router.route("/accounts")
				.method(HttpMethod.POST)
				.handler(HandlerFactory.handler(LoginRequest.class, this::login));
		router.route("/accounts")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(CreateAccountRequest.class, this::createAccount));
		router.route("/accounts")
				.method(HttpMethod.GET)
				.handler(authHandler);
		router.route("/accounts")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(GetAccountsRequest.class, this::getAccounts));

		router.route("/accounts-password")
				.handler(bodyHandler);
		router.route("/accounts-password")
				.handler(authHandler);
		router.route("/accounts-password")
				.method(HttpMethod.POST)
				.handler(HandlerFactory.handler(com.hiddenswitch.spellsource.client.models.ChangePasswordRequest.class, this::changePassword));

		router.route("/cards")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(this::getCards));

		router.route("/cards")
				.method(HttpMethod.HEAD)
				.handler(HandlerFactory.handler(this::getCards));

		router.route("/decks")
				.handler(bodyHandler);
		router.route("/decks")
				.handler(authHandler);
		router.route("/decks")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(DecksPutRequest.class, this::decksPut));
		router.route("/decks")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(this::decksGetAll));

		router.route("/decks/:deckId")
				.handler(bodyHandler);
		router.route("/decks/:deckId")
				.handler(authHandler);
		router.route("/decks/:deckId")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("deckId", this::decksGet));

		router.route("/decks/:deckId")
				.method(HttpMethod.POST)
				.handler(HandlerFactory.handler(DecksUpdateCommand.class, "deckId", this::decksUpdate));

		router.route("/decks/:deckId")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("deckId", this::decksDelete));

		router.route("/matchmaking/:queueId")
				.handler(bodyHandler);
		router.route("/matchmaking/:queueId")
				.handler(authHandler);
		router.route("/matchmaking/:queueId")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("queueId", this::matchmakingConstructedGet));
		router.route("/matchmaking/:queueId")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("queueId", this::matchmakingConstructedDelete));

		router.route("/matchmaking/:queueId")
				.handler(bodyHandler);
		router.route("/matchmaking/:queueId")
				.handler(authHandler);
		router.route("/matchmaking/:queueId")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(MatchmakingQueuePutRequest.class, "queueId",
						this::matchmakingConstructedQueuePut));

		router.route("/matchmaking/:queueId")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("queueId", this::matchmakingConstructedDelete));

		router.route("/matchmaking")
				.handler(authHandler);
		router.route("/matchmaking")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("queueId", this::matchmakingConstructedQueueDelete));

		router.route("/friends")
				.handler(bodyHandler);
		router.route("/friends")
				.handler(authHandler);
		router.route("/friends")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(FriendPutRequest.class, this::putFriend));

		router.route("/friends/:friendId")
				.handler(bodyHandler);
		router.route("/friends/:friendId")
				.handler(authHandler);
		router.route("/friends/:friendId")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("friendId", this::unFriend));

		router.route("/drafts")
				.handler(bodyHandler);
		router.route("/drafts")
				.handler(authHandler);
		router.route("/drafts")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(this::draftsGet));

		router.route("/drafts")
				.method(HttpMethod.POST)
				.handler(HandlerFactory.handler(DraftsPostRequest.class, this::draftsPost));

		router.route("/drafts/hero")
				.handler(bodyHandler);
		router.route("/drafts/hero")
				.handler(authHandler);
		router.route("/drafts/hero")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(DraftsChooseHeroRequest.class, this::draftsChooseHero));

		router.route("/drafts/cards")
				.handler(bodyHandler);
		router.route("/drafts/cards")
				.handler(authHandler);
		router.route("/drafts/cards")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(DraftsChooseCardRequest.class, this::draftsChooseCard));

		router.route("/friends/:friendId/conversation")
				.handler(bodyHandler);
		router.route("/friends/:friendId/conversation")
				.handler(authHandler);
		router.route("/friends/:friendId/conversation")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(SendMessageRequest.class, "friendId",
						this::sendFriendMessage));

		router.route("/friends/:friendId/conversation")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("friendId", this::getFriendConversation));

		Void listen = awaitResult(done -> {
			try {
				Spellsource.spellsource().httpServer(vertx).listen(then -> {
					done.handle(Future.succeededFuture());
				});
			} catch (IllegalStateException alreadyListening) {
				done.handle(Future.succeededFuture());
			} catch (Exception e) {
				done.handle(Future.failedFuture(e));
			}
		});

		logger.info("start: Router configured.");
	}

	@Override
	public WebResult<GetAccountsResponse> getAccount(RoutingContext context, String userId, String targetUserId) throws SuspendExecution, InterruptedException {
		// TODO: If it's an ally, send all the information
		if (userId.equals(targetUserId)) {
			final Account account = getAccount(userId);
			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(account)));
		} else {
			UserRecord record = getAccounts().get(targetUserId);
			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(new Account()
					.name(record.getUsername())
					.id(targetUserId))));
		}
	}


	@Override
	public WebResult<GetAccountsResponse> getAccounts(RoutingContext context, String userId, GetAccountsRequest request) throws SuspendExecution, InterruptedException {
		return WebResult.failed(404, new UnsupportedOperationException("Cannot retrieve multiple accounts through this interface."));
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
		} else if (internalResponse.isInvalidName()) {
			return WebResult.failed(new RuntimeException("Invalid name."));
		} else if (internalResponse.getUserId() == null) {
			throw new RuntimeException();
		}

		// Initialize the collection
		final String userId = internalResponse.getUserId();
		getLogic().initializeUser(new InitializeUserRequest(userId));
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
		DeckCreateRequest createRequest;
		if (request.getDeckList() == null) {
			final HeroClass heroClass = HeroClass.valueOf(request.getHeroClass());

			createRequest = new DeckCreateRequest()
					.withName(request.getName())
					.withInventoryIds(request.getInventoryIds())
					.withHeroClass(heroClass);
		} else {
			try {
				createRequest = DeckCreateRequest.fromDeckList(request.getDeckList());
			} catch (Exception e) {
				return WebResult.failed(e);
			}
		}

		DeckCreateResponse internalResponse = getDecks().createDeck(createRequest
				.withUserId(userId));

		return WebResult.succeeded(new DecksPutResponse()
				.deckId(internalResponse.getDeckId())
				.collection(internalResponse.getCollection().asInventoryCollection()));
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
		GetCollectionResponse collection = getInventory().getCollection(GetCollectionRequest.deck(deckId));
		if (!collection.getUserId().equals(userId)) {
			return WebResult.failed(new SecurityException("You can't delete someone else's deck!"));
		}
		return WebResult.succeeded(getDecks().deleteDeck(new DeckDeleteRequest(deckId)));
	}

	@Override
	public WebResult<MatchmakingQueuePutResponse> matchmakingConstructedQueuePut(RoutingContext routingContext, String userId, String queueId, MatchmakingQueuePutRequest request) throws SuspendExecution, InterruptedException {
		MatchmakingRequest internalRequest = new MatchmakingRequest(request, userId).withBotMatch(request.getCasual());
		MatchmakingResponse internalResponse = getMatchmaking().matchmakeAndJoin(internalRequest);

		// Compute the appropriate response
		MatchmakingQueuePutResponse userResponse = new MatchmakingQueuePutResponse();
		if (internalResponse.getRetry() == null) {
			userResponse.unityConnection(new MatchmakingQueuePutResponseUnityConnection());
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
		if (response == null
				|| response.getGameId() == null) {
			return WebResult.failed(new RuntimeException("Could not concede the requested game."));
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
								.tooltip("Play online unranked in Wild!")
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
				.setDisplayName(friendAccount.getUsername());
		FriendRecord friendOfFriendRecord = new FriendRecord().setFriendId(userId).setSince(startOfFriendship)
				.setDisplayName(friendAccount.getUsername());

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

	@Override
	public WebResult<Void> healthCheck(RoutingContext context) throws SuspendExecution, InterruptedException {
		return WebResult.succeeded(200, null);
	}

	public WebResult<SendMessageResponse> sendFriendMessage(
			RoutingContext context, String userId, String friendId, SendMessageRequest request)
			throws SuspendExecution, InterruptedException {
		UserRecord myAccount = (UserRecord) context.user();
		if (!myAccount.isFriend(friendId)) {
			return WebResult.failed(404, new Exception("Not friends"));
		}

		MessageRecord messageSent = Conversations.insertMessage(Mongo.mongo().client(), userId,
				myAccount.getUsername(), friendId, request.getText());
		SendMessageResponse response = new SendMessageResponse().message(messageSent.toMessageDto());
		return WebResult.succeeded(response);
	}

	@Override
	public WebResult<ChangePasswordResponse> changePassword(RoutingContext context, String userId, com.hiddenswitch.spellsource.client.models.ChangePasswordRequest request) throws SuspendExecution, InterruptedException {
		try {
			getAccounts().changePassword(new ChangePasswordRequest(new UserId(userId), request.getPassword()));
		} catch (RuntimeException ex) {
			return WebResult.failed(ex);
		}

		return WebResult.succeeded(200, new ChangePasswordResponse());
	}

	@Override
	public WebResult<GetCardsResponse> getCards(RoutingContext context) throws SuspendExecution, InterruptedException {
		SuspendableMap<String, Object> cache = SharedData.getClusterWideMap("Cards::cards");
		// Our objective is to create a cards version ONCE for the entire cluster
		final String thisCardsVersionId = deploymentID();
		final String thisDate = dateTimeFormatter.format(new Date());
		String cardsVersion = (String) cache.putIfAbsent("cards-version", thisCardsVersionId);
		String lastModified = (String) cache.putIfAbsent("cards-last-modified", thisDate);
		if (cardsVersion == null) {
			cardsVersion = thisCardsVersionId;
		}
		if (lastModified == null) {
			lastModified = thisDate;
		}

		context.response().putHeader("ETag", cardsVersion);
		final String userVersion = context.request().getHeader("If-None-Match");
		if (userVersion != null &&
				userVersion.equals(cardsVersion)) {
			return WebResult.succeeded(304, null);
		}

		context.response().putHeader("Cache-Control", "public, max-age=31536000");
		context.response().putHeader("Last-Modified", lastModified);
		context.response().putHeader("Date", thisDate);

		if (context.request().method() == HttpMethod.HEAD) {
			return WebResult.succeeded(null);
		}

		// We created the cache for the first time
		// TODO: It's possible that there are multiple versions of Spellsource sharing a cluster, so version should be a hash
		return WebResult.succeeded(new GetCardsResponse()
				.cards(Cards.getCards())
				.version(cardsVersion));
	}

	private Account getAccount(String userId) throws SuspendExecution, InterruptedException {
		// Get the personal collection
		UserRecord record = getAccounts().get(userId);
		GetCollectionResponse personalCollection = getInventory().getCollection(GetCollectionRequest.user(record.getId()));

		// Get the decks
		GetCollectionResponse deckCollections = getInventory().getCollection(GetCollectionRequest.decks(userId, record.getDecks()));

		final String displayName = record.getUsername();
		final List<GetCollectionResponse> responses = deckCollections.getResponses();
		return new Account()
				.id(record.getId())
				.decks((responses != null && responses.size() > 0) ? responses.stream()
						.filter(response -> !response.getTrashed()).map(GetCollectionResponse::asInventoryCollection).collect(toList()) : Collections.emptyList())
				.personalCollection(personalCollection.asInventoryCollection())
				.email(record.getEmails().get(0).getAddress())
				.inMatch(getMatchmaking().getCurrentMatch(new CurrentMatchRequest(userId)).getGameId() != null)
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
	}

}
