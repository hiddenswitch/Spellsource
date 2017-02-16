package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Server;
import com.hiddenswitch.proto3.net.client.models.*;
import com.hiddenswitch.proto3.net.client.models.CardRecord;
import com.hiddenswitch.proto3.net.client.models.CreateAccountRequest;
import com.hiddenswitch.proto3.net.client.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.client.models.LoginRequest;
import com.hiddenswitch.proto3.net.client.models.LoginResponse;
import com.hiddenswitch.proto3.net.impl.auth.TokenAuthProvider;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.models.MatchCancelResponse;
import com.hiddenswitch.proto3.net.util.Serialization;
import com.hiddenswitch.proto3.net.util.WebResult;
import io.vertx.core.Verticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.sync.SyncVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import net.demilich.metastone.game.entities.heroes.HeroClass;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.hiddenswitch.proto3.net.util.Sync.suspendableHandler;
import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Created by bberman on 11/27/16.
 */
public class ServerImpl extends SyncVerticle implements Server {
	AccountsImpl accounts = new AccountsImpl().withEmbeddedConfiguration();
	GamesImpl games = new GamesImpl();
	MatchmakingImpl matchmaking = new MatchmakingImpl();
	BotsImpl bots = new BotsImpl();
	LogicImpl logic = new LogicImpl();
	DecksImpl decks = new DecksImpl();
	InventoryImpl inventory = new InventoryImpl().withEmbeddedConfiguration();

	@Override
	@Suspendable
	public void start() {
		Logger logger = LoggerFactory.getLogger(ServerImpl.class);
		HttpServer server = vertx.createHttpServer(new HttpServerOptions()
				.setHost("0.0.0.0")
				.setPort(8080));
		Router router = Router.router(vertx);

		try {
			for (Verticle verticle : Arrays.asList(accounts, games, matchmaking, bots, logic, decks, inventory)) {
				final String name = verticle.getClass().getName();
				logger.info("Deploying " + name + "...");
				String deploymentId = Sync.awaitResult(done -> vertx.deployVerticle(verticle, done));
				logger.info("Deployed " + name + " with ID " + deploymentId);
			}

			logger.info("Configuring router...");
			final String MATCHMAKE_PATH = "/v1/matchmaking/constructed/queue";

			TokenAuthProvider authProvider = new TokenAuthProvider(vertx);
			router.route()
					.handler(RedirectAuthHandler.create(authProvider));

			router.route("/*")
					.handler(LoggerHandler.create());

			router.route(MATCHMAKE_PATH)
					.method(HttpMethod.DELETE)
					.blockingHandler(Sync.fiberHandler((context) -> {

					}));

			router.route(MATCHMAKE_PATH)
					.method(HttpMethod.PUT)
					.consumes("application/json")
					.produces("application/json")
					.handler(BodyHandler.create());

			router.route(MATCHMAKE_PATH)
					.method(HttpMethod.PUT)
					.blockingHandler(suspendableHandler(context -> {
						String userId = context.user().principal().getString("userId");
						MatchmakingQueuePutRequest request = Serialization.deserialize(context.getBodyAsString(), MatchmakingQueuePutRequest.class);
						WebResult<MatchmakingQueuePutResponse> result = matchmakingConstructedQueuePut(context, userId, request);
						context.response().setStatusCode(result.responseCode()).end(result.toString());
					}));

			router.route(MATCHMAKE_PATH).failureHandler(LoggerHandler.create());

			logger.info("Router configured.");
			HttpServer listening = awaitResult(done -> server.requestHandler(router::accept).listen(done));
			logger.info("Listening on port " + Integer.toString(server.actualPort()));
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public WebResult<GetAccountsResponse> getAccount(RoutingContext context, String userId, String targetUserId) throws SuspendExecution, InterruptedException {

		if (userId.equals(targetUserId)) {
			// TODO: Include private information including entire account join if necessary
			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(new Account().id(targetUserId))));
		} else {
			return WebResult.succeeded(new GetAccountsResponse().accounts(Collections.singletonList(new Account().id(targetUserId))));
		}
	}

	@Override
	public WebResult<GetAccountsResponse> getAccounts(RoutingContext context, String userId, GetAccountsRequest request) throws SuspendExecution, InterruptedException {
		return null;
	}

	@Override
	public WebResult<CreateAccountResponse> createAccount(RoutingContext context, CreateAccountRequest request) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.proto3.net.models.CreateAccountResponse internalResponse = accounts.createAccount(request.getEmail(), request.getPassword(), request.getName());

		if (internalResponse.invalidEmailAddress) {
			return WebResult.failed(new RuntimeException("Invalid email address."));
		} else if (internalResponse.invalidPassword) {
			return WebResult.failed(new RuntimeException("Invalid password."));
		}

		return WebResult.succeeded(new CreateAccountResponse()
				.loginToken(internalResponse.loginToken.token)
				.account(new Account()
						.id(internalResponse.userId)
						.name(request.getName())));
	}

	@Override
	public WebResult<LoginResponse> login(RoutingContext context, LoginRequest request) throws SuspendExecution, InterruptedException {
		com.hiddenswitch.proto3.net.amazon.LoginResponse internalResponse = accounts.login(request.getEmail(), request.getPassword());

		if (internalResponse.isBadPassword()) {
			return WebResult.failed(new RuntimeException("Invalid password."));
		} else if (internalResponse.isBadEmail()) {
			return WebResult.failed(new RuntimeException("Invalid email address."));
		}

		return WebResult.succeeded(new LoginResponse().loginToken(internalResponse.getToken().token));
	}

	@Override
	public WebResult<DecksPutResponse> decksPut(RoutingContext context, String userId, DecksPutRequest request) throws SuspendExecution, InterruptedException {
		final HeroClass heroClass = HeroClass.valueOf(request.getHeroClass());

		DeckCreateResponse internalResponse = decks.createDeck(new DeckCreateRequest()
				.withName(request.getName())
				.withInventoryIds(request.getInventoryIds())
				.withHeroClass(heroClass)
				.withUserId(userId));

		return WebResult.succeeded(new DecksPutResponse().deckId(internalResponse.getCollectionId()));
	}

	private WebResult<DecksGetResponse> getDeck(String userId, String deckId) throws SuspendExecution, InterruptedException {
		GetCollectionResponse updatedCollection = inventory.getCollection(new GetCollectionRequest()
				.withUserId(userId)
				.withDeckId(deckId));

		List<CardRecord> inventoryItems = updatedCollection.getCardRecords().stream().map(cr -> new CardRecord()
				.id(cr.getId())
				.cardDesc(cr.getCardDescMap())
				.collectionIds(cr.getCollectionIds())
				.borrowedByUserId(cr.getBorrowedByUserId())
				.userId(cr.getUserId())).collect(Collectors.toList());

		return WebResult.succeeded(new DecksGetResponse()
				.inventoryIdsSize(inventoryItems.size())
				.collection(new InventoryCollection()
						.inventory(inventoryItems)
						.heroClass(updatedCollection.getHeroClass().toString())
						.name(updatedCollection.getName())
						.type(CollectionTypes.DECK.toString())
						.userId(updatedCollection.getUserId())));
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
	public WebResult<Void> decksDelete(RoutingContext context, String userId, String deckId) throws SuspendExecution, InterruptedException {
		return null;
	}

	@Override
	public WebResult<MatchmakingQueuePutResponse> matchmakingConstructedQueuePut(RoutingContext routingContext, String userId, MatchmakingQueuePutRequest request) throws SuspendExecution, InterruptedException {
		MatchmakingRequest internalRequest = new MatchmakingRequest(request, userId);
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
		}

		// Determine status code
		int statusCode = 200;
		if (internalResponse.getRetry() != null) {
			userResponse.retry(new MatchmakingQueuePutRequest()
					.deckId(request.getDeckId()));
			statusCode = 202;
		}

		final HttpServerResponse response = routingContext.response();
		response.setStatusCode(statusCode);
		response.headers().add("Content-Type", "application/json");

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
}
