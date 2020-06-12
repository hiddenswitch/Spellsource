package com.hiddenswitch.spellsource.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.Tracing;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.impl.util.*;
import com.hiddenswitch.spellsource.net.models.*;
import com.hiddenswitch.spellsource.net.impl.PasswordResetRecord;
import com.hiddenswitch.spellsource.net.impl.Sync;
import com.lambdaworks.crypto.SCryptUtil;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import io.vertx.ext.mongo.MongoClientDeleteResult;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.*;
import static io.vertx.core.json.JsonObject.mapFrom;
import static io.vertx.ext.sync.Sync.awaitResult;


/**
 * The accounts services. Provides a way for end users to create accounts, get account data, and authenticate users.
 */
public interface Accounts {
	/**
	 * The USERS constant specifies the name of the collection in Mongo that contains the user data.
	 */
	String USERS = "accounts.users";
	/**
	 * This constant is the name of the collection that stores reset tokens for a specific email address.
	 */
	String RESET_TOKENS = "accounts.reset.tokens";
	/**
	 * This pattern specifies what characters make a valid username.
	 */
	Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_]+");
	Logger LOGGER = LoggerFactory.getLogger(Accounts.class);

	/**
	 * Applies a SHA256 hash to the specified text and returns a base64 string (digest) matching this format.
	 *
	 * @param text The text to hash.
	 * @return The "digest", or base64-encoded SHA256 hash.
	 */
	static String hash(String text) {
		byte[] data = text.getBytes(StandardCharsets.UTF_8);
		MessageDigest digester = null;
		try {
			digester = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		digester.update(data);
		return Base64.getEncoder().encodeToString(digester.digest());
	}

	/**
	 * Retrieves the userId from a routing context after it has been handled by an {@link
	 * com.hiddenswitch.spellsource.net.impl.SpellsourceAuthHandler}.
	 *
	 * @param context The routing context from which to retrieve the user ID
	 * @return The User ID
	 */
	static String userId(RoutingContext context) {
		return context.user().principal().getString("_id");
	}

	/**
	 * Creates an account.
	 * <p>
	 * Does <b>not</b> create the standard decks for the user.
	 *
	 * @param request A username, password and e-mail needed to create the account.
	 * @return The result of creating the account. If the field contains bad username, bad e-mail or bad password flags
	 * 		set to true, the account creation failed with the specified handled reason. On subsequent requests from a client
	 * 		that's using the HTTP API, the Login Token should be put into the X-Auth-Token header for subsequent requests.
	 * 		The token and user ID should be saved.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 * @see Logic#initializeUser(InitializeUserRequest) to create standard decks for the user
	 */
	@NotNull
	static CreateAccountResponse createAccount(@NotNull CreateAccountRequest request) throws SuspendExecution, InterruptedException {
		Objects.requireNonNull(request);

		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Accounts/createAccount")
				.withTag("name", request.getName())
				.withTag("emailAddress", request.getEmailAddress())
				.withTag("isBot", request.isBot())
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			CreateAccountResponse response = new CreateAccountResponse();

			if (!isValidName(request.getName())) {
				response.setInvalidName(true);
				return response;
			}

			if (!isValidEmailAddress(request.getEmailAddress())
					|| emailExists(request.getEmailAddress())) {
				response.setInvalidEmailAddress(true);
				span.setTag("invalidEmailAddress", true);
				return response;
			}

			String password = request.getPassword();
			if (!isValidPassword(password)) {
				response.setInvalidPassword(true);
				span.setTag("invalidPassword", true);
				return response;
			}

			String userId = RandomStringUtils.randomAlphanumeric(36).toLowerCase();
			UserRecord record = new UserRecord(userId);
			EmailRecord email = new EmailRecord();
			email.setAddress(request.getEmailAddress());
			record.setEmails(Collections.singletonList(email));
			record.setDecks(new ArrayList<>());
			record.setFriends(new ArrayList<>());
			record.setUsername(request.getName());
			record.setBot(request.isBot());
			record.setCreatedAt(Date.from(Instant.now()));
			record.setPrivacyToken(RandomStringUtils.randomNumeric(4));

			String scrypt = securedPassword(password);
			LoginToken forUser = LoginToken.createSecure(userId);

			HashedLoginTokenRecord loginToken = new HashedLoginTokenRecord(forUser);
			record.setServices(new ServicesRecord());
			ResumeRecord resume = new ResumeRecord();
			resume.setLoginTokens(Collections.singletonList(loginToken));
			record.getServices().setResume(resume);
			PasswordRecord passwordRecord = new PasswordRecord();
			passwordRecord.setScrypt(scrypt);
			record.getServices().setPassword(passwordRecord);

			mongo().insert(USERS, mapFrom(record));

			response.setUserId(userId);
			response.setLoginToken(forUser);
			response.setRecord(record);

			return response;
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}
	}

	/**
	 * Creates a secure (non-reversible) reprsentation of a password.
	 *
	 * @param password The user password, typically in plaintext.
	 * @return The SCrypted password.
	 */
	static String securedPassword(String password) {
		Span span = GlobalTracer.get().buildSpan("Accounts/securedPassword")
				.withTag("length", password.length())
				.start();
		try {
			return SCryptUtil.scrypt(password, 16384, 8, 1);
		} finally {
			span.finish();
		}
	}

	/**
	 * Validates that a password is not null and at least of length 1.
	 *
	 * @param password The password, in plaintext, to check.
	 * @return {@code true} if the password is not {@code null} and its length is at least 4.
	 */
	static boolean isValidPassword(String password) {
		return password != null && password.length() >= 4;
	}

	/**
	 * Checks if an email already exists.
	 *
	 * @param emailAddress The address ot check.
	 * @return {@code true} if the email exists in the database.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static boolean emailExists(String emailAddress) throws SuspendExecution, InterruptedException {
		Long count = mongo().count(USERS, json(UserRecord.EMAILS_ADDRESS, emailAddress));
		return count != 0;
	}

	/**
	 * Uses the Apache {@link EmailValidator} to determine if an email is valid.
	 *
	 * @param emailAddress The address to check.
	 * @return {@code true} if the address is valid.
	 */
	static boolean isValidEmailAddress(String emailAddress) {
		return EmailValidator.getInstance().isValid(emailAddress);
	}

	/**
	 * Checks that a username is valid.
	 *
	 * @param name The username to check.
	 * @return {@code true} if it's nonnull, nonempty, cnotains valid characters and is not vulgar.
	 */
	static boolean isValidName(String name) {
		return name != null && name.length() >= 1 && getUsernamePattern().matcher(name).matches()
				&& !isVulgar(name);
	}

	/**
	 * Checks if a username is vulgar.
	 *
	 * @param name The username to check
	 * @return {@code true} if the username is vulgar.
	 */
	static boolean isVulgar(String name) {
		return false;
	}

	static Pattern getUsernamePattern() {
		return USERNAME_PATTERN;
	}

	/**
	 * Creates an account.
	 *
	 * @param emailAddress
	 * @param password
	 * @param username
	 * @return
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static CreateAccountResponse createAccount(String emailAddress, String password, String username) throws SuspendExecution, InterruptedException {
		CreateAccountRequest request = new CreateAccountRequest();
		request.setEmailAddress(emailAddress);
		request.setPassword(password);
		request.setName(username);
		return Accounts.createAccount(request);
	}

	/**
	 * Gets a user's database record using their email address.
	 *
	 * @param email
	 * @return
	 */
	@Suspendable
	static UserRecord getWithEmail(String email) {
		return mongo().findOne(USERS, json(UserRecord.EMAILS_ADDRESS, email), UserRecord.class);
	}

	/**
	 * Login with the specified email and password, returning a secret token.
	 *
	 * @param email
	 * @param password
	 * @return
	 */
	@Suspendable
	static LoginResponse login(String email, String password) {
		LoginRequest request = new LoginRequest();
		request.setPassword(password);
		request.setEmail(email);
		return login(request);
	}

	/**
	 * Determines if the specified secret authorizes the caller for the specified user ID.
	 *
	 * @param userId
	 * @param secret
	 * @return
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@SuppressWarnings("unchecked")
	static boolean isAuthorizedWithToken(String userId, String secret) throws SuspendExecution, InterruptedException {
		if (secret == null
				|| userId == null) {
			return false;
		}
		JsonObject result = mongo().findOne(USERS, json("_id", userId),
				json(UserRecord.SERVICES_RESUME_LOGIN_TOKENS, 1));

		if (result == null
				|| result.isEmpty()) {
			return false;
		}

		List<HashedLoginTokenRecord> tokens = fromJson(
				result.getJsonObject(UserRecord.SERVICES).getJsonObject(UserRecord.RESUME).getJsonArray(UserRecord.LOGIN_TOKENS),
				HashedLoginTokenRecord.class);

		return isTokenInList(secret, tokens);
	}

	/**
	 * Determines if the specified secret appears in the user's token list in their user record fetched from the
	 * database.
	 *
	 * @param record
	 * @param secret
	 * @return
	 */
	static boolean isAuthorizedWithToken(UserRecord record, String secret) {
		if (record == null || record.getServices() == null || record.getServices().getResume() == null
				|| record.getServices().getResume().getLoginTokens() == null
				|| record.getServices().getResume().getLoginTokens().size() == 0) {
			return false;
		}
		return isTokenInList(secret, record.getServices().getResume().getLoginTokens());
	}

	/**
	 * Determines whether the specified token is in the list of hashed tokens.
	 *
	 * @param secret
	 * @param hashedSecrets
	 * @return
	 */
	static boolean isTokenInList(String secret, List<HashedLoginTokenRecord> hashedSecrets) {
		for (HashedLoginTokenRecord loginToken : hashedSecrets) {
			if (loginToken.check(secret)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Login with the provided email and password to receive a login token. Pass the login token in the X-Auth-Token
	 * header for subsequent requests in the HTTP API. Save the user ID.
	 *
	 * @param request An email and password combination.
	 * @return The result of logging in, or information about why the login failed if it was for a handled reason (bad
	 * 		email or password).
	 */
	@Suspendable
	static LoginResponse login(LoginRequest request) {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Accounts/login")
				.withTag("email", request.getEmail())
				.start();
		Scope scope = tracer.activateSpan(span);
		try {
			if (request.getEmail() == null) {
				span.setTag("badEmail", true);
				return new LoginResponse(true, false);
			}
			if (request.getPassword() == null) {
				span.setTag("badPassword", true);
				return new LoginResponse(false, true);
			}

			String email = request.getEmail();
			UserRecord userRecord = getWithEmail(email);

			if (userRecord == null) {
				span.setTag("badEmail", true);
				return new LoginResponse(true, false);
			}

			if (request.getPassword() != null) {
				boolean check = false;
				try {
					check = SCryptUtil.check(request.getPassword(), userRecord.getServices().getPassword().getScrypt());
				} catch (IllegalArgumentException notHashed) {
					LOGGER.error("Not hashed correctly for user {}", userRecord.getUsername(), notHashed);
					Tracing.error(notHashed, span, false);
				} finally {
					if (!check) {
						span.setTag("badPassword", true);
						return new LoginResponse(false, true);
					}
				}
			}

			// Since we don't store the tokens unhashed, we have to add this token always. We slice down five tokens.
			LoginToken token = LoginToken.createSecure(userRecord.getId());
			HashedLoginTokenRecord hashedLoginTokenRecord = new HashedLoginTokenRecord(token);
			int sliceLastFiveElements = -5;

			// Update the record with the new token.
			MongoClientUpdateResult updateResult = mongo().updateCollection(USERS,
					json("_id", userRecord.getId()),
					json("$push",
							json(UserRecord.SERVICES_RESUME_LOGIN_TOKENS,
									json("$each",
											Collections.singletonList(mapFrom(hashedLoginTokenRecord)),
											"$slice",
											sliceLastFiveElements))));

			if (updateResult.getDocModified() == 0) {
				throw new RuntimeException("login failed");
			}

			span.setTag("userId", userRecord.getId());
			return new LoginResponse(token, userRecord);
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}
	}

	/**
	 * Gets a user given a token of the form userId:secret.
	 *
	 * @param token
	 * @return
	 */
	@Suspendable
	static UserRecord getWithToken(String token) {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Accounts/getWithToken").start();
		Scope scope = tracer.activateSpan(span);
		try {
			String[] components = token.split(":");
			String userId = components[0];
			String secret = components[1];

			UserRecord record = Accounts.get(userId);
			if (Accounts.isAuthorizedWithToken(record, secret)) {
				span.setTag("userId", userId);
				return record;
			} else {
				return null;
			}
		} finally {
			span.finish();
			scope.close();
		}
	}

	/**
	 * Gets a user by the specified user ID, or {@code null} if one was not found/
	 *
	 * @param userId The user's ID.
	 * @return The {@link UserRecord} or {@code null} if none was found for this ID.
	 */
	@Suspendable
	@Nullable
	static UserRecord get(String userId) {
		return mongo().findOne(USERS, json("_id", userId), UserRecord.class);
	}

	/**
	 * Changes the specified user's password. This does not consume an authorization token.
	 *
	 * @param request
	 * @return
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	static ChangePasswordResponse changePassword(ChangePasswordRequest request) throws SuspendExecution, InterruptedException {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Accounts/changePassword")
				.withTag("userId", request.getUserId().toString())
				.start();
		Scope scope = tracer.activateSpan(span);

		try {
			if (request.getUserId() == null) {
				throw new NullPointerException("No user specified.");
			}

			UserRecord record = get(request.getUserId().toString());
			if (record == null) {
				throw new NullPointerException("User not found.");
			}

			if (!Accounts.isValidPassword(request.getPassword())) {
				throw new SecurityException("Invalid password.");
			}

			String scrypt = Accounts.securedPassword(request.getPassword());

			// Set new password hash, clear all login tokens
			MongoClientUpdateResult result = mongo().updateCollection(USERS,
					json(MongoRecord.ID, record.getId()),
					json("$set", json(
							UserRecord.SERVICES_PASSWORD_SCRYPT, scrypt,
							UserRecord.SERVICES_RESUME_LOGIN_TOKENS, array()
					)));
			LOGGER.debug("changePassword: Changed password for userId={}, username={}", record.getId(), record.getUsername());

			if (result.getDocModified() == 0) {
				throw new IllegalStateException("Unable to save the password change at this time.");
			}

			return new ChangePasswordResponse();
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}

	}

	/**
	 * Removes the specified user account by its ID, removing inventory and collection entries too
	 *
	 * @param id
	 * @return
	 * @throws SuspendExecution
	 */
	static boolean removeAccount(@NotNull UserId id) throws SuspendExecution {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Accounts/removeAccount")
				.withTag("userId", id.toString())
				.start();
		Scope scope = tracer.activateSpan(span);

		try {
			UserRecord record = get(id.toString());
			if (record == null) {
				return false;
			}

			// Remove all collections
			mongo().removeDocuments(Inventory.COLLECTIONS, json("userId", record.getId()));
			// Remove all inventory records
			mongo().removeDocuments(Inventory.INVENTORY, json("userId", record.getId()));
			// Remove the user document
			mongo().removeDocument(Accounts.USERS, json("_id", record.getId()));
			return true;
		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}
	}

	/**
	 * Removes the specified user accounts en-masse using bulk writes.
	 *
	 * @param ids
	 * @return
	 * @throws SuspendExecution
	 */
	static long removeAccounts(List<UserId> ids) throws SuspendExecution {
		Tracer tracer = GlobalTracer.get();
		Span span = tracer.buildSpan("Accounts/removeAccounts")
				.withTag("userIds", ids.toString())
				.start();
		Scope scope = tracer.activateSpan(span);

		try {
			JsonArray userIds = new JsonArray(ids.stream().map(UserId::toString).collect(Collectors.toList()));
			// Remove all collections
			mongo().removeDocuments(Inventory.COLLECTIONS, json("userId", json("$in", userIds)));
			// Remove all inventory records
			mongo().removeDocuments(Inventory.INVENTORY, json("userId", json("$in", userIds)));
			// Remove the user document
			MongoClientDeleteResult result = mongo().removeDocuments(Accounts.USERS, json("_id", json("$in", userIds)));

			// TODO: Remove friend entries for deleted accounts
			return result.getRemovedCount();

		} catch (RuntimeException runtimeException) {
			Tracing.error(runtimeException, span, true);
			throw runtimeException;
		} finally {
			span.finish();
			scope.close();
		}

	}

	/**
	 * Configures handlers for password resetting (web URLs)
	 */
	static void passwordReset(Router router) {
		String requestUrl = "/reset/passwords/request";
		String resetUrl = "/reset/passwords/with-token";

		StaticHandler staticHandler = StaticHandler.create("webroot/reset/passwords");
		BodyHandler bodyHandler = BodyHandler.create();

		router
				.route("/reset/passwords/*")
				.handler(staticHandler);

		router.route(requestUrl)
				.method(HttpMethod.GET)
				.handler(routingContext -> {
					routingContext.response().setStatusCode(303);
					routingContext.response().putHeader("Location", "passwordresetrequest.html");
					routingContext.response().end();
				});

		router.route(resetUrl)
				.method(HttpMethod.GET)
				.handler(Sync.fiber(routingContext -> {
					routingContext.response().setStatusCode(303);

					if (routingContext.queryParam("token").size() != 1) {
						routingContext.response().putHeader("Location", "passwordresetexpired.html");
						routingContext.response().end();
						return;
					}
					String token = routingContext.queryParam("token").get(0);
					PasswordResetRecord passwordResetRecord = mongo().findOne(RESET_TOKENS, json("_id", token), PasswordResetRecord.class);
					if (System.currentTimeMillis() > passwordResetRecord.getExpiresAt()) {
						routingContext.response().putHeader("Location", "passwordresetexpired.html");
						routingContext.response().end();
						return;
					}

					routingContext.addCookie(io.vertx.core.http.Cookie.cookie("token", token));
					routingContext.response().putHeader("Location", "passwordreset.html");
					routingContext.response().end();
				}));

		router.route(resetUrl)
				.method(HttpMethod.POST)
				.handler(bodyHandler);

		router.route(resetUrl)
				.method(HttpMethod.POST)
				.handler(HandlerFactory.returnUnhandledExceptions(routingContext -> {
					routingContext.response().setStatusCode(303);

					String password1 = routingContext.request().getFormAttribute("password1");
					String password2 = routingContext.request().getFormAttribute("password2");

					if (!Objects.equals(password1, password2) || !Accounts.isValidPassword(password1)) {
						routingContext.response().putHeader("Location", "passwordsdidnotmatch.html");
						routingContext.response().end();
						return;
					}

					io.vertx.core.http.Cookie cookie = routingContext.getCookie("token");
					String token;
					if (cookie == null) {
						token = routingContext.queryParams().get("token");
					} else {
						token = cookie.getValue();
					}
					if (token == null) {
						routingContext.response().putHeader("Location", "passwordresetexpired.html");
						routingContext.response().end();
						return;
					}

					PasswordResetRecord passwordResetRecord = mongo().findOne(RESET_TOKENS, json("_id", token), PasswordResetRecord.class);
					if (passwordResetRecord == null || System.currentTimeMillis() > passwordResetRecord.getExpiresAt()) {
						routingContext.response().putHeader("Location", "passwordresetexpired.html");
						routingContext.response().end();
						return;
					}

					try {
						Accounts.changePassword(ChangePasswordRequest.request(new UserId(passwordResetRecord.getUserId()), password1));
						mongo().removeDocument(RESET_TOKENS, json("_id", token));
						routingContext.response().putHeader("Location", "passwordresetted.html");
					} catch (RuntimeException any) {
						routingContext.response().putHeader("Location", "passwordresetexpired.html");
					} finally {
						routingContext.removeCookie("token");
						routingContext.response().end();
					}
				}));

		router.route(requestUrl)
				.method(HttpMethod.POST)
				.handler(bodyHandler);

		router.route(requestUrl)
				.method(HttpMethod.POST)
				.handler(Sync.fiber(routingContext -> {
					routingContext.response().setStatusCode(303);
					String email = routingContext.request().getFormAttribute("email");
					boolean isValid = EmailValidator.getInstance().isValid(email);

					if (isValid) {
						UserRecord userRecord = mongo().findOne(USERS, json(UserRecord.EMAILS_ADDRESS, email), UserRecord.class);

						// End the request first to prevent the timing of the function from leaking whether or not an account exists.
						routingContext.response().putHeader("Location", "passwordresetrequestsent.html");
						routingContext.response().end();

						if (userRecord != null) {
							String userId = userRecord.getId();
							CreateResetTokenResponse createResetTokenResult = createResetToken(userId);
							String token = createResetTokenResult.getToken();
							String id = createResetTokenResult.getId();

							MailClient mailClient = MailClient.createShared(Vertx.currentContext().owner(),
									new MailConfig()
											.setHostname(System.getenv().getOrDefault("SMTP_HOST", "smtp.mailgun.org"))
											.setUsername(System.getenv().getOrDefault("SMTP_USERNAME", "no-reply@hiddenswitch.com"))
											.setPassword(System.getenv().getOrDefault("SMTP_PASSWORD", "password")));
							boolean sent = false;
							try {
								String emailUrl = com.hiddenswitch.spellsource.client.Configuration.getDefaultApiClient().getBasePath() + resetUrl + "?token=" + token;
								MailResult mailResult = awaitResult(h -> mailClient.sendMail(new MailMessage()
										.setFrom("no-reply@hiddenswitch.com")
										.setTo(email)
										.setSubject("Your Password Reset Request from Spellsource")
										.setHtml(
												String.format("Please visit this URL to reset your password for Spellsource: <br /> <a href=\"%s\">%s</a>", emailUrl, emailUrl)), h));
								if (mailResult.getMessageID() != null) {
									sent = true;
								}
							} finally {
								mailClient.close();
								if (!sent) {
									mongo().removeDocument(RESET_TOKENS, json("_id", id));
								}
							}
						}
					} else {
						routingContext.response().putHeader("Location", "passwordresetrequestinvalid.html");
						routingContext.response().end();
					}
				}));
	}

	@Suspendable
	static CreateResetTokenResponse createResetToken(String userId) {
		String token = RandomStringUtils.randomAlphanumeric(64).toLowerCase();
		PasswordResetRecord record = new PasswordResetRecord(token);
		record.setUserId(userId);
		String id = mongo().insert(RESET_TOKENS, mapFrom(record));
		return new CreateResetTokenResponse().setToken(token).setId(id);
	}
}
