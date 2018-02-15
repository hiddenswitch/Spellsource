package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.*;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.models.LoginResponse;
import com.hiddenswitch.spellsource.util.Mongo;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.Registration;
import com.hiddenswitch.spellsource.impl.util.*;
import com.hiddenswitch.spellsource.models.CreateAccountRequest;
import com.hiddenswitch.spellsource.models.LoginRequest;
import com.lambdaworks.crypto.SCryptUtil;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static com.hiddenswitch.spellsource.util.QuickJson.fromJson;
import static com.hiddenswitch.spellsource.util.QuickJson.json;
import static com.hiddenswitch.spellsource.util.QuickJson.toJson;
import static io.vertx.ext.sync.Sync.awaitResult;

public class AccountsImpl extends AbstractService<AccountsImpl> implements Accounts {
	private Pattern usernamePattern = Pattern.compile("[A-Za-z0-9_]+");
	private Registration registration;

	@Override
	@Suspendable
	public void start() throws SuspendExecution {
		super.start();
		List<String> collections = awaitResult(h -> getMongo().getCollections(h));
		if (!collections.contains(USERS)) {
			Mongo.mongo().createCollection(USERS);
			Mongo.mongo().createIndex(USERS, json(UserRecord.EMAILS_ADDRESS, 1));
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

		Mongo.mongo().insert(USERS, toJson(record));

		response.setUserId(userId);
		response.setLoginToken(forUser);

		return response;
	}

	private String securedPassword(String password) {
		return SCryptUtil.scrypt(password, 16384, 8, 1);
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

	@Suspendable
	public UserRecord getWithEmail(String email) {
		return Mongo.mongo().findOne(USERS, json(UserRecord.EMAILS_ADDRESS, email), UserRecord.class);
	}

	@SuppressWarnings("unchecked")
	public boolean isAuthorizedWithToken(String userId, String secret) throws SuspendExecution, InterruptedException {
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

	public boolean isAuthorizedWithToken(UserRecord record, String secret) {
		return isTokenInList(secret, record.getServices().getResume().getLoginTokens());
	}

	private boolean isTokenInList(String secret, List<HashedLoginTokenRecord> hashedSecrets) {
		for (HashedLoginTokenRecord loginToken : hashedSecrets) {
			if (loginToken.check(secret)) {
				return true;
			}
		}

		return false;
	}


	private boolean isValidPassword(String password) {
		return password != null && password.length() >= 1;
	}

	private boolean emailExists(String emailAddress) throws SuspendExecution, InterruptedException {
		Long count = Mongo.mongo().count(USERS, json(UserRecord.EMAILS_ADDRESS, emailAddress));
		return count != 0;
	}

	private boolean isValidEmailAddress(String emailAddress) {
		return EmailValidator.getInstance().isValid(emailAddress);
	}

	private boolean isValidName(String name) {
		return getUsernamePattern().matcher(name).matches()
				&& !isVulgar(name);
	}

	private boolean isVulgar(String name) {
		return false;
	}

	private Pattern getUsernamePattern() {
		return usernamePattern;
	}

	@Override
	@Suspendable
	public void stop() throws Exception {
		super.stop();
		Rpc.unregister(registration);
	}
}
