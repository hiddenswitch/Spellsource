package com.hiddenswitch.framework;

import com.avast.grpc.jwt.client.JwtCallCredentials;
import com.hiddenswitch.framework.rpc.*;
import com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity;
import com.hiddenswitch.spellsource.rpc.DecksPutResponse;
import com.hiddenswitch.spellsource.rpc.HiddenSwitchSpellsourceAPIServiceGrpc;
import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.grpc.VertxChannelBuilder;
import org.keycloak.representations.AccessTokenResponse;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Client implements AutoCloseable {
	protected final AtomicReference<ManagedChannel> managedChannel = new AtomicReference<>();
	protected final Vertx vertx;
	protected final WebClient webClient;
	protected final String keycloakPath;
	private AccessTokenResponse accessToken;
	private UserEntity userEntity;
	private String username;
	private String email;
	private String password;

	public Client(Vertx vertx, WebClient webClient, String keycloakPath) {
		this.vertx = vertx;
		this.webClient = webClient;
		this.keycloakPath = keycloakPath;
		vertx.getOrCreateContext().addCloseHook(handler -> {
			close();
			handler.handle(Future.succeededFuture());
		});
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

	public Future<LoginOrCreateReply> createAndLogin(String username, String email, String password) {
		var stub = UnauthenticatedGrpc.newVertxStub(channel());
		return stub.createAccount(CreateAccountRequest.newBuilder()
				.setUsername(username)
				.setEmail(email)
				.setPassword(password).build())
				.onSuccess(this::handleCreateAccountReply);
	}

	public Future<LoginOrCreateReply> createAndLogin() {
		return createAndLogin(UUID.randomUUID().toString(), UUID.randomUUID().toString().replace("-", ".") + "@hiddenswitch.com", UUID.randomUUID().toString());
	}

	public CallCredentials credentials() {
		if (getAccessToken() != null) {
			return new JwtCallCredentials.Synchronous(() -> getAccessToken().getToken());
		} else {
			return new JwtCallCredentials.Asynchronous(() ->
					login(email, password)
							.onSuccess(this::handleLogin)
							.map(AccessTokenResponse::getToken)
							.recover(ignored -> createAndLogin(username, email, password)
									.onSuccess(this::handleCreateAccountReply)
									.map(response -> response.getAccessTokenResponse().getToken()))
							.toCompletionStage()
							.toCompletableFuture());
		}
	}


	public Future<AccessTokenResponse> privilegedCreateAndLogin(String username, String email, String password) {
		return Accounts.createUser(username, email, password)
				.onSuccess(this::handleAccountsCreateUser)
				.compose(ignored -> login(username, password));
	}

	public Future<AccessTokenResponse> login(String usernameOrEmail, String password) {
		Objects.requireNonNull(webClient);
		return Environment.configuration()
				.compose(serverConfiguration -> {
					var promise = Promise.<HttpResponse<Buffer>>promise();
					webClient.postAbs(keycloakPath + Accounts.KEYCLOAK_LOGIN_PATH)
							.sendForm(MultiMap.caseInsensitiveMultiMap()
									.add("client_id", serverConfiguration.getKeycloak().getClientId())
									.add("grant_type", "password")
									.add("client_secret", serverConfiguration.getKeycloak().getClientSecret())
									.add("scope", "openid")
									// username or password can be used here
									.add("username", usernameOrEmail)
									.add("password", password), promise);
					return promise.future();
				})
				.map(res -> res.bodyAsJson(AccessTokenResponse.class))
				.onSuccess(this::handleLogin);
	}

	public AccessTokenResponse getAccessToken() {
		return accessToken;
	}

	public UserEntity getUserEntity() {
		return userEntity;
	}

	public ManagedChannel channel() {
		return managedChannel.updateAndGet(existing -> {
			if (existing != null) {
				return existing;
			}

			return VertxChannelBuilder.forTarget(vertx, grpcAddress())
					.usePlaintext()
					.build();
		});
	}

	public static String grpcAddress() {
		return "localhost:" + Gateway.grpcPort();
	}

	@Override
	public void close() {
		var managedChannel = this.managedChannel.get();
		if (managedChannel != null && !managedChannel.isShutdown()) {
			managedChannel.shutdownNow();
			try {
				managedChannel.awaitTermination(1900L, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
		}
	}

	private void handleCreateAccountReply(LoginOrCreateReplyOrBuilder reply) {
		handleLogin(new AccessTokenResponse());
		this.accessToken.setToken(reply.getAccessTokenResponse().getToken());
	}

	private void handleLogin(AccessTokenResponse accessToken) {
		this.accessToken = accessToken;
	}

	private void handleAccountsCreateUser(UserEntity userEntity) {
		this.userEntity = userEntity;
	}

	public HiddenSwitchSpellsourceAPIServiceGrpc.HiddenSwitchSpellsourceAPIServiceVertxStub legacy() {
		return HiddenSwitchSpellsourceAPIServiceGrpc.newVertxStub(channel())
				.withCallCredentials(credentials());
	}

	public Future<Void> close(Object ignored) {
		close();
		return Future.succeededFuture();
	}

	public void close(AsyncResult<?> ignored) {
		close();
	}
}