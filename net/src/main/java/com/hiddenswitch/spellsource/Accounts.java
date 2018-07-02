package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.*;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.QuickJson;
import com.lambdaworks.crypto.SCryptUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.*;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.Mongo.mongo;
import static com.hiddenswitch.spellsource.util.QuickJson.*;
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
	 * This pattern specifies what characters make a valid username.
	 */
	Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9_]+");
	Logger LOGGER = LoggerFactory.getLogger(Accounts.class);

	/**
	 * Updates an account. Useful for joining data into the account object, like deck or statistics information.
	 *
	 * @param client        A database client
	 * @param userId        The user's ID
	 * @param updateCommand A JSON object that corresponds to a Mongo update command.
	 * @return The mongo client update result
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static MongoClientUpdateResult update(MongoClient client, String userId, JsonObject updateCommand) throws SuspendExecution, InterruptedException {
		return awaitResult(h -> client.updateCollection(Accounts.USERS, json("_id", userId), updateCommand, h));
	}

	/**
	 * Updates an account. Useful for joining data into the account object, like deck or statistics information.
	 *
	 * @param userId        The user's ID
	 * @param updateCommand A JSON object that corresponds to a Mongo update command.
	 * @return The mongo client update result
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static MongoClientUpdateResult update(String userId, JsonObject updateCommand) throws SuspendExecution, InterruptedException {
		return mongo().updateCollection(Accounts.USERS, json("_id", userId), updateCommand);
	}

	/**
	 * Update multiple accounts. Useful for joining data into the account object, like deck or statistics information.
	 *
	 * @param query         A JSON object corresponding to a query on the user's collection.
	 * @param updateCommand A JSON object that corresponds to a Mongo update command.
	 * @return The mongo client update result
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static MongoClientUpdateResult update(JsonObject query, JsonObject updateCommand) throws SuspendExecution {
		return mongo().updateCollectionWithOptions(Accounts.USERS, query, updateCommand, new UpdateOptions().setMulti(true));
	}

	/**
	 * Finds a user account with the given options.
	 *
	 * @param client  A database client
	 * @param query   The user's ID
	 * @param options Mongo FindOptions
	 * @return Documents with the specified fields.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static List<JsonObject> find(MongoClient client, JsonObject query, FindOptions options) throws SuspendExecution, InterruptedException {
		return awaitResult(h -> client.findWithOptions(Accounts.USERS, query, options, h));
	}

	/**
	 * Finds a user account with the given user ID.
	 *
	 * @param userId The ID of the user to find.
	 * @return The {@link UserRecord}, or {@code null} if no user record exists.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static UserRecord findOne(String userId) throws SuspendExecution, InterruptedException {
		return mongo().findOne(Accounts.USERS, json("_id", userId), UserRecord.class);
	}

	static UserRecord findOne(UserId userId) throws SuspendExecution, InterruptedException {
		return findOne(userId.toString());
	}

	/**
	 * Finds user accounts with the given options.
	 *
	 * @param client A database client
	 * @param query  The user's ID
	 * @return User records with all the fields. Careful with these documents, since they contain password hashes.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	static List<UserRecord> find(MongoClient client, JsonObject query) throws SuspendExecution, InterruptedException {
		List<JsonObject> records = awaitResult(h -> client.find(Accounts.USERS, query, h));
		return QuickJson.fromJson(records, UserRecord.class);
	}

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
	 * com.hiddenswitch.spellsource.impl.SpellsourceAuthHandler}.
	 *
	 * @param context The routing context from which to retrieve the user ID
	 * @return The User ID
	 */
	static String userId(RoutingContext context) {
		return context.user().principal().getString("_id");
	}

	/**
	 * Creates an account.
	 *
	 * @param request A username, password and e-mail needed to create the account.
	 * @return The result of creating the account. If the field contains bad username, bad e-mail or bad password flags
	 * set to true, the account creation failed with the specified handled reason. On subsequent requests from a client
	 * that's using the HTTP API, the Login Token should be put into the X-Auth-Token header for subsequent requests. The
	 * token and user ID should be saved.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@NotNull
	static CreateAccountResponse createAccount(CreateAccountRequest request) throws SuspendExecution, InterruptedException {
		CreateAccountResponse response = new CreateAccountResponse();

		if (!isValidName(request.getName())) {
			response.setInvalidName(true);
			return response;
		}

		if (!isValidEmailAddress(request.getEmailAddress())
				|| emailExists(request.getEmailAddress())) {
			response.setInvalidEmailAddress(true);
			return response;
		}

		final String password = request.getPassword();
		if (!isValidPassword(password)) {
			response.setInvalidPassword(true);
			return response;
		}

		final String userId = RandomStringUtils.randomAlphanumeric(36).toLowerCase();
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

		final String scrypt = securedPassword(password);
		LoginToken forUser = LoginToken.createSecure(userId);

		HashedLoginTokenRecord loginToken = new HashedLoginTokenRecord(forUser);
		record.setServices(new ServicesRecord());
		ResumeRecord resume = new ResumeRecord();
		resume.setLoginTokens(Collections.singletonList(loginToken));
		record.getServices().setResume(resume);
		PasswordRecord passwordRecord = new PasswordRecord();
		passwordRecord.setScrypt(scrypt);
		record.getServices().setPassword(passwordRecord);

		mongo().insert(USERS, toJson(record));

		response.setUserId(userId);
		response.setLoginToken(forUser);
		response.setRecord(record);

		return response;
	}

	/**
	 * Creates a secure (non-reversible) reprsentation of a password.
	 *
	 * @param password The user password, typically in plaintext.
	 * @return The SCrypted password.
	 */
	static String securedPassword(String password) {
		return SCryptUtil.scrypt(password, 16384, 8, 1);
	}

	/**
	 * Validates that a password is not null and at least of length 1.
	 * @param password The password, in plaintext, to check.
	 * @return {@code true} if the password is not {@code null} and its length is at least 1.
	 */
	static boolean isValidPassword(String password) {
		return password != null && password.length() >= 1;
	}

	/**
	 * Checks if an email already exists.
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
	 * @param emailAddress The address to check.
	 * @return {@code true} if the address is valid.
	 */
	static boolean isValidEmailAddress(String emailAddress) {
		return EmailValidator.getInstance().isValid(emailAddress);
	}

	/**
	 * Checks that a username is valid.
	 * @param name The username to check.
	 * @return {@code true} if it's nonnull, nonempty, cnotains valid characters and is not vulgar.
	 */
	static boolean isValidName(String name) {
		return name != null && name.length() >= 1 && getUsernamePattern().matcher(name).matches()
				&& !isVulgar(name);
	}

	/**
	 * Checks if a username is vulgar.
	 * @param name The username to check
	 * @return {@code true} if the username is vulgar.
	 */
	static boolean isVulgar(String name) {
		return false;
	}

	static Pattern getUsernamePattern() {
		return USERNAME_PATTERN;
	}

	static CreateAccountResponse createAccount(String emailAddress, String password, String username) throws SuspendExecution, InterruptedException {
		CreateAccountRequest request = new CreateAccountRequest();
		request.setEmailAddress(emailAddress);
		request.setPassword(password);
		request.setName(username);
		return Accounts.createAccount(request);
	}

	@Suspendable
	static UserRecord getWithEmail(String email) {
		return mongo().findOne(USERS, json(UserRecord.EMAILS_ADDRESS, email), UserRecord.class);
	}

	@Suspendable
	static LoginResponse login(String email, String password) {
		LoginRequest request = new LoginRequest();
		request.setPassword(password);
		request.setEmail(email);
		return login(request);
	}

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

	static boolean isAuthorizedWithToken(UserRecord record, String secret) {
		if (record == null || record.getServices() == null || record.getServices().getResume() == null
				|| record.getServices().getResume().getLoginTokens() == null
				|| record.getServices().getResume().getLoginTokens().size() == 0) {
			return false;
		}
		return isTokenInList(secret, record.getServices().getResume().getLoginTokens());
	}

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
	 * email or password).
	 */
	@Suspendable
	static LoginResponse login(LoginRequest request) {
		if (request.getEmail() == null) {
			return new LoginResponse(true, false);
		}
		if (request.getPassword() == null) {
			return new LoginResponse(false, true);
		}

		final String email = request.getEmail();
		UserRecord userRecord = getWithEmail(email);

		if (userRecord == null) {
			return new LoginResponse(true, false);
		}

		if (request.getPassword() != null
				&& !SCryptUtil.check(request.getPassword(), userRecord.getServices().getPassword().getScrypt())) {
			return new LoginResponse(false, true);
		}

		// Since we don't store the tokens unhashed, we have to add this token always. We slice down five tokens.
		LoginToken token = LoginToken.createSecure(userRecord.getId());
		HashedLoginTokenRecord hashedLoginTokenRecord = new HashedLoginTokenRecord(token);
		final int sliceLastFiveElements = -5;

		// Update the record with the new token.
		MongoClientUpdateResult updateResult = mongo().updateCollection(USERS,
				json("_id", userRecord.getId()),
				json("$push",
						json(UserRecord.SERVICES_RESUME_LOGIN_TOKENS,
								json("$each",
										Collections.singletonList(toJson(hashedLoginTokenRecord)),
										"$slice",
										sliceLastFiveElements))));

		if (updateResult.getDocModified() == 0) {
			throw new RuntimeException();
		}

		return new LoginResponse(token, userRecord);
	}

	@Suspendable
	static UserRecord getWithToken(String token) {
		final String[] components = token.split(":");
		final String userId = components[0];
		final String secret = components[1];

		UserRecord record = Accounts.get(userId);
		if (Accounts.isAuthorizedWithToken(record, secret)) {
			return record;
		} else {
			return null;
		}
	}

	@Suspendable
	static UserRecord get(String userId) {
		return mongo().findOne(USERS, json("_id", userId), UserRecord.class);
	}

	@Suspendable
	static UserRecord get(UserId userId) {
		return get(userId.toString());
	}

	@Suspendable
	static ChangePasswordResponse changePassword(ChangePasswordRequest request) throws SuspendExecution, InterruptedException {
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

		final String scrypt = Accounts.securedPassword(request.getPassword());

		MongoClientUpdateResult result = mongo().updateCollection(USERS,
				json(MongoRecord.ID, record.getId()),
				json("$set", json(
						UserRecord.SERVICES_PASSWORD_SCRYPT, scrypt
				)));
		LOGGER.debug("changePassword: Changed password for userId={}, username={}", record.getId(), record.getUsername());

		if (result.getDocModified() == 0) {
			throw new IllegalStateException("Unable to save the password change at this time.");
		}

		return new ChangePasswordResponse();
	}

	static boolean removeAccount(UserId id) throws SuspendExecution {
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
	}

	static long removeAccounts(List<UserId> ids) throws SuspendExecution {
		JsonArray userIds = new JsonArray(ids.stream().map(UserId::toString).collect(Collectors.toList()));
		// Remove all collections
		mongo().removeDocuments(Inventory.COLLECTIONS, json("userId", json("$in", userIds)));
		// Remove all inventory records
		mongo().removeDocuments(Inventory.INVENTORY, json("userId", json("$in", userIds)));
		// Remove the user document
		MongoClientDeleteResult result = mongo().removeDocuments(Accounts.USERS, json("_id", json("$in", userIds)));
		return result.getRemovedCount();
	}
}
