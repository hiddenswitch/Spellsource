package com.hiddenswitch.framework;

import com.google.protobuf.Empty;
import com.hiddenswitch.framework.impl.GrpcClientWithOptions;
import com.hiddenswitch.framework.rpc.Hiddenswitch.CreateAccountRequest;
import com.hiddenswitch.framework.rpc.Hiddenswitch.LoginOrCreateReply;
import com.hiddenswitch.framework.rpc.Hiddenswitch.LoginOrCreateReplyOrBuilder;
import com.hiddenswitch.framework.rpc.VertxAuthenticatedCardsGrpcClient;
import com.hiddenswitch.framework.rpc.VertxUnauthenticatedCardsGrpcClient;
import com.hiddenswitch.framework.rpc.VertxUnauthenticatedGrpcClient;
import com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity;
import com.hiddenswitch.spellsource.rpc.MatchmakingGrpc;
import com.hiddenswitch.spellsource.rpc.Spellsource.*;
import com.hiddenswitch.spellsource.rpc.Spellsource.MessageTypeMessage.MessageType;
import com.hiddenswitch.spellsource.rpc.VertxHiddenSwitchSpellsourceAPIServiceGrpcClient;
import com.hiddenswitch.spellsource.rpc.VertxMatchmakingGrpcClient;
import io.grpc.Status;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.vertx.ext.auth.impl.jose.JWK;
import io.vertx.ext.auth.impl.jose.JWT;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.grpc.common.GrpcStatus;
import net.demilich.metastone.game.logic.XORShiftRandom;
import org.keycloak.representations.AccessTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;

import static io.vertx.await.Async.await;
import static io.vertx.core.CompositeFuture.all;
import static io.vertx.core.CompositeFuture.any;

public class Client {
	protected final AtomicReference<GrpcClientWithOptions> managedChannel = new AtomicReference<>();
	protected final Vertx vertx;
	protected final WebClient webClient;
	protected final String keycloakPath;
	private final Logger LOGGER = LoggerFactory.getLogger(Client.class);
	private final String loginUri;
	private final XORShiftRandom random = new XORShiftRandom(System.nanoTime());
	private final JWT jwt;
	private AccessTokenResponse accessToken;
	private UserEntity userEntity;
	private String username;
	private String email;
	private String password;
	private Promise<MatchmakingQueuePutResponse> matchmakingResponseFut;
	private Promise<Void> matchmakingEndedFut;

	public Client(Vertx vertx, WebClient webClient, String keycloakPath) {
		this.vertx = vertx;
		this.webClient = webClient;
		this.keycloakPath = keycloakPath;
		this.jwt = new JWT();
		loginUri = URI.create(keycloakPath + Accounts.KEYCLOAK_LOGIN_PATH).normalize().toString();
	}

	public Client(Vertx vertx, WebClient webClient, String username, String email, String password) {
		this(vertx, webClient);
		this.username = username;
		this.email = email;
		this.password = password;
	}

	public Client(Vertx vertx, WebClient webClient) {
		this(vertx, webClient, Accounts.keycloakAuthUrl());
	}

	public Client(Vertx vertx) {
		this(vertx, WebClient.create(vertx, new WebClientOptions()
				.setKeepAlive(true)
				.setProtocolVersion(HttpVersion.HTTP_2)));
	}

	public Future<ServerToClientMessage> connectToGame() {
		var writerFut = Promise.<WriteStream<ClientToServerMessage>>promise();
		var promise = Promise.<ServerToClientMessage>promise();
		var readerFut = legacy().subscribeGame(writerFut::tryComplete);

		readerFut.onSuccess(reader -> reader.handler(promise::complete));
		readerFut.onSuccess(reader -> reader.exceptionHandler(promise::fail));
		readerFut.onFailure(promise::fail);

		return writerFut.future()
				.compose(writer -> {
					writer.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageTypeMessage.MessageType.FIRST_MESSAGE)
									.build())
							.onFailure(promise::tryFail);
					return promise.future();
				});
	}

	public Future<ServerToClientMessage> playUntilGameOver() {
		var writerPromise = Promise.<WriteStream<ClientToServerMessage>>promise();
		var readerFut = legacy().subscribeGame(writerPromise::tryComplete);
		var writerFut = writerPromise.future();
		var gameOverPromise = Promise.<ServerToClientMessage>promise();

		readerFut.onSuccess(reader -> reader.handler(message -> {
			var writer = writerFut.result();
			switch (message.getMessageType()) {
				case ON_MULLIGAN:
					writer.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageTypeMessage.MessageType.UPDATE_MULLIGAN)
									.setRepliesTo(message.getId())
									.addDiscardedCardIndices(0)
									.build())
							.onFailure(gameOverPromise::tryFail);
					break;
				case ON_REQUEST_ACTION:
					writer.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.UPDATE_ACTION)
									.setRepliesTo(message.getId())
									.setActionIndex(random.nextInt(message.getActions().getAllCount()))
									.build())
							.onFailure(gameOverPromise::tryFail);
					break;
				case ON_GAME_END:
					gameOverPromise.complete(message);
					break;
			}
		}));

		readerFut.onFailure(gameOverPromise::tryFail);
		readerFut.onSuccess(reader -> reader.exceptionHandler(gameOverPromise::tryFail));

		return writerFut
				.compose(writer -> {
					writer.exceptionHandler(gameOverPromise::tryFail);
					writer.write(ClientToServerMessage.newBuilder()
									.setMessageType(MessageType.FIRST_MESSAGE)
									.build())
							.onFailure(gameOverPromise::tryFail);
					return gameOverPromise
							.future()
							.eventually(v -> writer.end());
				});
	}

	public Future<MatchmakingQueuePutResponse> matchmake(VertxMatchmakingGrpcClient matchmaking, String queueId, String deckId) {
		if (matchmakingResponseFut != null) {
			throw new RuntimeException("already matchmaking");
		}
		LOGGER.trace("starting matchmaking for {}", userEntity.getId());
		var matchmakingRequestsFut = Promise.<WriteStream<MatchmakingQueuePutRequest>>promise();
		var matchmakingResponsesFut = matchmaking.enqueue(matchmakingRequestsFut::complete);
		var request = matchmakingRequestsFut.future()
				.compose(matchmakingRequests -> {
					LOGGER.trace("sent matchmaking put for {}", userEntity.getId());
					return matchmakingRequests.write(MatchmakingQueuePutRequest.newBuilder()
							.setDeckId(deckId)
							.setQueueId(queueId).build());
				});
		matchmakingResponseFut = Promise.<MatchmakingQueuePutResponse>promise();
		matchmakingEndedFut = Promise.<Void>promise();
		matchmakingResponsesFut.onSuccess(matchmakingResponses -> {
			matchmakingResponses.handler(matchmakingResponseFut::complete);
			matchmakingResponses.endHandler(matchmakingEndedFut::complete);
		});
		var response = matchmakingResponseFut.future();
		// deals with cancellation
		response.onFailure(t -> {
			var requests = matchmakingRequestsFut.future().result();
			if (t instanceof CancellationException) {
				requests.write(MatchmakingQueuePutRequest.newBuilder().setQueueId(queueId).setCancel(true).build());
			}
		});
		return all(request, any(response, matchmakingEndedFut.future()).map(cf -> {
			if (cf.isComplete(0)) {
				return cf.<MatchmakingQueuePutResponse>resultAt(0);
			}
			return null;
		}))
				.onComplete(ignored -> {
					if (matchmakingResponseFut.future().succeeded()) {
						matchmakingRequestsFut.future().result().end();
					}
					matchmakingResponseFut = null;
				})
				.onFailure(Environment.onFailure())
				.map(f -> f.resultAt(1));
	}

	public Future<MatchmakingQueuePutResponse> matchmake(String queueId) {
		return matchmake(matchmaking(), queueId);
	}

	public Future<MatchmakingQueuePutResponse> matchmake(VertxMatchmakingGrpcClient matchmaking, String queueId) {
		var random = new Random();
		return legacy()
				.decksGetAll(Empty.getDefaultInstance())
				.compose(decks -> {
					var index = random.nextInt(0, decks.getDecksCount());
					return matchmake(matchmaking, queueId, decks.getDecks(index).getCollection().getId());
				})
				.onFailure(Environment.onFailure());
	}

	public Future<Void> cancelMatchmaking() {
		if (matchmakingResponseFut == null) {
			throw new RuntimeException("not matchmaking");
		}
		matchmakingResponseFut.tryFail(new CancellationException("cancelling matchmaking"));
		return matchmakingEndedFut.future();
	}

	public Future<LoginOrCreateReply> createAndLogin(String username, String email, String password) {
		return createAndLogin(username, email, password, true);
	}

	public Future<LoginOrCreateReply> createAndLogin(String username, String email, String password, boolean premadeDecks) {
		var stub = new VertxUnauthenticatedGrpcClient(client(), address());
		return stub.createAccount(CreateAccountRequest.newBuilder()
						.setUsername(username)
						.setEmail(email)
						.setPassword(password)
						.setDecks(premadeDecks)
						.build())
				.onFailure(Environment.onFailure())
				.compose(this::handleCreateAccountReply);
	}

	public Future<LoginOrCreateReply> createAndLogin() {
		return createAndLogin(UUID.randomUUID().toString(), UUID.randomUUID().toString().replace("-", ".") + "@hiddenswitch.com", UUID.randomUUID().toString());
	}

	public MultiMap credentials() {
		if (getAccessToken() != null) {
			return HeadersMultiMap.httpHeaders().add("Authorization", "Bearer " + getAccessToken().getToken());
		} else {
			await(login(email, password)
					.onSuccess(this::handleLogin)
					.map(AccessTokenResponse::getToken)
					.recover(ignored -> createAndLogin(username, email, password)
							.onSuccess(this::handleCreateAccountReply)
							.onFailure(Environment.onFailure())
							.map(response -> response.getAccessTokenResponse().getToken())));
			return credentials();
		}
	}


	public Future<AccessTokenResponse> privilegedCreateAndLogin(String email, String username, String password) {
		return Accounts.createUser(email, username, password)
				.onSuccess(this::handleAccountsCreateUser)
				.compose(ignored -> login(username, password));
	}

	public Future<AccessTokenResponse> login(String emailOrUsername, String password) {
		Objects.requireNonNull(webClient);
		var serverConfiguration = Environment.getConfiguration();
		var promise = Promise.<HttpResponse<Buffer>>promise();
		webClient.postAbs(loginUri)
				.sendForm(MultiMap.caseInsensitiveMultiMap()
						.add("client_id", serverConfiguration.getKeycloak().getClientId())
						.add("grant_type", "password")
						.add("client_secret", serverConfiguration.getKeycloak().getClientSecret())
						.add("scope", "openid")
						// username or password can be used here
						.add("username", emailOrUsername)
						.add("password", password), promise);
		return promise.future()
				.compose(res -> {
					var object = res.bodyAsJsonObject();
					if (object.containsKey("error")) {
						return Future.failedFuture(new RuntimeException(object.getString("error")));
					}

					var valid = object.mapTo(AccessTokenResponse.class);
					return Future.succeededFuture(valid);
				})
				.onSuccess(this::handleLogin);
	}

	public AccessTokenResponse getAccessToken() {
		return accessToken;
	}

	public UserEntity getUserEntity() {
		return userEntity;
	}

	public GrpcClientWithOptions client() {
		return managedChannel.updateAndGet(existing -> {
			if (existing != null) {
				return existing;
			}

			return new GrpcClientWithOptions(vertx, new HttpClientOptions()
					.setProtocolVersion(HttpVersion.HTTP_2)
					.setHttp2ClearTextUpgrade(false)
					.setSsl(false));
		});
	}

	protected String host() {
		return "localhost";
	}

	protected int port() {
		return Gateway.defaultGrpcPort();
	}

	public void close() {
		if (managedChannel.get() != null) {
			managedChannel.get().close();
		}
	}

	public Future<Void> closeFut() {
		return close((Object) null);
	}

	private Future<LoginOrCreateReply> handleCreateAccountReply(LoginOrCreateReply reply) {
		Future<LoginOrCreateReply> jwtComplete;
		if (this.jwt.isUnsecure()) {
			jwtComplete = Accounts.jwtAuthOptions(this.webClient)
					.compose(jwtAuthOptions -> {
						if (jwtAuthOptions.getJwks() != null) {
							jwtAuthOptions.getJwks().forEach(jwk -> jwt.addJWK(new JWK(jwk)));
						}
						if (jwtAuthOptions.getPubSecKeys() != null) {
							jwtAuthOptions.getPubSecKeys().forEach(psk -> jwt.addJWK(new JWK(psk)));
						}
						return Future.succeededFuture(reply);
					});
		} else {
			jwtComplete = Future.succeededFuture(reply);
		}
		return jwtComplete.compose(v -> {
			var jwtDecoded = this.jwt.decode(reply.getAccessTokenResponse().getToken());
			var atr = new AccessTokenResponse();
			atr.setToken(reply.getAccessTokenResponse().getToken());
			atr.setExpiresIn((jwtDecoded.getLong("exp") - jwtDecoded.getLong("iat")));
			handleLogin(atr);
			handleAccountsCreateUser(new UserEntity()
					.setId(reply.getUserEntity().getId())
					.setUsername(reply.getUserEntity().getUsername())
					.setEmail(reply.getUserEntity().getEmail()));
			this.accessToken.setToken(reply.getAccessTokenResponse().getToken());
			return Future.succeededFuture(reply);
		});
	}

	private void handleLogin(AccessTokenResponse accessToken) {
		this.accessToken = accessToken;
	}

	private void handleAccountsCreateUser(UserEntity userEntity) {
		this.userEntity = userEntity;
	}

	public VertxHiddenSwitchSpellsourceAPIServiceGrpcClient legacy() {
		return new VertxHiddenSwitchSpellsourceAPIServiceGrpcClient(client().setRequestOptions(new RequestOptions().setHeaders(credentials())), SocketAddress.inetSocketAddress(port(), host()));
	}

	public VertxAuthenticatedCardsGrpcClient cards() {
		return new VertxAuthenticatedCardsGrpcClient(client().setRequestOptions(new RequestOptions().setHeaders(credentials())), SocketAddress.inetSocketAddress(port(), host()));
	}

	public VertxMatchmakingGrpcClient matchmaking() {
		var client = client().setRequestOptions(new RequestOptions().setHeaders(credentials()));
		var socketAddress = address();
		return new VertxMatchmakingGrpcClient(client, socketAddress) {
			@Override
			public Future<ReadStream<MatchmakingQueuePutResponse>> enqueue(Handler<WriteStream<com.hiddenswitch.spellsource.rpc.Spellsource.MatchmakingQueuePutRequest>> request) {
				return client.request(socketAddress, MatchmakingGrpc.getEnqueueMethod())
						.compose(req -> {
							request.handle(req);
							var keepAliveManager = Environment.keepAliveManager(req.connection(), v -> {
								try {
									req.end();
								} catch (IllegalStateException ignored) {
									// already closed
								}
							}, true);
							keepAliveManager.onTransportStarted();
							req.exceptionHandler(t -> keepAliveManager.onTransportTermination());
							req.response().onSuccess(res -> {
								res.endHandler(v -> keepAliveManager.onTransportTermination());
							});
							return req.response().flatMap(resp -> {
								if (resp.status() != null && resp.status() != GrpcStatus.OK) {
									keepAliveManager.onTransportTermination();
									return Future.failedFuture(Status.fromCodeValue(resp.status().code).withDescription(resp.statusMessage()).asRuntimeException());
								} else {
									keepAliveManager.onDataReceived();
									return Future.succeededFuture(resp);
								}
							});
						});
			}
		};
	}

	public Future<Void> close(Object ignored) {
		close();
		return Future.succeededFuture();
	}

	public Future<MatchmakingQueuePutResponse> matchmakingResponse() {
		if (matchmakingResponseFut == null) {
			return Future.failedFuture("not matchmaking");
		}
		return matchmakingResponseFut.future();
	}

	public void close(AsyncResult<?> ignored) {
		close();
	}

	public VertxUnauthenticatedCardsGrpcClient unauthenticatedCards() {
		return new VertxUnauthenticatedCardsGrpcClient(client(), address());
	}

	public VertxUnauthenticatedGrpcClient unauthenticated() {
		return new VertxUnauthenticatedGrpcClient(client(), address());
	}

	public SocketAddress address() {
		return SocketAddress.inetSocketAddress(port(), host());
	}
}
