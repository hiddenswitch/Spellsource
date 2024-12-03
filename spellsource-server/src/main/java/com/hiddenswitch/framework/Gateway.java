package com.hiddenswitch.framework;

import com.hiddenswitch.framework.impl.BindAll;
import com.hiddenswitch.framework.impl.SqlCachedCardCatalogue;
import com.hiddenswitch.framework.rpc.*;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import com.hiddenswitch.spellsource.rpc.HiddenSwitchSpellsourceAPIServiceGrpc;
import com.hiddenswitch.spellsource.rpc.MatchmakingGrpc;
import io.grpc.Context;
import io.grpc.InternalServer;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServiceBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.vertx.await.Async.await;

public class Gateway extends AbstractVirtualThreadVerticle {
	private static final Logger LOGGER = LoggerFactory.getLogger(Gateway.class);
	public static ThreadLocal<RoutingContext> ROUTING_CONTEXT = new ThreadLocal<>();
	private final SqlCachedCardCatalogue cardCatalogue = new SqlCachedCardCatalogue();
	private final int port;
	private Matchmaking.Services matchmaking;
	private HttpServer httpServer;

	public Gateway() {
		this.port = defaultGrpcPort();
	}

	public Gateway(int port) {
		this.port = port;
	}

	public static int defaultGrpcPort() {
		return Environment.getConfiguration().getGrpcConfiguration().getPort();
	}

	@Override
	public void startVirtual() {
		cardCatalogue.subscribe();
		cardCatalogue.invalidateAllAndRefresh();

		this.matchmaking = Matchmaking.services();
		var services = new BindAll<?>[]{
				Legacy.services(cardCatalogue),
				Cards.unauthenticatedCards(cardCatalogue),
				Cards.authenticatedCards(cardCatalogue),
				matchmaking.binder(),
				Accounts.unauthenticatedService(),
				Accounts.authenticatedService(),
				Games.services()};

		var server = GrpcServer.server(vertx);
		var jwtAuth = JWTAuth.create(vertx, Accounts.jwtAuthOptions());
		var router = Router.router(vertx);
		this.httpServer = vertx.createHttpServer(new HttpServerOptions()
				.setPort(this.port)
				.setHttp2ClearTextEnabled(true)
				.setSsl(false)
				.setTcpKeepAlive(true));

		var realm = await(Accounts.realm());
		for (var serviceName : new String[]{
				UnauthenticatedCardsGrpc.SERVICE_NAME,
				UnauthenticatedGrpc.SERVICE_NAME,
		}) {
			router.route("/" + serviceName + "/*")
					.handler(rc -> {
						ROUTING_CONTEXT.set(rc);
						server.handle(rc.request());
					});
		}
		for (var serviceName : new String[]{
				HiddenSwitchSpellsourceAPIServiceGrpc.SERVICE_NAME,
				MatchmakingGrpc.SERVICE_NAME,
				AccountsGrpc.SERVICE_NAME,
				GamesGrpc.SERVICE_NAME,
				AuthenticatedCardsGrpc.SERVICE_NAME
		}) {
			router.route("/" + serviceName + "/*")
					.handler(JWTAuthHandler.create(jwtAuth, realm.toRepresentation().getRealm()))
					.handler(rc -> {
						ROUTING_CONTEXT.set(rc);
						server.handle(rc.request());
					});
		}

		for (var service : services) {
			// basic interceptors for grpc
			service.bindAll(server);
		}

		await(httpServer.requestHandler(router)
				.listen(port));
	}

	@Override
	public void stopVirtual() {
		var promise1 = Promise.<Void>promise();
		matchmaking.clientMatchmakingService().close(promise1);
		await(promise1.future());
		await(this.httpServer.close());
	}

	public SqlCachedCardCatalogue getCardCatalogue() {
		return cardCatalogue;
	}

	public int getPort() {
		return port;
	}

	public Matchmaking.Services getMatchmaking() {
		return matchmaking;
	}
}
