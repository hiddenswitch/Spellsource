package com.hiddenswitch.framework;

import com.hiddenswitch.framework.impl.BindAll;
import com.hiddenswitch.framework.impl.SqlCachedCardCatalogue;
import com.hiddenswitch.framework.rpc.Hiddenswitch;
import com.hiddenswitch.framework.rpc.VertxAuthenticatedCardsGrpcServer;
import com.hiddenswitch.framework.rpc.VertxUnauthenticatedCardsGrpcServer;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.grpc.server.GrpcServerRequest;
import org.jetbrains.annotations.NotNull;

public class Cards {
	private static final String GIT_CARD_URI = "git@github.com:hiddenswitch/Spellsource.git";
	private static final String WEBSITE_CARD_URI = "https://playspellsource.com/card-editor";

	public static BindAll<VertxAuthenticatedCardsGrpcServer.AuthenticatedCardsApi> authenticatedCards(SqlCachedCardCatalogue cardCatalogue) {
		return new VertxAuthenticatedCardsGrpcServer.AuthenticatedCardsApi() {
			@Override
			public void getCardsByUser(GrpcServerRequest<Hiddenswitch.GetCardsRequest, Hiddenswitch.GetCardsResponse> grpcServerRequest, Hiddenswitch.GetCardsRequest request, Promise<Hiddenswitch.GetCardsResponse> response) {
				if (!Thread.currentThread().isVirtual()) {
					throw new UnsupportedOperationException();
				}

				var userId = request.getUserId();

				if (request.getUserId().isBlank()) {
					userId = Gateway.ROUTING_CONTEXT.get().user().subject();
				}

				request = Hiddenswitch.GetCardsRequest.newBuilder(request).setUserId(userId).build();

				try {
					response.tryComplete(cardCatalogue.cachedRequest(request));
				} catch (Throwable t) {
					response.tryFail(Environment.onGrpcFailure().apply(t).cause());
				}
			}
		}::bindAll;
	}

	public static BindAll<VertxUnauthenticatedCardsGrpcServer.UnauthenticatedCardsApi> unauthenticatedCards(SqlCachedCardCatalogue cardCatalogue) {
		return new VertxUnauthenticatedCardsGrpcServer.UnauthenticatedCardsApi() {
			@Override
			public Future<Hiddenswitch.GetCardsResponse> getCards(@NotNull Hiddenswitch.GetCardsRequest request) {
				if (!Thread.currentThread().isVirtual()) {
					throw new UnsupportedOperationException();
				}

				request = Hiddenswitch.GetCardsRequest
						.newBuilder(request)
						.setUserId("").build();
				try {
					return Future.succeededFuture(cardCatalogue.cachedRequest(request));
				} catch (Throwable t) {
					return Environment.<Hiddenswitch.GetCardsResponse>onGrpcFailure().apply(t);
				}
			}
		}::bindAll;
	}
}
