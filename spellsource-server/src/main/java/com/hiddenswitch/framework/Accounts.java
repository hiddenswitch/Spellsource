package com.hiddenswitch.framework;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.hiddenswitch.framework.impl.BindAll;
import com.hiddenswitch.framework.impl.WeakVertxMap;
import com.hiddenswitch.framework.rpc.Hiddenswitch.*;
import com.hiddenswitch.framework.rpc.Hiddenswitch.ClientConfiguration.AccountsConfiguration;
import com.hiddenswitch.framework.rpc.Hiddenswitch.UserEntity.Builder;
import com.hiddenswitch.framework.rpc.VertxAccountsGrpcServer.AccountsApi;
import com.hiddenswitch.framework.rpc.VertxUnauthenticatedGrpcServer.UnauthenticatedApi;
import com.hiddenswitch.framework.schema.keycloak.tables.daos.UserEntityDao;
import com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity;
import com.hiddenswitch.framework.schema.spellsource.tables.mappers.RowMappers;
import com.hiddenswitch.framework.virtual.VirtualThreadRoutingContextHandler;
import io.grpc.Status;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.grpc.server.GrpcServerRequest;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.validator.routines.EmailValidator;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.common.VerificationException;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.credential.hash.Pbkdf2Sha512PasswordHashProviderFactory;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static com.hiddenswitch.framework.Environment.query;
import static com.hiddenswitch.framework.Environment.withDslContext;
import static com.hiddenswitch.framework.schema.keycloak.Keycloak.KEYCLOAK;
import static com.hiddenswitch.framework.schema.spellsource.Tables.GUESTS;
import static io.vertx.await.Async.await;
import static java.util.stream.Collectors.toMap;

/**
 * Implements the account services from {@link AccountsApi} against Keycloak, the open source Java identity management
 * application.
 * <p>
 * In the server, a {@link Keycloak} instance is used to create and manage user accounts.
 * <p>
 * The client creates an account with {@link com.hiddenswitch.framework.rpc.VertxUnauthenticatedGrpcClient}. The access
 * token returned by the reply, {@link LoginOrCreateReply#getAccessTokenResponse()}'s
 * {@link AccessTokenResponse#getToken()}
 * <p>
 * Keycloak manages the migration of the {@link com.hiddenswitch.framework.schema.keycloak.Keycloak} SQL schema. The
 * {@link com.hiddenswitch.framework.schema.keycloak.tables.UserEntity} table's
 * {@link com.hiddenswitch.framework.schema.keycloak.tables.UserEntity#ID} field is the user ID used throughout the
 * application.
 */
public class Accounts {
	public static final String SHOW_PREMADE_DECKS = "showPremadeDecks";
	protected static final String KEYCLOAK_LOGIN_PATH = "/realms/hiddenswitch/protocol/openid-connect/token";
	protected static final String KEYCLOAK_FORGOT_PASSWORD_PATH = "/realms/hiddenswitch/login-actions/reset-credentials";
	private static final Logger LOGGER = LoggerFactory.getLogger(Accounts.class);
	private static final WeakVertxMap<Keycloak> keycloakReference = new WeakVertxMap<>(Accounts::keycloakConstructor);
	public static final String WITH_PREMADE_DECKS = "With Premade Decks";
	public static final String DEFAULTS = "Defaults";

	/**
	 * Gets the user associated with the GRPC context currently being executed.
	 * <p>
	 * This must be called first in the GRPC method implementation body:
	 * <pre>
	 *   {@code
	 *    Future<Empty> grpcMethodOverride(Empty request) {
	 *      var userRequest = Accounts.user();
	 *      userRequest
	 *        .compose(user -> ...)
	 *        .onComplete(request);
	 *    }
	 *   }
	 * </pre>
	 * <p>
	 * Since vertx code is typically async, the GRPC context's data is only valid in the first scope of the GRPC server
	 * implementation.
	 *
	 * @return A {@link UserEntity} SQL model of a user record
	 * @see #userId() when the user ID will suffice
	 */
	public static Future<UserEntity> user(String userId) {
		try {
			var dao = new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
			var record = await(dao.findOneById(userId));
			return Future.succeededFuture(record);
		} catch (Throwable t) {
			return Future.failedFuture(t);
		}
	}


	/**
	 * Retrieves the user ID in a GRPC context.
	 * <p>
	 * This must be called first in the GRPC method implementation body:
	 * <pre>
	 *   {@code
	 *    Future<Empty> grpcMethodOverride(Empty request) {
	 *      var userRequest = Accounts.user();
	 *      userRequest
	 *        .compose(user -> ...)
	 *        .onComplete(request);
	 *    }
	 *   }
	 * </pre>
	 * <p>
	 * Since vertx code is typically async, the GRPC context's data is only valid in the first scope of the GRPC server
	 * implementation.
	 *
	 * @return a user ID, or {@code null} if no token was retrieved
	 */
	public static String userId() {
		var user = VirtualThreadRoutingContextHandler.current().user();
		if (user == null) {
			return null;
		}

		return user.subject();
	}

	/**
	 * Creates a {@link UnauthenticatedApi} implementation that handles account creation and logins over GRPC.
	 * <p>
	 * Eventually, the client will be ported to use native OAuth2 account creation provided by Keycloak.
	 *
	 * @return a {@link BindAll} to apply to a {@link io.vertx.grpc.server.GrpcServer}
	 */
	public static BindAll<UnauthenticatedApi> unauthenticatedService() {
		var context = Vertx.currentContext();
		Objects.requireNonNull(context, "context");

		var webClient = WebClient.create(context.owner());

		return new UnauthenticatedApi() {
			private final JWTAuth auth = JWTAuth.create(Vertx.currentContext().owner(), Accounts.jwtAuthOptions());

			private LoginOrCreateReply handleAccessTokenUserEntityTuple(Object[] tuple) {
				var accessTokenResponse = (org.keycloak.representations.AccessTokenResponse) tuple[0];
				var userEntity = (UserEntity) tuple[1];
				return LoginOrCreateReply.newBuilder()
						.setUserEntity(toProto(userEntity))
						.setAccessTokenResponse(AccessTokenResponse.newBuilder().setToken(accessTokenResponse.getToken()).build()).build();
			}

			@Override
			public Future<LoginOrCreateReply> createAccount(CreateAccountRequest request) {
				Future<UserEntity> createUserEntity;
				var email = request.getEmail();
				var password = request.getPassword();
				if (request.getGuest()) {
					var uuid1 = UUID.randomUUID().toString();
					password = UUID.randomUUID().toString();
					email = uuid1 + "@spellsource.com";
					var finalEmail = email;
					var finalPassword = password;
					// todo: make this a longer transaction
					createUserEntity = query(dsl -> dsl.insertInto(GUESTS).defaultValues().returning())
							.map(rowSet -> Lists.newArrayList(rowSet.iterator()).stream().map(RowMappers.getGuestsMapper()).collect(Collectors.toList()))
							.compose(guestRow -> {
								var guestId = guestRow.get(0).getId();
								return createUser(finalEmail, "Guest " + guestId, finalPassword)
										.compose(userEntity ->
												withDslContext(dsl ->
														dsl.update(GUESTS)
																.set(GUESTS.USER_ID, userEntity.getId())
																.where(GUESTS.ID.eq(guestId)))
														.map(userEntity));
							});
				} else {
					if (!EmailValidator.getInstance().isValid(email)) {
						return Future.failedFuture(Status.INVALID_ARGUMENT.withDescription("The e-mail address isn't valid.").asRuntimeException());
					}
					if (request.getUsername().length() <= 4) {
						return Future.failedFuture(Status.INVALID_ARGUMENT.withDescription("The username must be at least 4 characters.").asRuntimeException());
					}
					if (password.length() <= 4) {
						return Future.failedFuture(Status.INVALID_ARGUMENT.withDescription("The password must be at least 4 characters.").asRuntimeException());
					}

					final var finalEmail = email;
					final var finalPassword = password;
					createUserEntity = realm()
							.compose(realm -> Environment.executeBlocking(() -> realm.users().search(request.getUsername(), true)))
							.compose(existingUsers -> {
								if (!existingUsers.isEmpty()) {
									return Future.failedFuture(Status.INVALID_ARGUMENT.withDescription("This username already exists.").asRuntimeException());
								}

								return createUser(finalEmail, request.getUsername(), finalPassword)
										.recover(t -> Future.failedFuture(Status.INVALID_ARGUMENT.withDescription("The username, e-mail or password are invalid. Please check them and try again.").asRuntimeException()));
							});
				}


				final var finalEmail = email;
				final var finalPassword = password;
				return createUserEntity.compose(userEntity -> {
							// Login here
							var client = new Client(context.owner(), webClient);
							return client.login(finalEmail, finalPassword).map(accessTokenResponse -> new Object[]{accessTokenResponse, userEntity});
						})
						.map(this::handleAccessTokenUserEntityTuple)
						// hide the premade decks if the user requested to hide them
						.compose(reply -> withDslContext(dsl -> dsl.insertInto(KEYCLOAK.USER_ATTRIBUTE)
								.set(KEYCLOAK.USER_ATTRIBUTE.USER_ID, reply.getUserEntity().getId())
								.set(KEYCLOAK.USER_ATTRIBUTE.NAME, SHOW_PREMADE_DECKS)
								.set(KEYCLOAK.USER_ATTRIBUTE.ID, UUID.randomUUID().toString())
								.set(KEYCLOAK.USER_ATTRIBUTE.VALUE, request.getDecks() ? WITH_PREMADE_DECKS : DEFAULTS)
								.onDuplicateKeyIgnore())
								.map(reply))
						.recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<LoginOrCreateReply> login(LoginRequest request) {
				var client = new Client(context.owner(), webClient);
				return client.login(request.getUsernameOrEmail(), request.getPassword())
						.onComplete(v -> client.closeFut())
						.compose(accessTokenResponse -> {
							var token = TokenVerifier.create(accessTokenResponse.getToken(), AccessToken.class);
							try {
								var userId = token.getToken().getSubject();
								var userEntityRes = (new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient()))
										.findOneById(userId);
								return userEntityRes.map(userEntity -> new Object[]{accessTokenResponse, userEntity});
							} catch (VerificationException e) {
								return Future.failedFuture(e);
							}
						})
						.map(this::handleAccessTokenUserEntityTuple)
						.recover(t -> Future.failedFuture(Status.INVALID_ARGUMENT.withDescription("The username, email or password are invalid.").asRuntimeException()));
			}

			@Override
			public Future<BoolValue> verifyToken(AccessTokenResponse request) {
				return auth.authenticate(new TokenCredentials()
								.setToken(request.getToken()))
						.map(user -> BoolValue.of(user != null))
						.recover(t -> Future.succeededFuture(BoolValue.of(false)));
			}

			@Override
			public Future<ClientConfiguration> getConfiguration(Empty request) {
				var config = Environment.getConfiguration();
				var authUrl = config.getKeycloak().getPublicAuthUrl();
				return Future.succeededFuture(ClientConfiguration.newBuilder()
						.setAccounts(AccountsConfiguration.newBuilder()
								.setKeycloakResetPasswordUrl(URI.create(authUrl + KEYCLOAK_FORGOT_PASSWORD_PATH).normalize().toString())
								.setKeycloakAccountManagementUrl(URI.create(authUrl + KEYCLOAK_LOGIN_PATH).normalize().toString())
								.build())
						.build());
			}
		}::bindAll;
	}

	/**
	 * Creates a {@link AccountsApi} instance that handles account services for users that have logged in.
	 *
	 * @return a service for {@link io.vertx.grpc.server.GrpcServer}
	 */
	public static BindAll<AccountsApi> authenticatedService() {
		// Does not require vertx blocking service because it makes no blocking calls
		return new AccountsApi() {

			@Override
			public Future<LoginOrCreateReply> changePassword(GrpcServerRequest<ChangePasswordRequest, LoginOrCreateReply> grpcServerRequest, ChangePasswordRequest request) {
				var userId = grpcServerRequest.routingContext().user().subject();
				// for now we don't invalidate and refresh the old token
				var token = Accounts.token();
				if (token == null) {
					return Future.failedFuture("must log in with old password first");
				}
				return realm()
						.compose(realm -> Environment.executeBlocking(() -> {
							realm.users().get(userId).resetPassword(getPasswordCredential(request.getNewPassword()));
							return Future.succeededFuture();
						}))
						.compose(v -> (new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient()).findOneById(userId)))
						.map(userEntity -> LoginOrCreateReply.newBuilder()
								.setUserEntity(toProto(userEntity))
								.setAccessTokenResponse(AccessTokenResponse.newBuilder()
										.setToken(token)
										.build()).build())
						.recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<GetAccountsReply> getAccount(GrpcServerRequest<Empty, GetAccountsReply> grpcServerRequest, Empty request) {
				var userId = grpcServerRequest.routingContext().user().subject();
				return user(userId)
						.compose(thisUser -> {
							if (thisUser == null) {
								return Future.failedFuture("must log in");
							}

							return Future.succeededFuture(GetAccountsReply.newBuilder().addUserEntities(
									com.hiddenswitch.framework.rpc.Hiddenswitch.UserEntity.newBuilder()
											.setUsername(thisUser.getUsername())
											.setEmail(thisUser.getEmail())
											.setId(thisUser.getId())
											.build()
							).build());
						})
						.recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<GetAccountsReply> getAccounts(GrpcServerRequest<GetAccountsRequest, GetAccountsReply> grpcServerRequest, GetAccountsRequest request) {
				var userId = grpcServerRequest.routingContext().user().subject();
				return user(userId)
						.compose(thisUser -> {
							if (thisUser == null) {
								return Future.failedFuture("must log in");
							}

							// TODO: Join with friends
							return (new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient()))
									.findManyByIds(request.getIdsList())
									.map(users ->
											GetAccountsReply.newBuilder()
													.addAllUserEntities(users.stream().map(ue -> {
														var build = com.hiddenswitch.framework.rpc.Hiddenswitch.UserEntity.newBuilder()
																.setUsername(ue.getUsername())
																.setId(ue.getId());

														if (ue.getId().equals(thisUser.getId())) {
															build.setEmail(ue.getEmail());
														}
														return build.build();
													}).collect(Collectors.toList())).build());
						})
						.recover(Environment.onGrpcFailure());
			}
		}::bindAll;
	}

	private static Builder toProto(UserEntity userEntity) {
		return com.hiddenswitch.framework.rpc.Hiddenswitch.UserEntity.newBuilder()
				.setEmail(userEntity.getEmail())
				.setUsername(userEntity.getUsername())
				.setId(userEntity.getId());
	}

	private static String token() {
		// from JwtServerInterceptor
		var user = VirtualThreadRoutingContextHandler.current().user();
		if (user == null) {
			return null;
		}
		// from User.fromToken
		return user.get("access_token");
	}

	public static Future<RealmResource> realm() {
		var realmId = Environment.getConfiguration().getKeycloak().getRealmId();
		return Future.succeededFuture(keycloakReference.get().realm(realmId));
	}

	public static Future<UserEntity> createUser(String email, String username, String password) {
		return realm()
				.compose(realm -> {
					var user = new UserRepresentation();
					user.setEmail(email);
					user.setUsername(username);
					user.setEnabled(true);
					var credential = new CredentialRepresentation();
					credential.setType(CredentialRepresentation.PASSWORD);
					credential.setValue(password);
					credential.setTemporary(false);
					user.setCredentials(Collections.singletonList(credential));

					// TODO: Not sure yet how to get the ID of the user you just created
					return Environment.executeBlocking(() -> {
						var response = realm.users().create(user);
						if (response.getStatus() >= 400) {
							throw new RuntimeException();
						}
						return response;
					}).map(response -> {
						var parts = response.getLocation().getPath().split("/");
						var id = parts[parts.length - 1];
						user.setId(id);
						return user;
					});
				})
				.compose(userRepresentation -> {
					var users = new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlClient());
					return users.findOneById(userRepresentation.getId());
				})
				.compose(userEntity -> {
					if (userEntity == null) {
						return Future.failedFuture(new ArrayIndexOutOfBoundsException("invalid user creation result"));
					}
					return Future.succeededFuture(userEntity);
				});
	}

	@NotNull
	private static CredentialRepresentation getPasswordCredential(String password) {
		var factory = new Pbkdf2Sha512PasswordHashProviderFactory();
		var credentialModel = factory.create(null).encodedCredential(password, -1);
		var credential = ModelToRepresentation.toRepresentation(credentialModel);
		credential.setTemporary(false);
		return credential;
	}

	private static Keycloak keycloakConstructor(Vertx vertx) {
		var client = (ResteasyClientBuilder) ResteasyClientBuilder.newBuilder();
		/* TODO: Use a regular blocking client for now, until this is updated for 4.0
		if (vertx != null) {
			var engine = new VertxClientHttpEngine(vertx);
			client.httpEngine(engine);
		}*/
		var config = Environment.getConfiguration();

		return KeycloakBuilder.builder()
				.serverUrl(config.getKeycloak().getAuthUrl())
				.realm("master")
				.username(config.getKeycloak().getAdminUsername())
				.password(config.getKeycloak().getAdminPassword())
				.clientId("admin-cli")
				.grantType("password")
				.resteasyClient(client.build())
				.build();
	}

	public static Future<UserResource> disableUser(String userId) {
		return Accounts.realm()
				.compose(realm -> Environment.executeBlocking(() -> {
					var userRepresentation = new UserRepresentation();
					userRepresentation.setEnabled(false);
					realm.users().get(userId).disableCredentialType(Collections.singletonList(CredentialRepresentation.PASSWORD));
					var userResource = realm.users().get(userId);
					userResource.update(userRepresentation);
					return userResource;
				}))
				.onFailure(Environment.onFailure());
	}

	public static Future<RealmResource> createRealmIfAbsent() {
		return Environment.executeBlocking(() -> {
					var configuration = Environment.getConfiguration();
					var keycloak = keycloakReference.get();
					var realmId = configuration.getKeycloak().getRealmId();

					try {
						var realm = keycloak.realm(realmId);
						var exists = realm.toRepresentation();
						if (exists.isEnabled()) {
							return realm;
						}
					} catch (Throwable t) {
						var root = Throwables.getRootCause(t);
						if (!(root instanceof NotFoundException)) {
							throw t;
						}
						LOGGER.trace("because an existing realm was not found, created realm {}", realmId);
					}

					// Create a default
					var realmRepresentation = new RealmRepresentation();
					realmRepresentation.setRealm(realmId);
					realmRepresentation.setDisplayName(configuration.getKeycloak().getRealmDisplayName());
					realmRepresentation.setSslRequired(SslRequired.EXTERNAL.toString());
					realmRepresentation.setEnabled(true);
					// use scrypt provider
					LOGGER.info("""
							realm password policy was NOT set to hashAlgorithm(scrypt), not compatible with ancient passwords.
							set realmRepresentation.setPasswordPolicy("hashAlgorithm(scrypt)") to make compatible""");

					realmRepresentation.setRegistrationAllowed(true);
					realmRepresentation.setResetPasswordAllowed(true);
					realmRepresentation.setResetCredentialsFlow("reset credentials");
					realmRepresentation.setRegistrationFlow("registration");
					realmRepresentation.setRememberMe(true);
					realmRepresentation.setEditUsernameAllowed(true);
					realmRepresentation.setVerifyEmail(false);
					realmRepresentation.setLoginWithEmailAllowed(true);

					keycloak.realms().create(realmRepresentation);

					var realm = keycloak.realms().realm(realmId);
					var flows = realm.flows().getFlows()
							.stream().collect(toMap(AuthenticationFlowRepresentation::getAlias, AuthenticationFlowRepresentation::getId));
					var client = new ClientRepresentation();
					client.setClientId(configuration.getKeycloak().getClientId());
					client.setDirectAccessGrantsEnabled(true);

					// Should now be confidential
					client.setClientAuthenticatorType("client-secret");
					client.setServiceAccountsEnabled(false);
					client.setStandardFlowEnabled(true);
					client.setSecret(configuration.getKeycloak().getClientSecret());
					client.setRedirectUris(List.of("/oauth2callback", "http://localhost:3000/*"));
					client.setAuthenticationFlowBindingOverrides(ImmutableMap.of(
							"direct_grant", flows.get("direct grant"),
							"browser", flows.get("browser")
					));

					client.setWebOrigins(Collections.singletonList("+"));
					realm.clients().create(client);
					return realm;
				})
				.onFailure(Environment.onFailure());
	}

	public static String keycloakAuthUrl() {
		return Environment.getConfiguration().getKeycloak().getAuthUrl();
	}

	public static JWTAuthOptions jwtAuthOptions() {
		var issuer = keycloakAuthUrl();
		var issuerUri = URI.create(issuer);
		var jwksUri = URI.create(String.format("%s://%s:%d%s",
				issuerUri.getScheme(), issuerUri.getHost(), issuerUri.getPort(), issuerUri.getPath() + "realms/hiddenswitch/protocol/openid-connect/certs"));
		var vertx = Vertx.currentContext().owner();
		var webClient = WebClient.create(vertx);
		var response = await(webClient.get(jwksUri.getPort(), jwksUri.getHost(), jwksUri.getPath())
				.as(BodyCodec.jsonObject())
				.send());

		var jwksResponse = response.body();
		var keys = jwksResponse.getJsonArray("keys");

		// Configure JWT validation options
		var jwtOptions = new JWTOptions();
//		jwtOptions.setIssuer(issuer);

		// extract JWKS from keys array
		var jwks = ((List<Object>) keys.getList()).stream()
				.map(o -> new JsonObject((Map<String, Object>) o))
				.collect(Collectors.toList());
		// configure JWTAuth
		var jwtAuthOptions = new JWTAuthOptions();
		jwtAuthOptions.setJwks(jwks);
		jwtAuthOptions.setJWTOptions(jwtOptions);
		jwtAuthOptions.setPermissionsClaimKey("realm_access/roles");

		return jwtAuthOptions;
	}
}
