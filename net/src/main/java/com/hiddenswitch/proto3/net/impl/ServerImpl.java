package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.client.models.JavaSerializationObject;
import com.hiddenswitch.proto3.net.client.models.MatchmakingQueuePutRequest;
import com.hiddenswitch.proto3.net.client.models.MatchmakingQueuePutResponse;
import com.hiddenswitch.proto3.net.models.MatchCancelRequest;
import com.hiddenswitch.proto3.net.models.MatchCancelResponse;
import com.hiddenswitch.proto3.net.models.MatchmakingRequest;
import com.hiddenswitch.proto3.net.models.MatchmakingResponse;
import com.hiddenswitch.proto3.net.util.Serialization;
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

import java.io.IOException;

import static io.vertx.ext.sync.Sync.awaitResult;

/**
 * Created by bberman on 11/27/16.
 */
public class ServerImpl extends SyncVerticle {
	GamesImpl games = new GamesImpl();
	MatchmakingImpl matchmaking = new MatchmakingImpl();
	BotsImpl bots = new BotsImpl();

	@Override
	@Suspendable
	public void start() {
		Logger logger = LoggerFactory.getLogger(ServerImpl.class);
		HttpServer server = vertx.createHttpServer(new HttpServerOptions()
				.setHost("0.0.0.0")
				.setPort(8080));
		Router router = Router.router(vertx);

		try {


			logger.info("Deploying gameGessions...");
			String socketServerDeploymentId = awaitResult(done -> vertx.deployVerticle(games, done));
			logger.info("Deployed games with verticle ID " + socketServerDeploymentId);


			String gamesDeploymentId = awaitResult(done -> vertx.deployVerticle(matchmaking, done));
			logger.info("Deployed matchmaking with verticle ID " + gamesDeploymentId);

			logger.info("Deploying bots...");

			String botsDeploymentId = awaitResult(done -> vertx.deployVerticle(bots, done));
			logger.info("Deployed bots with verticle ID " + botsDeploymentId);

			logger.info("Configuring router...");
			final String MATCHMAKE_PATH = "/v1/matchmaking/constructed/queue";

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
						.javaSerialized(Serialization.serializeBytes(matchmakingResponse.getConnection()));
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
}
