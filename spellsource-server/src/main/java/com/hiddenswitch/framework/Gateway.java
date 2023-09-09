package com.hiddenswitch.framework;

import com.hiddenswitch.framework.impl.BindAll;
import com.hiddenswitch.framework.impl.SqlCachedCardCatalogue;
import com.hiddenswitch.framework.rpc.AccountsGrpc;
import com.hiddenswitch.framework.rpc.AuthenticatedCardsGrpc;
import com.hiddenswitch.framework.rpc.GamesGrpc;
import com.hiddenswitch.framework.virtual.VirtualThreadRoutingContextHandler;
import com.hiddenswitch.framework.virtual.concurrent.AbstractVirtualThreadVerticle;
import com.hiddenswitch.spellsource.rpc.HiddenSwitchSpellsourceAPIServiceGrpc;
import com.hiddenswitch.spellsource.rpc.MatchmakingGrpc;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServiceBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.vertx.await.Async.await;

public class Gateway extends AbstractVirtualThreadVerticle {
	private static Logger LOGGER = LoggerFactory.getLogger(Gateway.class);
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
				Legacy.unauthenticatedCards(cardCatalogue),
				Legacy.authenticatedCards(cardCatalogue),
				matchmaking.binder(),
				Accounts.unauthenticatedService(),
				Accounts.authenticatedService(),
				Games.services()};

		var server = GrpcServer.server(vertx);
		var jwtAuth = JWTAuth.create(vertx, Accounts.jwtAuthOptions());
		var router = Router.router(vertx);
		this.httpServer = vertx.createHttpServer(new HttpServerOptions()
				.setTcpKeepAlive(true));

		var realm = await(Accounts.realm());
		for (var serviceName : new String[]{
				HiddenSwitchSpellsourceAPIServiceGrpc.SERVICE_NAME,
				MatchmakingGrpc.SERVICE_NAME,
				AccountsGrpc.SERVICE_NAME,
				GamesGrpc.SERVICE_NAME,
				AuthenticatedCardsGrpc.SERVICE_NAME
		}) {
			router.route("/" + serviceName + "/*").handler(JWTAuthHandler.create(jwtAuth, realm.toRepresentation().getRealm()));
		}

		for (var service : services) {
			// basic interceptors for grpc
			service.bindAll(server);
		}

		GrpcServiceBridge.bridge(ProtoReflectionService.newInstance().bindService()).bind(server);
		server.mount(router);

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

}
