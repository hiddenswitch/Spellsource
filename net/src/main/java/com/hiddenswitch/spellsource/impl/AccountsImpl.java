package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.Registration;
import com.hiddenswitch.spellsource.impl.util.*;
import com.lambdaworks.crypto.SCryptUtil;
import io.vertx.core.VertxException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;

import java.util.Collections;
import java.util.List;

import static com.hiddenswitch.spellsource.util.QuickJson.fromJson;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static com.hiddenswitch.spellsource.util.QuickJson.toJson;
import static io.vertx.ext.sync.Sync.awaitResult;

public class AccountsImpl extends AbstractService<AccountsImpl> implements Accounts {
	private Registration registration;

	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		super.start();
		List<String> collections = Mongo.mongo().getCollections();
		if (!collections.contains(USERS)) {
			try {
				Mongo.mongo().createCollection(USERS);
				Mongo.mongo().createIndex(USERS, json(UserRecord.EMAILS_ADDRESS, 1));
			} catch (VertxException ex) {
				logger.error("An error occurred while trying to create the users collection: {}", ex.getMessage());
			}
		}
		registration = Rpc.register(this, Accounts.class, vertx.eventBus());
	}

	public CreateAccountResponse createAccount(String emailAddress, String password, String username) throws SuspendExecution, InterruptedException {
		CreateAccountRequest request = new CreateAccountRequest();
		request.setEmailAddress(emailAddress);
		request.setPassword(password);
		request.setName(username);
		return this.createAccount(request);
	}

	public LoginResponse login(String email, String password) throws SuspendExecution, InterruptedException {
		LoginRequest request = new LoginRequest();
		request.setPassword(password);
		request.setEmail(email);
		return this.login(request);
	}

	@Override
	public CreateAccountResponse createAccount(CreateAccountRequest request) throws SuspendExecution, InterruptedException {
		return Accounts.createAccountInner(request);
	}

	@Override
	@Suspendable
	public LoginResponse login(LoginRequest request) {
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
		MongoClientUpdateResult updateResult = Mongo.mongo().updateCollection(USERS,
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

	@Override
	@Suspendable
	public UserRecord getWithToken(String token) {
		final String[] components = token.split(":");
		final String userId = components[0];
		final String secret = components[1];

		UserRecord record = get(userId);
		if (isAuthorizedWithToken(record, secret)) {
			return record;
		} else {
			return null;
		}
	}

	@Suspendable
	public UserRecord get(String userId) {
		return Mongo.mongo().findOne(USERS, json("_id", userId), UserRecord.class);
	}

	@Override
	@Suspendable
	public ChangePasswordResponse changePassword(ChangePasswordRequest request) throws SuspendExecution, InterruptedException {
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

		MongoClientUpdateResult result = Mongo.mongo().updateCollection(USERS,
				json(MongoRecord.ID, record.getId()),
				json("$set", json(
						UserRecord.SERVICES_PASSWORD_SCRYPT, scrypt
				)));
		logger.debug("changePassword: Changed password for userId={}, username={}", record.getId(), record.getUsername());

		if (result.getDocModified() == 0) {
			throw new IllegalStateException("Unable to save the password change at this time.");
		}

		return new ChangePasswordResponse();
	}

	@Suspendable
	public static UserRecord getWithEmail(String email) {
		return Mongo.mongo().findOne(USERS, json(UserRecord.EMAILS_ADDRESS, email), UserRecord.class);
	}

	@SuppressWarnings("unchecked")
	public static boolean isAuthorizedWithToken(String userId, String secret) throws SuspendExecution, InterruptedException {
		if (secret == null
				|| userId == null) {
			return false;
		}
		JsonObject result = Mongo.mongo().findOne(USERS, json("_id", userId),
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

	public static boolean isAuthorizedWithToken(UserRecord record, String secret) {
		return isTokenInList(secret, record.getServices().getResume().getLoginTokens());
	}

	private static boolean isTokenInList(String secret, List<HashedLoginTokenRecord> hashedSecrets) {
		for (HashedLoginTokenRecord loginToken : hashedSecrets) {
			if (loginToken.check(secret)) {
				return true;
			}
		}

		return false;
	}


	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		Rpc.unregister(registration);
	}
}
