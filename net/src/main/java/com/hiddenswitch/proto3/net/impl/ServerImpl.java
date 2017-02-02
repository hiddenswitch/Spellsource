package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.client.models.JavaSerializationObject;
import com.hiddenswitch.proto3.net.client.models.MatchmakingQueuePutRequest;
import com.hiddenswitch.proto3.net.client.models.MatchmakingQueuePutResponse;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Serialization;
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
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.LoggerHandler;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.cards.CardSet;
import net.demilich.metastone.game.decks.DeckFormat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Created by bberman on 11/27/16.
 */
public class ServerImpl extends SyncVerticle {
	AccountsImpl accounts = new AccountsImpl().withEmbeddedConfiguration();
	GamesImpl games = new GamesImpl();
	MatchmakingImpl matchmaking = new MatchmakingImpl();
	BotsImpl bots = new BotsImpl();
	LogicImpl logic = new LogicImpl();
	InventoryImpl inventory = new InventoryImpl();

	@Override
	@Suspendable
	public void start() {
		Logger logger = LoggerFactory.getLogger(ServerImpl.class);
		HttpServer server = vertx.createHttpServer(new HttpServerOptions()
				.setHost("0.0.0.0")
				.setPort(8080));
		Router router = Router.router(vertx);

		try {
			for (Verticle verticle : Arrays.asList(accounts, games, matchmaking, bots, logic, inventory)) {
				final String name = verticle.getClass().getName();
				logger.info("Deploying " + name + "...");
				String deploymentId = Sync.awaitResult(done -> vertx.deployVerticle(verticle, done));
				logger.info("Deployed " + name + " with ID " + deploymentId);
			}

			logger.info("Configuring router...");
			final String MATCHMAKE_PATH = "/v1/matchmaking/constructed/queue";

			router.route("/*")
					.handler(LoggerHandler.create());

			router.route(MATCHMAKE_PATH)
					.method(HttpMethod.DELETE)
					.blockingHandler(Sync.fiberHandler(this::matchmakingConstructedQueueDelete));

			router.route(MATCHMAKE_PATH)
					.method(HttpMethod.PUT)
					.consumes("application/json")
					.produces("application/json")
					.handler(BodyHandler.create());

			router.route(MATCHMAKE_PATH)
					.method(HttpMethod.PUT)
					.blockingHandler(Sync.fiberHandler(this::matchmakingConstructedQueuePut));

			router.route("/test")
					.method(HttpMethod.GET)
					.handler(Sync.fiberHandler(routingContext -> {
						final String userId = "doctorpangloss";
						CreateAccountResponse createAccountResponse = accounts.createAccount("benjamin.s.berman@gmail.com", "testpass", userId);

						try {
							logic.initializeUser(new InitializeUserRequest().withUserId(userId));
						} catch (InterruptedException | SuspendExecution e) {
							routingContext.fail(e);
							return;
						}

						GetCollectionResponse response = null;
						try {
							response = inventory.getCollection(new GetCollectionRequest(userId));
						} catch (SuspendExecution suspendExecution) {
							routingContext.fail(suspendExecution);
							return;
						}

						Set<String> cardIds = null;
						if (response != null) {
							cardIds = response.getInventoryRecords().stream().map(r -> r.card.getCardId()).collect(Collectors.toSet());
						} else {
							routingContext.fail(new NullPointerException());
							return;
						}

						// Should contain the classic cards
						final DeckFormat deckFormat = new DeckFormat().withCardSets(CardSet.BASIC, CardSet.CLASSIC);
						final List<String> basicClassicCardIds = CardCatalogue.query(deckFormat).toList().stream().map(Card::getCardId).collect(Collectors.toList());
						logger.info("Contains all? " + Boolean.toString(cardIds.containsAll(basicClassicCardIds)));
						routingContext.response().end();
					}));

			router.route(MATCHMAKE_PATH).failureHandler(LoggerHandler.create());

			logger.info("Router configured.");
			HttpServer listening = awaitResult(done -> server.requestHandler(router::accept).listen(done));
			logger.info("Listening on port " + Integer.toString(server.actualPort()));
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Suspendable
	public void matchmakingConstructedQueueDelete(RoutingContext routingContext) {
		String userId = routingContext.request().getHeader("X-Auth-UserId");
		MatchCancelResponse response = matchmaking.cancel(new MatchCancelRequest(userId));
		com.hiddenswitch.proto3.net.client.models.MatchCancelResponse userResponse =
				new com.hiddenswitch.proto3.net.client.models.MatchCancelResponse()
						.isCanceled(response.getCanceled());
		routingContext.response().setStatusCode(200);
		routingContext.response().end(Serialization.serialize(userResponse));
	}

	@Suspendable
	public void matchmakingConstructedQueuePut(RoutingContext routingContext) {
		MatchmakingQueuePutRequest userRequest = Serialization.deserialize(routingContext.getBodyAsString(), MatchmakingQueuePutRequest.class);
		// TODO: Use real user IDs
		String userId = routingContext.request().getHeader("X-Auth-UserId");
		MatchmakingRequest request = new MatchmakingRequest(userRequest, userId);
		MatchmakingResponse matchmakingResponse = null;
		try {
			matchmakingResponse = matchmaking.matchmakeAndJoin(request);
		} catch (InterruptedException | SuspendExecution e) {
			routingContext.fail(e);
			return;
		}
		MatchmakingQueuePutResponse userResponse = new MatchmakingQueuePutResponse();
		if (matchmakingResponse.getConnection() != null) {
			final JavaSerializationObject connection;
			try {
				connection = new JavaSerializationObject()
						.javaSerialized(Serialization.serializeBase64(matchmakingResponse.getConnection()));
			} catch (IOException e) {
				routingContext.fail(e);
				return;
			}
			userResponse.connection(connection);
		}
		int statusCode = 200;
		if (matchmakingResponse.getRetry() != null) {
			userResponse.retry(new MatchmakingQueuePutRequest()
					.deck(request.getDeck()));
			statusCode = 202;
		}
		final HttpServerResponse response = routingContext.response();
		response.setStatusCode(statusCode);
		response.headers().add("Content-Type", "application/json");
		response.end(Serialization.serialize(matchmakingResponse));
	}

	@Suspendable
	public void accountsCreatePush(RoutingContext routingContext) {
//		AccountsCreatePushRequest userRequest = Serialization.deserialize(routingContext.getBodyAsString(), AccountsCreatePushRequest.class);

	}
}
