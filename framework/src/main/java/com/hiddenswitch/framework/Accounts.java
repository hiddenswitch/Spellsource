package com.hiddenswitch.framework;

import com.avast.grpc.jwt.server.JwtServerInterceptor;
import com.avast.grpc.jwt.server.JwtTokenParser;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.hiddenswitch.framework.impl.WeakVertxMap;
import com.hiddenswitch.framework.rpc.*;
import com.hiddenswitch.framework.schema.keycloak.tables.daos.UserEntityDao;
import com.hiddenswitch.framework.schema.keycloak.tables.pojos.UserEntity;
import com.lambdaworks.crypto.SCryptUtil;
import io.grpc.*;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.shareddata.Shareable;
import io.vertx.ext.web.client.WebClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jetbrains.annotations.NotNull;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.common.VerificationException;
import org.keycloak.common.enums.SslRequired;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.idm.*;

import javax.ws.rs.NotFoundException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class Accounts {
	protected static final String KEYCLOAK_LOGIN_PATH = "/realms/hiddenswitch/protocol/openid-connect/token";
	private static final String RSA = "RSA";
	private static final WeakVertxMap<Keycloak> keycloakReference = new WeakVertxMap<>(Accounts::keycloakConstructor);
	private static final WeakVertxMap<PublicKeyJwtServerInterceptor> interceptor = new WeakVertxMap<>(Accounts::authorizationInterceptorConstructor);

	private interface PublicKeyStorage {
		PublicKey get();

		void set(PublicKey publicKey);
	}

	private static class VertxPublicKeyStorage implements PublicKeyStorage {

		private static class ShareablePublicKey implements Shareable {
			private final PublicKey value;

			public ShareablePublicKey(PublicKey value) {
				this.value = value;
			}

			public PublicKey getValue() {
				return value;
			}
		}

		private final Vertx vertx;

		public VertxPublicKeyStorage(Vertx vertx) {
			this.vertx = vertx;
		}

		@Override
		public PublicKey get() {
			var res = ((ShareablePublicKey) vertx.sharedData().getLocalMap("Accounts:VertxPublicKeyStorage").get("publicKey"));
			return res == null ? null : res.getValue();
		}

		@Override
		public void set(PublicKey publicKey) {
			vertx.sharedData().getLocalMap("Accounts:VertxPublicKeyStorage").put("publicKey", new ShareablePublicKey(publicKey));
		}
	}

	private static class AtomicReferencePublicKeyStorage implements PublicKeyStorage {

		private static final AtomicReference<PublicKey> publicKey = new AtomicReference<>();

		@Override
		public PublicKey get() {
			return publicKey.get();
		}

		@Override
		public void set(PublicKey publicKey) {
			AtomicReferencePublicKeyStorage.publicKey.set(publicKey);
		}
	}

	private static PublicKeyJwtServerInterceptor authorizationInterceptorConstructor(Vertx vertx) {
		var publicKey = vertx == null ? new AtomicReferencePublicKeyStorage() : new VertxPublicKeyStorage(vertx);
		return new PublicKeyJwtServerInterceptor(publicKey, jwtToken -> {
			var pk = publicKey.get();
			return verify(jwtToken, pk);
		});
	}

	@NotNull
	public static CompletableFuture<AccessToken> verify(String jwtToken, PublicKey pk) {
		Objects.requireNonNull(pk, "public key is null");
		var verified = TokenVerifier.create(jwtToken, AccessToken.class);
		verified.publicKey(pk);
		try {
			verified.verify();
			return CompletableFuture.completedFuture(verified.getToken());
		} catch (VerificationException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	private static class PublicKeyJwtServerInterceptor extends JwtServerInterceptor<AccessToken> {
		private PublicKeyStorage publicKey;

		public PublicKeyJwtServerInterceptor(PublicKeyStorage publicKey, JwtTokenParser<AccessToken> tokenParser) {
			super(tokenParser);
			this.publicKey = publicKey;
		}

		public PublicKeyStorage getPublicKey() {
			return publicKey;
		}
	}

	public static Future<ServerInterceptor> authorizationInterceptor() {
		// retrieve public key
		Future<PublicKey> publicKeyFut = getPublicKey();

		return publicKeyFut.compose(publicKey -> {
			var thisInterceptor = interceptor.get();
			return Future.succeededFuture(thisInterceptor);
		});
	}

	private static Future<PublicKey> getPublicKey() {
		var publicKeyStorage = new VertxPublicKeyStorage(Vertx.currentContext().owner());
		Future<PublicKey> publicKeyFut;
		if (publicKeyStorage.get() == null) {
			publicKeyFut = get()
					.compose(realm -> Environment.executeBlocking(() -> {
						var keys = realm.keys().getKeyMetadata().getKeys();
						var keyBase64 = keys.stream().filter(key -> key.getPublicKey() != null
								&& key.getType().equals(RSA)).findFirst().orElseThrow().getPublicKey();
						var keyBytes = Base64.getDecoder().decode(keyBase64);
						var encodedKeySpec = new X509EncodedKeySpec(keyBytes);
						try {
							var factory = KeyFactory.getInstance(RSA);
							return factory.generatePublic(encodedKeySpec);
						} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
							throw new RuntimeException(e);
						}
					}).compose(publicKey -> {
						var thisInterceptor = interceptor.get();
						thisInterceptor.getPublicKey().set(publicKey);
						return Future.succeededFuture(publicKey);
					}))
					.onFailure(Environment.onFailure());
		} else {
			publicKeyFut = Future.succeededFuture(publicKeyStorage.get());
		}
		return publicKeyFut;
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
					return (new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate())).findOneById(accessToken.getSubject());
				}
			}
		}

		// For now we do not support getting the context any other way
		return Future.failedFuture("no context");
	}

	public static String userId() {
		var interceptor = Accounts.interceptor.get();
		if (interceptor != null) {
			var accessTokenContextKey = interceptor.AccessTokenContextKey;
			if (accessTokenContextKey != null) {
				var accessToken = accessTokenContextKey.get();
				if (accessToken == null) {
					return null;
				} else {
					return accessToken.getSubject();
				}
			}
		}
		return null;
	}

	public static Future<BindableService> unauthenticatedService() {
		var context = Vertx.currentContext();
		Objects.requireNonNull(context, "context");

		var webClient = WebClient.create(context.owner());

		return Future.succeededFuture(new VertxUnauthenticatedGrpc.UnauthenticatedVertxImplBase() {

			private LoginOrCreateReply handleAccessTokenUserEntityTuple(Object[] tuple) {
				var accessTokenResponse = (org.keycloak.representations.AccessTokenResponse) tuple[0];
				var userEntity = (UserEntity) tuple[1];
				return LoginOrCreateReply.newBuilder()
						.setUserEntity(toProto(userEntity))
						.setAccessTokenResponse(AccessTokenResponse.newBuilder().setToken(accessTokenResponse.getToken()).build()).build();
			}

			@Override
			public Future<LoginOrCreateReply> createAccount(CreateAccountRequest request) {
				return createUser(request.getEmail(), request.getUsername(), request.getPassword())
						.compose(userEntity -> {
							// Login here
							var client = new Client(context.owner(), webClient);
							return client.login(request.getEmail(), request.getPassword()).map(accessTokenResponse -> new Object[]{accessTokenResponse, userEntity});
						})
						.map(this::handleAccessTokenUserEntityTuple)
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
								var userEntityRes = (new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate()))
										.findOneById(userId);
								return userEntityRes.map(userEntity -> new Object[]{accessTokenResponse, userEntity});
							} catch (VerificationException e) {
								return Future.failedFuture(e);
							}
						})
						.map(this::handleAccessTokenUserEntityTuple)
						.recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<BoolValue> verifyToken(AccessTokenResponse request) {
				var context = Vertx.currentContext();
				return Accounts.getPublicKey()
						.compose(pk -> Future.fromCompletionStage(verify(request.getToken(), pk).minimalCompletionStage(), context))
						.map(BoolValue.of(true))
						.otherwise(BoolValue.of(false))
						.recover(Environment.onGrpcFailure());
			}
		});
	}

	public static Future<ServerServiceDefinition> authenticatedService() {
		// Does not require vertx blocking service because it makes no blocking calls
		return Future.succeededFuture(new VertxAccountsGrpc.AccountsVertxImplBase() {

			@Override
			public Future<LoginOrCreateReply> changePassword(ChangePasswordRequest request) {
				var userId = Accounts.userId();
				// for now we don't invalidate and refresh the old token
				var token = Accounts.token();
				if (token == null) {
					return Future.failedFuture("must log in with old password first");
				}
				return get()
						.compose(realm -> Environment.executeBlocking(() -> {
							realm.users().get(userId).resetPassword(getPasswordCredential(request.getNewPassword()));
							return Future.succeededFuture();
						}))
						.compose(v -> (new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate()).findOneById(userId)))
						.map(userEntity -> LoginOrCreateReply.newBuilder()
								.setUserEntity(toProto(userEntity))
								.setAccessTokenResponse(AccessTokenResponse.newBuilder()
										.setToken(token)
										.build()).build())
						.recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<GetAccountsReply> getAccount(Empty request) {
				return user()
						.compose(thisUser -> {
							if (thisUser == null) {
								return Future.failedFuture("must log in");
							}

							return Future.succeededFuture(GetAccountsReply.newBuilder().addUserEntities(
									com.hiddenswitch.framework.rpc.UserEntity.newBuilder()
											.setUsername(thisUser.getUsername())
											.setEmail(thisUser.getEmail())
											.setId(thisUser.getId())
											.build()
							).build());
						})
						.recover(Environment.onGrpcFailure());
			}

			@Override
			public Future<GetAccountsReply> getAccounts(GetAccountsRequest request) {
				return user()
						.compose(thisUser -> {
							if (thisUser == null) {
								return Future.failedFuture("must log in");
							}

							// TODO: Join with friends
							return (new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate()))
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
						.recover(Environment.onGrpcFailure());
			}
		})
				.compose(Accounts::requiresAuthorization);
	}

	private static com.hiddenswitch.framework.rpc.UserEntity.Builder toProto(UserEntity userEntity) {
		return com.hiddenswitch.framework.rpc.UserEntity.newBuilder()
				.setEmail(userEntity.getEmail())
				.setUsername(userEntity.getUsername())
				.setId(userEntity.getId());
	}

	private static String token() {
		// from JwtServerInterceptor
		return (String) Context.key("AccessToken").get();
	}

	public static Future<ServerServiceDefinition> requiresAuthorization(BindableService service) {
		return Accounts.authorizationInterceptor()
				.compose(interceptor -> Future.succeededFuture(ServerInterceptors.intercept(service, interceptor)));
	}

	public static Future<ServerServiceDefinition> requiresAuthorization(ServerServiceDefinition service) {
		return Accounts.authorizationInterceptor()
				.compose(interceptor -> Future.succeededFuture(ServerInterceptors.intercept(service, interceptor)));
	}

	public static Future<RealmResource> get() {
		return Environment.executeBlocking(() -> {
			var realmId = Environment.cachedConfigurationOrGet().getKeycloak().getRealmId();
			return keycloakReference.get().realm(realmId);
		})
				.onFailure(Environment.onFailure());
	}

	private static AtomicInteger v = new AtomicInteger();

	public static Future<UserEntity> createUser(String email, String username, String password) {
		return get()
				.compose(realm -> {
					var user = new UserRepresentation();
					user.setEmail(email);
					user.setUsername(username);
					user.setEnabled(true);
					var credential = getPasswordCredential(password);
					user.setCredentials(Collections.singletonList(credential));

					// TODO: Not sure yet how to get the ID of the user you just created
					return Environment.executeBlocking(() -> {
						var response = realm.users().create(user);
						if (response.getStatus() >= 400) {
							throw new RuntimeException("A user with the specified e-mail or username already exists.");
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
					var users = new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
					return users.findOneById(userRepresentation.getId());
				})
				.compose(userEntity -> {
					if (userEntity == null) {
						return Future.failedFuture(new ArrayIndexOutOfBoundsException("invalid user creation result"));
					}
					return Future.succeededFuture(userEntity);
				})
				.onFailure(Environment.onFailure());
	}

	public static Future<UserEntity> createUserWithHashed(String email, String username, String scryptHashedPassword) {
		return get()
				.compose(realm -> {
					var credentialModel = PasswordCredentialModel.createFromValues("scrypt", new byte[0], 1, scryptHashedPassword);
					var credential = ModelToRepresentation.toRepresentation(credentialModel);
					credential.setTemporary(false);
					var user = new UserRepresentation();
					user.setEmail(email);
					user.setUsername(username);
					user.setEnabled(true);
					user.setCredentials(Collections.singletonList(credential));
					return Environment.executeBlocking(() -> {
						var response = realm.users().create(user);
						if (response.getStatus() >= 400) {
							throw new RuntimeException("A user with the specified e-mail or username already exists.");
						}
						return response;
					}).map(response -> {
						var parts = response.getLocation().getPath().split("/");
						var id = parts[parts.length - 1];
						user.setId(id);
						return user;
					}).map(UserRepresentation::getId);
				})
				.compose(userId -> {
					var users = new UserEntityDao(Environment.jooqAkaDaoConfiguration(), Environment.sqlPoolAkaDaoDelegate());
					return users.findOneById(userId);
				})
				.compose(res -> res == null ? Future.failedFuture("invalid user ID") : Future.succeededFuture(res))
				.onFailure(Environment.onFailure());
	}

	@NotNull
	private static CredentialRepresentation getPasswordCredential(String password) {
		var credentialModel = PasswordCredentialModel.createFromValues("scrypt", new byte[0], 1, SCryptUtil.scrypt(password, 16384, 8, 1));
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
		var config = Environment.cachedConfigurationOrGet();
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
		return Accounts.get()
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
			var configuration = Environment.cachedConfigurationOrGet();
			var keycloak = keycloakReference.get();
			var existing = Optional.<RealmRepresentation>empty();
			var realmId = configuration.getKeycloak().getRealmId();
			try {
				existing = keycloak.realms().findAll().stream().filter(realm -> realm.getRealm().equals(realmId)).findFirst();
			} catch (NotFoundException ignored) {
			}

			if (existing.isPresent()) {
				return keycloak.realm(realmId);
			}

			// Create a default
			var realmRepresentation = new RealmRepresentation();
			realmRepresentation.setRealm(realmId);
			realmRepresentation.setDisplayName(configuration.getKeycloak().getRealmDisplayName());
			realmRepresentation.setSslRequired(SslRequired.EXTERNAL.toString());
			realmRepresentation.setEnabled(true);
			// use scrypt provider
			realmRepresentation.setPasswordPolicy("hashAlgorithm(scrypt)");

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
			client.setRedirectUris(Collections.singletonList("/oauth2callback"));
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

	public synchronized static String keycloakAuthUrl() {
		return Environment.cachedConfigurationOrGet().getKeycloak().getAuthUrl();
	}
}
