package com.hiddenswitch.proto3.net;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.impl.util.UserRecord;
import com.hiddenswitch.proto3.net.models.CreateAccountRequest;
import com.hiddenswitch.proto3.net.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.models.LoginRequest;
import com.hiddenswitch.proto3.net.models.LoginResponse;
import com.hiddenswitch.proto3.net.util.Mongo;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;

import java.util.List;

import static com.hiddenswitch.proto3.net.util.QuickJson.fromJson;
import static com.hiddenswitch.proto3.net.util.QuickJson.json;
import static io.vertx.ext.sync.Sync.awaitResult;


/**
 * The Accounts service. Provides a way for end users to create accounts, get account data, and authenticate users.
 */
public interface Accounts {
	/**
	 * The USERS constant specifies the name of the collection in Mongo that contains the user data.
	 */
	String USERS = "accounts.users";

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
		return Mongo.mongo().updateCollection(Accounts.USERS, json("_id", userId), updateCommand);
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
	static MongoClientUpdateResult update(JsonObject query, JsonObject updateCommand) throws SuspendExecution, InterruptedException {
		return Mongo.mongo().updateCollectionWithOptions(Accounts.USERS, query, updateCommand, new UpdateOptions().setMulti(true));
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
		return Mongo.mongo().findOne(Accounts.USERS, json("_id", userId), UserRecord.class);
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
		return fromJson(records, UserRecord.class);
	}

	/**
	 * Creates an account.
	 *
	 * @param request A username, password and e-mail needed to create the account.
	 * @return The result of creating the account. If the field contains bad username, bad e-mail or bad password flags
	 * set to true, the account creation failed with the specified handled reason. On subsequent requests from a client
	 * that's using the HTTP API, the Login Token should be put into the X-Auth-Token header for subsequent requests.
	 * The token and user ID should be saved.
	 * @throws SuspendExecution
	 * @throws InterruptedException
	 */
	@Suspendable
	CreateAccountResponse createAccount(CreateAccountRequest request) throws SuspendExecution, InterruptedException;

	/**
	 * Login with the provided email and password to receive a login token. Pass the login token in the X-Auth-Token
	 * header for subsequent requests in the HTTP API. Save the user ID.
	 *
	 * @param request An email and password combination.
	 * @return The result of logging in, or information about why the login failed if it was for a handled reason (bad
	 * email or password).
	 */
	@Suspendable
	LoginResponse login(LoginRequest request);

	/**
	 * Gets a user record for the given token.
	 *
	 * @param token The token that was returned by a login or create account request.
	 * @return The user record.
	 */
	@Suspendable
	UserRecord getWithToken(String token);

	/**
	 * Gets a user record for the given user ID.
	 *
	 * @param userId The user's ID as returned by a login or create account request.
	 * @return The user record.
	 */
	@Suspendable
	UserRecord get(String userId);
}
