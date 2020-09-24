package com.hiddenswitch.framework;

import com.avast.grpc.jwt.server.JwtServerInterceptor;
import com.google.common.collect.ImmutableMap;
import com.hiddenswitch.framework.rpc.*;
import com.hiddenswitch.framework.schema.tables.daos.UserEntityDao;
import com.hiddenswitch.framework.schema.tables.pojos.UserEntity;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.engines.vertx.VertxClientHttpEngine;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.common.VerificationException;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.*;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class Accounts {
	protected static final String KEYCLOAK_LOGIN_PATH = "/realms/hiddenswitch/protocol/openid-connect/token";
	protected static final String CLIENT_SECRET = "clientsecret";
	protected static final String CLIENT_ID = "spellsource";
	private static final AtomicReference<Keycloak> keycloakReference = new AtomicReference<>();
	private static final AtomicReference<JwtServerInterceptor<AccessToken>> interceptor = new AtomicReference<>();
	private static String realmId;

	/**
	 * Does not make blocking calls, so the interceptor can be synchronous on the event loop!
	 *
	 * @return
	 */
	public static Future<ServerInterceptor> authorizationInterceptor() {
		try {
			return Future.succeededFuture(interceptor.updateAndGet(existing -> {
				if (existing != null) {
					return existing;
				}

				return new JwtServerInterceptor<>(jwtToken -> {
					var verified = TokenVerifier.create(jwtToken, AccessToken.class);
					try {
						return CompletableFuture.completedFuture(verified.getToken());
					} catch (VerificationException e) {
						return CompletableFuture.failedFuture(e);
					}
				});
			}));
		} catch (Throwable t) {
			return Future.failedFuture(t);
		}
	}

	public static Future<UserEntity> user() {
		// are we in a grpc context?
		var interceptor = Accounts.interceptor.get();
		if (interceptor != null) {
			var accessTokenContextKey = interceptor.AccessTokenContextKey;
			if (accessTokenContextKey != null) {
				var accessToken = accessTokenContextKey.get();
				if (accessToken == null) {
					return Future.succeededFuture();
				} else {
					return (new UserEntityDao(Environment.jooq(), Environment.pool())).findOneById(accessToken.getSubject());
				}
			}
		}

		// For now we do not support getting the context any other way
		return Future.failedFuture("no context");
	}

	public static Future<BindableService> unauthenticatedService() {
		var context = Vertx.currentContext();
		Objects.requireNonNull(context, "context");

		var webClient = WebClient.create(context.owner());

		return Future.succeededFuture(new UnauthenticatedGrpc.UnauthenticatedVertxImplBase() {

			private LoginOrCreateReply handleAccessTokenUserEntityTuple(Object[] tuple) {
				var accessTokenResponse = (org.keycloak.representations.AccessTokenResponse) tuple[0];
				var userEntity = (UserEntity) tuple[1];
				return LoginOrCreateReply.newBuilder()
						.setUserEntity(com.hiddenswitch.framework.rpc.UserEntity.newBuilder().setId(userEntity.getId()).setEmail(userEntity.getEmail()).setUsername(userEntity.getUsername()).build())
						.setAccessTokenResponse(AccessTokenResponse.newBuilder().setToken(accessTokenResponse.getToken()).build()).build();
			}

			@Override
			public void createAccount(CreateAccountRequest request, Promise<LoginOrCreateReply> response) {
				createUser(request.getEmail(), request.getUsername(), request.getPassword())
						.compose(userEntity -> {
							// Login here
							var client = new Client(context.owner(), webClient);
							return client.login(request.getEmail(), request.getPassword()).map(accessTokenResponse -> new Object[]{accessTokenResponse, userEntity});
						})
						.map(this::handleAccessTokenUserEntityTuple)
						.onComplete(response);
			}

			@Override
			public void login(LoginRequest request, Promise<LoginOrCreateReply> response) {
				var client = new Client(context.owner(), webClient);
				client.login(request.getUsernameOrEmail(), request.getPassword())
						.compose(accessTokenResponse -> {
							var token = TokenVerifier.create(accessTokenResponse.getToken(), AccessToken.class);
							try {
								var userId = token.getToken().getSubject();
								var userEntityRes = (new UserEntityDao(Environment.jooq(), Environment.pool()))
										.findOneById(userId);
								return userEntityRes.map(userEntity -> new Object[]{accessTokenResponse, userEntity});
							} catch (VerificationException e) {
								return Future.failedFuture(e);
							}
						})
						.map(this::handleAccessTokenUserEntityTuple)
						.onComplete(response);
			}
		});
	}

	public static Future<ServerServiceDefinition> authenticatedService() {
		return Future.succeededFuture(new AccountsGrpc.AccountsVertxImplBase() {

			@Override
			public void getAccounts(GetAccountsRequest request, Promise<GetAccountsReply> response) {
				user()
						.compose(thisUser -> {
							if (thisUser == null) {
								return Future.failedFuture("must log in");
							}

							// TODO: Join with friends
							return (new UserEntityDao(Environment.jooq(), Environment.pool()))
									.findManyByIds(request.getIdsList())
									.map(users ->
											GetAccountsReply.newBuilder()
													.addAllUserEntities(users.stream().map(ue -> {
														var build = com.hiddenswitch.framework.rpc.UserEntity.newBuilder()
																.setUsername(ue.getUsername())
																.setId(ue.getId());

														if (ue.getId().equals(thisUser.getId())) {
															build.setEmail(ue.getEmail());
														}
														return build.build();
													}).collect(Collectors.toList())).build());
						})
						.onComplete(response);
			}
		})
				.compose(service ->
						// Does not require vertx blocking service because it makes no blocking calls
						Future.succeededFuture(ServerInterceptors.intercept(service, Accounts.authorizationInterceptor().result())));
	}

	public static Future<RealmResource> get() {
		return Environment.executeBlocking(() -> keycloakReference.get().realm(realmId));
	}

	public static Future<UserEntity> createUser(String email, String username, String password) {
		return get()
				.compose(realm -> {
					var user = new UserRepresentation();
					user.setEmail(email);
					user.setUsername(username);
					user.setEnabled(true);
					var credential = new CredentialRepresentation();
					user.setCredentials(Collections.singletonList(credential));
					credential.setType(CredentialRepresentation.PASSWORD);
					credential.setValue(password);
					credential.setTemporary(false);

					// TODO: Not sure yet how to get the ID of the user you just created
					return Environment.executeBlocking(() -> realm.users().create(user)).map(user);
				})
				.compose(userRepresentation -> {
					var users = new UserEntityDao(Environment.jooq(), Environment.pool());
					return users.findManyByEmail(Collections.singletonList(userRepresentation.getEmail()), 1);
				})
				.compose(result -> {
					if (result.size() != 1) {
						return Future.failedFuture(new ArrayIndexOutOfBoundsException("invalid user creation result"));
					}
					return Future.succeededFuture(result.get(0));
				});
	}

	public static Future<RealmResource> getOrCreate(String authServerUrl,
	                                                String adminUsername,
	                                                String adminPassword,
	                                                String clientSecret,
	                                                String clientId,
	                                                String realmId,
	                                                String realmDisplayName) {
		return Environment.executeBlocking(() -> {
			var context = Vertx.currentContext();
			var keycloak = keycloakReference.updateAndGet(existing -> {
				if (existing != null) {
					return existing;
				}
				var client = (ResteasyClientBuilder) ResteasyClientBuilder.newBuilder();


				if (context != null) {
					var engine = new VertxClientHttpEngine(context.owner());
					client.httpEngine(engine);
				}

				return KeycloakBuilder.builder()
						.serverUrl(authServerUrl)
						.realm("master")
						.username(adminUsername)
						.password(adminPassword)
						.clientId("admin-cli")
						.grantType("password")
						.resteasyClient(client.build())
						.build();
			});

			if (Accounts.realmId != null && !Objects.equals(Accounts.realmId, realmId)) {
				// TODO: Warn that realm was already set.
			}

			Accounts.realmId = realmId;

			var existing = keycloak.realms().findAll().stream().filter(realm -> realm.getId().equals(Accounts.realmId)).findFirst();

			if (existing.isPresent()) {
				return keycloak.realm(Accounts.realmId);
			}

			// Create a default
			var realmRepresentation = new RealmRepresentation();
			realmRepresentation.setRealm(Accounts.realmId);
			realmRepresentation.setDisplayName(realmDisplayName);
			realmRepresentation.setSslRequired(SslRequired.EXTERNAL.toString());
			realmRepresentation.setEnabled(true);

			keycloak.realms().create(realmRepresentation);

			var realm = keycloak.realms().realm(Accounts.realmId);
			var flows = realm.flows().getFlows()
					.stream().collect(toMap(AuthenticationFlowRepresentation::getAlias, AuthenticationFlowRepresentation::getId));
			var client = new ClientRepresentation();
			client.setClientId(clientId);
			client.setDirectAccessGrantsEnabled(true);

			// Should now be confidential
			client.setClientAuthenticatorType("client-secret");
			client.setServiceAccountsEnabled(false);
			client.setStandardFlowEnabled(true);
			client.setSecret(clientSecret);
			client.setRedirectUris(Collections.singletonList("/oauth2callback"));
			client.setAuthenticationFlowBindingOverrides(ImmutableMap.of(
					"direct_grant", flows.get("direct grant"),
					"browser", flows.get("browser")
			));

			client.setWebOrigins(Collections.singletonList("+"));
			realm.clients().create(client);
			return realm;
		});
	}

	public static String keycloakAuthUrl() {
		return (String) System.getProperties().getOrDefault("keycloak.auth.url", "http://localhost:8080/auth");
	}
}
