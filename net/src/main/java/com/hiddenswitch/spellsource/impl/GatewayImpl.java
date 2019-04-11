package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.Sets;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.client.models.*;
import com.hiddenswitch.spellsource.client.models.CreateAccountRequest;
import com.hiddenswitch.spellsource.client.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.client.models.LoginRequest;
import com.hiddenswitch.spellsource.client.models.LoginResponse;
import com.hiddenswitch.spellsource.common.DeckCreateRequest;
import com.hiddenswitch.spellsource.concurrent.SuspendableMap;
import com.hiddenswitch.spellsource.impl.util.*;
import com.hiddenswitch.spellsource.models.ChangePasswordRequest;
import com.hiddenswitch.spellsource.models.ChangePasswordResponse;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.models.MatchCancelResponse;
import com.hiddenswitch.spellsource.util.*;
import io.vertx.core.Closeable;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.array;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;
import static java.util.stream.Collectors.toList;

/**
 * An implementation of an <a href="https://www.linkedin.com/pulse/api-gateway-pattern-subhash-chandran">API gateway</a>
 * for user-accessible services in Spellsource.
 *
 * @see Gateway for a detailed description on how to add methods to the API gateway.
 */
public class GatewayImpl extends SyncVerticle implements Gateway {
	private static Logger logger = LoggerFactory.getLogger(Gateway.class);
	private final int port;
	private HttpServer server;
	private Closeable queues;
	private Closeable serverMessaging;

	public GatewayImpl(int port) {
		this.port = port;
	}


	@Override
	@Suspendable
	public void start() throws RuntimeException, SuspendExecution {
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		io.vertx.core.logging.LoggerFactory.initialise();
		server = vertx.createHttpServer(new HttpServerOptions()
				.setHost("0.0.0.0")
				.setPort(port)
				.setMaxWebsocketFrameSize(65536)
				.setMaxWebsocketMessageSize(100 * 65536)
				.setPerFrameWebsocketCompressionSupported(true)
				.setPerMessageWebsocketCompressionSupported(true)
				.setCompressionSupported(true));
		Router router = Router.router(vertx);

		logger.info("start: Configuring router on instance {}", this.deploymentID());

		final AuthHandler authHandler = SpellsourceAuthHandler.create();
		final BodyHandler bodyHandler = BodyHandler.create();

		// Handle all realtime messaging here
		router.route("/realtime")
				.method(HttpMethod.GET)
				.handler(authHandler);

		router.route("/realtime")
				.method(HttpMethod.GET)
				.handler(Connection.handler());

		// Send game traffic over the Connection nowadays.
		serverMessaging = ServerGameContext.handleConnections();

		// Enable friend list updates via envelope messaging channel
		Friends.handleConnections();

		// Enable presence
		Presence.handleConnections();

		// Enable realtime conversations
		Conversations.handleConnections();

		// Create default matchmaking queues
		queues = Matchmaking.startDefaultQueues();
		// Create draft queue
		Closeable draftQueue = Draft.startDraftQueue();

		final Closeable originalQueues = queues;
		queues = fut -> {
			originalQueues.close(v1 -> {
				serverMessaging.close(v2 -> {
					draftQueue.close(fut);
				});
			});
		};

		// Handle the enqueue and dequeue methods through the matchmaker
		Matchmaking.handleConnections();

		// Handle realtime notification of invitations
		Invites.handleConnections();

		// Health check comes first
		router.route("/")
				.handler(routingContext -> {
					// Check that hazelcast is ready in this health check
					if (!Hazelcast.getHazelcastInstance().getLifecycleService().isRunning()) {
						routingContext.fail(500);
						return;
					}

					routingContext.response().setStatusCode(200);
					routingContext.response().end("OK");
				});


		// Version
		router.route("/version")
				.method(HttpMethod.GET)
				.handler(routingContext -> routingContext.response().end(Version.version()));

		// All routes need logging of URLs. URLs never leak private information
		router.route().handler(LoggerHandler.create(false, LoggerFormat.DEFAULT));

		// CORS
		router.route().handler(CorsHandler.create(".*")
				.allowedHeader("Content-Type")
				.allowedHeader("X-Auth-Token")
				.allowedHeader("If-None-Match")
				.allowedHeader("Accept-Encoding")
				.exposedHeader("Content-Type")
				.exposedHeader("Content-Encoding")
				.exposedHeader("ETag")
				.exposedHeader("Cache-Control")
				.exposedHeader("Last-Modified")
				.exposedHeader("Date")
				.exposedHeader("Connection")
				.exposedHeader("WebSocket")
				.exposedHeader("Upgrade")
				.allowCredentials(true)
				.allowedMethods(Sets.newHashSet(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS, HttpMethod.HEAD)));

		// Pass through cookies
		// TODO: This obviously isn't working for the load balancer / cookies coming from the client
		router.route().handler(CookieHandler.create());

		// Add body handling to all routes
		router.route().handler(bodyHandler);

		// Add "content-type=application/json" to all responses
		router.route().handler(context -> {
			if (!context.request().uri().contains("/realtime")) {
				context.response().putHeader("Content-Type", "application/json");
			}
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
				if (!routingContext.response().closed()) {
					routingContext.response().end(Serialization.serialize(new SpellsourceException().message(routingContext.failure().getMessage())));
				}
			} else {
				if (!routingContext.response().closed()) {
					routingContext.response().end(Serialization.serialize(new SpellsourceException().message("An internal server error occurred. Try again later.")));
				}
			}

		});

		// Password reset
		Accounts.passwordReset(router);

		router.route("/accounts/:targetUserId")
				.handler(authHandler);
		router.route("/accounts/:targetUserId")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("targetUserId", this::getAccount));

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
				.handler(authHandler);
		router.route("/decks")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(DecksPutRequest.class, this::decksPut));
		router.route("/decks")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(this::decksGetAll));

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

		router.route("/matchmaking")
				.handler(authHandler);
		router.route("/matchmaking")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(this::matchmakingGet));

		router.route("/matchmaking")
				.handler(authHandler);
		router.route("/matchmaking")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler(this::matchmakingDelete));

		router.route("/friends")
				.handler(authHandler);
		router.route("/friends")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(FriendPutRequest.class, this::friendPut));

		router.route("/friends/:friendId")
				.handler(authHandler);
		router.route("/friends/:friendId")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("friendId", this::unFriend));

		router.route("/games/:gameId")
				.handler(authHandler);
		router.route("/games/:gameId")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("gameId", this::getGameRecord));

		router.route("/games")
				.handler(authHandler);
		router.route("/games")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(this::getGameRecordIds));

		router.route("/invites")
				.handler(authHandler);
		router.route("/invites")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(this::getInvites));

		router.route("/invites")
				.method(HttpMethod.POST)
				.handler(HandlerFactory.handler(InvitePostRequest.class, this::postInvite));

		router.route("/invites/:inviteId")
				.handler(authHandler);

		router.route("/invites/:inviteId")
				.method(HttpMethod.POST)
				.handler(HandlerFactory.handler(AcceptInviteRequest.class, "inviteId", this::acceptInvite));

		router.route("/invites/:inviteId")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler("inviteId", this::getInvite));

		router.route("/invites/:inviteId")
				.method(HttpMethod.DELETE)
				.handler(HandlerFactory.handler("inviteId", this::deleteInvite));

		router.route("/drafts")
				.handler(authHandler);
		router.route("/drafts")
				.method(HttpMethod.GET)
				.handler(HandlerFactory.handler(this::draftsGet));

		router.route("/drafts")
				.method(HttpMethod.POST)
				.handler(HandlerFactory.handler(DraftsPostRequest.class, this::draftsPost));

		router.route("/drafts/hero")
				.handler(authHandler);
		router.route("/drafts/hero")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(DraftsChooseHeroRequest.class, this::draftsChooseHero));

		router.route("/drafts/cards")
				.handler(authHandler);
		router.route("/drafts/cards")
				.method(HttpMethod.PUT)
				.handler(HandlerFactory.handler(DraftsChooseCardRequest.class, this::draftsChooseCard));

		router.route("/invites")
				.handler(authHandler);
		router.route("/invites")
				.method(HttpMethod.POST);

		Cards.invalidateCardCache();

		server.requestHandler(router);
		HttpServer listening = awaitResult(server::listen);

		logger.info("start: Router configured.");
	}

	@Override
	@Suspendable
	public WebResult<MatchCancelResponse> matchmakingDelete(RoutingContext context) throws SuspendExecution {
		Matchmaking.dequeue(new UserId(Accounts.userId(context)));
		return WebResult.succeeded(new MatchCancelResponse(true, null, 0));
	}

	@Override
	public WebResult<GetAccountsResponse> getAccount(@NotNull RoutingContext context, String userId, String targetUserId) throws SuspendExecution, InterruptedException {
		if (targetUserId == null) {
			return WebResult.notFound("A null targetUserId was given.");
		}

		if (userId.equals(targetUserId)) {
			final Account account = getAccount(userId);
			if (account == null) {
				return WebResult.notFound("Unexpectedly, an account with your userId %s was not found", userId);
			}

			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(account)));
		} else {
			UserRecord record = Accounts.get(targetUserId);
			if (record == null) {
				return WebResult.notFound("An account with userId %s was not found", userId);
			}

			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(new Account()
					.name(record.getUsername())
					.id(targetUserId))));
		}
	}


	@Override
	public WebResult<GetAccountsResponse> getAccounts(RoutingContext context, String userId, GetAccountsRequest request) throws SuspendExecution, InterruptedException {
		return WebResult.unsupported("Cannot retrieve multiple accounts through this interface.");
	}

	@Override
	public WebResult<CreateAccountResponse> createAccount(RoutingContext context, CreateAccountRequest request) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.spellsource.models.CreateAccountResponse internalResponse = Accounts
				.createAccount(new com.hiddenswitch.spellsource.models.CreateAccountRequest()
						.withEmailAddress(request.getEmail())
						.withPassword(request.getPassword())
						.withName(request.getName()));

		if (internalResponse.isInvalidEmailAddress()) {
			return WebResult.invalidArgument("E-mail address already exists");
		} else if (internalResponse.isInvalidPassword()) {
			return WebResult.invalidArgument("Password is too short (at least 6 characters)");
		} else if (internalResponse.isInvalidName()) {
			return WebResult.invalidArgument("Username invalid (only alphanumerics, starts with letter)");
		} else if (internalResponse.getUserId() == null) {
			return WebResult.notFound("Account was not successfully created, try again later");
		}

		// Initialize the collection
		final String userId = internalResponse.getUserId();
		Logic.initializeUser(InitializeUserRequest.create(userId));
		final Account account = getAccount(userId);
		return WebResult.succeeded(new CreateAccountResponse()
				.loginToken(internalResponse.getLoginToken().getToken())
				.account(account));
	}

	@Override
	public WebResult<LoginResponse> login(RoutingContext context, LoginRequest request) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.spellsource.models.LoginResponse internalResponse;
		internalResponse = Accounts.login(
				new com.hiddenswitch.spellsource.models.LoginRequest().withEmail(request.getEmail())
						.withPassword(request.getPassword()));


		if (internalResponse.isBadPassword()) {
			return WebResult.invalidArgument("Bad password");
		} else if (internalResponse.isBadEmail()) {
			return WebResult.invalidArgument("Bad email address");
		}

		return WebResult.succeeded(new LoginResponse()
				.account(getAccount(internalResponse.getToken().getAccessKey()))
				.loginToken(internalResponse.getToken().getToken()));
	}

	@Override
	public WebResult<DecksPutResponse> decksPut(RoutingContext context, String userId, DecksPutRequest request) throws SuspendExecution, InterruptedException {
		DeckCreateRequest createRequest;
		if (request.getDeckList() == null
				|| request.getDeckList().equals("")) {
			final HeroClass heroClass = HeroClass.valueOf(request.getHeroClass().name());

			createRequest = new DeckCreateRequest()
					.withName(request.getName())
					.withFormat(request.getFormat().toString())
					.withInventoryIds(request.getInventoryIds())
					.withHeroClass(heroClass);
		} else {
			createRequest = DeckCreateRequest.fromDeckList(request.getDeckList());
		}

		DeckCreateResponse internalResponse = Decks.createDeck(createRequest
				.withUserId(userId));

		return WebResult.succeeded(new DecksPutResponse()
				.deckId(internalResponse.getDeckId())
				.collection(internalResponse.getCollection().asInventoryCollection()));
	}

	private WebResult<DecksGetResponse> getDeck(String userId, String deckId) throws SuspendExecution, InterruptedException {
		if (!Inventory.isOwner(deckId, new UserId(userId))) {
			return WebResult.forbidden("Cannot access a different user's deck, until you are an ally.");
		}

		GetCollectionResponse collection = Inventory.getCollection(new GetCollectionRequest()
				.withUserId(userId)
				.withDeckId(deckId));

		return WebResult.succeeded(new DecksGetResponse()
				.inventoryIdsSize(collection.getInventoryRecords().size())
				.collection(collection.asInventoryCollection()));
	}

	@Override
	public WebResult<DecksGetResponse> decksUpdate(RoutingContext context, String userId, String deckId, DecksUpdateCommand updateCommand) throws SuspendExecution, InterruptedException {
		// Checks if the user can modify this deck
		if (!Inventory.isOwner(deckId, new UserId(userId))) {
			return WebResult.forbidden("You cannot modify this deck, you are not its owner");
		}

		Decks.updateDeck(DeckUpdateRequest.create(userId, deckId, updateCommand));

		// Get the updated collection
		return getDeck(userId, deckId);
	}

	@Override
	public WebResult<DecksGetResponse> decksGet(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException {
		if (!Inventory.isOwner(deckId, new UserId(userId))) {
			return WebResult.forbidden("You cannot read this deck, you are not its owner");
		}

		return getDeck(userId, deckId);
	}

	@Override
	public WebResult<DecksGetAllResponse> decksGetAll(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		List<String> decks = Accounts.get(userId).getDecks();

		List<DecksGetResponse> responses = new ArrayList<>();
		for (String deck : decks) {
			responses.add(getDeck(userId, deck).result());
		}

		return WebResult.succeeded(new DecksGetAllResponse().decks(responses));
	}

	@Override
	public WebResult<DeckDeleteResponse> decksDelete(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException {
		if (!Inventory.isOwner(deckId, new UserId(userId))) {
			return WebResult.forbidden("You cannot delete this deck, you are not its owner");
		}

		return WebResult.succeeded(Decks.deleteDeck(DeckDeleteRequest.create(deckId)));
	}

	@Override
	public WebResult<MatchmakingQueuesResponse> matchmakingGet(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		List<String> decks = Accounts.get(userId).getDecks();
		return WebResult.succeeded(
				new MatchmakingQueuesResponse()
						.addQueuesItem(new MatchmakingQueueItem()
								.name("Quick Play")
								.description("Play a game against a skilled computer opponent.")
								.tooltip("Play against a bot!")
								.queueId("quickPlay")
								.requires(new MatchmakingQueueItemRequires()
										.deckIdChoices(decks)))
						.addQueuesItem(new MatchmakingQueueItem()
								.name("Constructed")
								.description("An unranked constructed with decks in the Custom format (includes community cards).")
								.tooltip("Play online with custom cards!")
								.queueId("constructed")
								.requires(new MatchmakingQueueItemRequires()
										.deckIdChoices(decks))));
	}

	@Override
	public WebResult<FriendPutResponse> friendPut(RoutingContext context, String userId, FriendPutRequest req)
			throws SuspendExecution, InterruptedException {
		// lookup own user account
		UserRecord myAccount = (UserRecord) context.user();

		if (req.getFriendId() != null) {
			return WebResult.failed(409, new IllegalArgumentException("Not supported."));
		}

		if (req.getUsernameWithToken() == null
				|| req.getUsernameWithToken().split("#").length != 2) {
			return WebResult.failed(409, new IllegalArgumentException("No username and security token specified; or, an invalid one was specified."));
		}

		try {
			FriendPutResponse response = Friends.putFriend(myAccount, req);
			return WebResult.succeeded(response);
		} catch (NullPointerException ex) {
			return WebResult.failed(404, ex);
		} catch (IllegalArgumentException ex) {
			return WebResult.failed(409, ex);
		}
	}

	@Override
	public WebResult<UnfriendResponse> unFriend(RoutingContext context, String userId, String friendId)
			throws SuspendExecution, InterruptedException {
		UserRecord myAccount = (UserRecord) context.user();
		try {
			UnfriendResponse response = Friends.unfriend(myAccount, friendId);
			return WebResult.succeeded(response);
		} catch (NullPointerException ex) {
			return WebResult.failed(404, ex);
		} catch (IllegalStateException ex) {
			return WebResult.failed(418, ex);
		}
	}

	@Override
	public WebResult<DraftState> draftsGet(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		DraftRecord record = Draft.get(new GetDraftRequest().withUserId(userId));
		if (record == null) {
			return WebResult.notFound("You have not started a draft. Start one first.");
		}

		return WebResult.succeeded(Draft.toDraftState(record.getPublicDraftState()));
	}

	@Override
	public WebResult<DraftState> draftsPost(RoutingContext context, String userId, DraftsPostRequest request) throws SuspendExecution, InterruptedException {
		if (null != request.isStartDraft()
				&& request.isStartDraft()) {
			try {
				return WebResult.succeeded(
						Draft.toDraftState(
								Draft.doDraftAction(new DraftActionRequest().withUserId(userId))
										.getPublicDraftState()));
			} catch (NullPointerException unexpectedRequest) {
				return WebResult.failed(400, unexpectedRequest);
			}
		} else if (null != request.isRetireEarly()
				&& request.isRetireEarly()) {
			return WebResult.succeeded(
					Draft.toDraftState(
							Draft.retireDraftEarly(new RetireDraftRequest().withUserId(userId))
									.getRecord()
									.getPublicDraftState()));

		}

		return WebResult.failed(400, new UnsupportedOperationException("You must choose a valid action."));
	}

	@Override
	public WebResult<DraftState> draftsChooseHero(RoutingContext context, String userId, DraftsChooseHeroRequest request) throws SuspendExecution, InterruptedException {
		try {
			DraftRecord record = Draft.doDraftAction(new DraftActionRequest()
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
			DraftRecord record = Draft.doDraftAction(new DraftActionRequest()
					.withUserId(userId)
					.withCardIndex(request.getCardIndex()));
			return WebResult.succeeded(Draft.toDraftState(record.getPublicDraftState()));
		} catch (NullPointerException unexpectedRequest) {
			return WebResult.failed(400, unexpectedRequest);
		} catch (Exception ignored) {
			return WebResult.failed(400, new UnsupportedOperationException("You must choose a card index, or the draft has been completed."));
		}
	}

	@Override
	public WebResult<AcceptInviteResponse> acceptInvite(RoutingContext context, String userId, String inviteId, AcceptInviteRequest request) throws SuspendExecution, InterruptedException {
		return WebResult.succeeded(Invites.accept(new InviteId(inviteId), request, (UserRecord) context.user()));
	}

	@Override
	public WebResult<InviteResponse> getInvite(RoutingContext context, String userId, String inviteId) throws SuspendExecution, InterruptedException {
		Invite invite = mongo().findOne(Invites.INVITES, json("_id", inviteId), Invite.class);
		if (invite == null) {
			return WebResult.notFound("This invite was not found");
		}
		return WebResult.succeeded(new InviteResponse().invite(invite));
	}

	@Override
	public WebResult<InviteResponse> deleteInvite(RoutingContext context, String userId, String inviteId) throws SuspendExecution, InterruptedException {
		return WebResult.succeeded(Invites.deleteInvite(new InviteId(inviteId), new UserId(userId)));
	}

	@Override
	public WebResult<InviteResponse> postInvite(RoutingContext context, String userId, InvitePostRequest request) throws SuspendExecution, InterruptedException {
		return WebResult.succeeded(Invites.invite(request, (UserRecord) context.user()));
	}

	@Override
	public WebResult<InviteGetResponse> getInvites(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		return WebResult.succeeded(new InviteGetResponse()
				.invites(mongo().find(Invites.INVITES,
						json("$or", array(json("toUserId", userId), json("fromUserId", userId))),
						Invite.class)));
	}

	@Override
	public WebResult<Void> healthCheck(RoutingContext context) throws SuspendExecution, InterruptedException {
		return WebResult.succeeded(200, null);
	}

	@Override
	public WebResult<ChangePasswordResponse> changePassword(RoutingContext context, String userId, com.hiddenswitch.spellsource.client.models.ChangePasswordRequest request) throws SuspendExecution, InterruptedException {
		Accounts.changePassword(ChangePasswordRequest.request(new UserId(userId), request.getPassword()));
		return WebResult.succeeded(200, new ChangePasswordResponse());
	}

	@Override
	public WebResult<GetCardsResponse> getCards(RoutingContext context) throws SuspendExecution, InterruptedException {
		SuspendableMap<String, String> cache = SuspendableMap.getOrCreate("Cards::cards");

		String cardsVersion = cache.get("cards-version");
		String lastModified = cache.get("cards-last-modified");

		context.response().putHeader("ETag", cardsVersion);
		String userVersion = context.request().getHeader("If-None-Match");
		if (userVersion != null &&
				userVersion.equals(cardsVersion)) {
			return WebResult.succeeded(304, new GetCardsResponse().version(cardsVersion));
		}

		context.response().putHeader("Cache-Control", "public, max-age=31536000");
		context.response().putHeader("Last-Modified", lastModified);
		context.response().putHeader("Date", DATE_TIME_FORMATTER.format(new Date()));

		if (context.request().method() == HttpMethod.HEAD) {
			return WebResult.succeeded(new GetCardsResponse().version(cardsVersion));
		}

		// We created the cache for the first time
		// TODO: It's possible that there are multiple versions of Spellsource sharing a cluster, so version should be a hash
		return WebResult.succeeded(new GetCardsResponse()
				.cards(Cards.getCards()));
	}

	@Override
	public WebResult<GetGameRecordResponse> getGameRecord(RoutingContext context, String userId, String gameId) throws SuspendExecution, InterruptedException {
		GameRecord record = mongo().findOne(Games.GAMES, json("_id", gameId), GameRecord.class);
		if (record == null) {
			return WebResult.notFound("No game with the specified ID was found.");
		}

		if (record.getPlayerUserIds().stream().noneMatch(s -> s.equals(userId))) {
			return WebResult.forbidden("Cannot view a game you didn't participate in.");
		}

		// TODO: Use the user's actual localized time zone, maybe as for it as a parameter?
		String createdAtLocalized = record.getCreatedAt()
				.toInstant()
				.atZone(ZoneId.systemDefault())
				.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));

		Replay replay = record.getReplay();

		// Censor non-bot games
		if (!record.isBotGame()) {
			int opponentId = userId.equals(record.getPlayerUserIds().get(0)) ? 1 : 0;
			for (int i = 0; i < replay.getGameStates().size(); i++) {
				ReplayGameStates pair = replay.getGameStates().get(i);
				if (opponentId == 0) {
					pair.first(null);
				} else {
					pair.second(null);
				}
			}
		}

		return WebResult.succeeded(new GetGameRecordResponse()
				.playerNames(record.getPlayerNames())
				.replay(replay)
				.isBotGame(record.isBotGame())
				.completedAt(record.getCreatedAt().getTime())
				.completedAtLocalized(createdAtLocalized));
	}

	@Override
	public WebResult<GetGameRecordIdsResponse> getGameRecordIds(RoutingContext context, String userId) throws SuspendExecution, InterruptedException {
		List<JsonObject> ids = mongo().findWithOptions(Games.GAMES, json(GameRecord.PLAYER_USER_IDS, userId), new FindOptions().setFields(json(MongoRecord.ID, 1)));
		return WebResult.succeeded(
				new GetGameRecordIdsResponse()
						.gameIds(ids.stream().map(j -> j.getString(MongoRecord.ID))
								.collect(toList())));
	}

	private Account getAccount(String userId) throws SuspendExecution, InterruptedException {
		// Get the personal collection
		UserRecord record = Accounts.get(userId);
		GetCollectionResponse personalCollection = Inventory.getCollection(GetCollectionRequest.user(record.getId()));

		// Get the decks
		GetCollectionResponse deckCollections = Inventory.getCollection(GetCollectionRequest.decks(userId, record.getDecks()));

		final String displayName = record.getUsername();
		final List<GetCollectionResponse> responses = deckCollections.getResponses();
		return new Account()
				.id(record.getId())
				.friends(record.getFriends().stream().map(FriendRecord::toFriendDto).collect(toList()))
				.decks((responses != null && responses.size() > 0) ? responses.stream()
						.filter(response -> !response.getTrashed()).map(GetCollectionResponse::asInventoryCollection).collect(toList()) : Collections.emptyList())
				.personalCollection(personalCollection.asInventoryCollection())
				.email(record.getEmails().get(0).getAddress())
				.inMatch(Games.getUsersInGames().containsKey(new UserId(userId)))
				.name(displayName + "#" + record.getPrivacyToken())
				.privacyToken(record.getPrivacyToken());
	}


	public Games getGames() throws InterruptedException, SuspendExecution {
		return Rpc.connect(Games.class).sync();
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		if (server != null) {
			server.close();
		}
		if (queues != null) {
			Sync.invoke1(queues::close);
		}
	}

}
