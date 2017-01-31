package com.hiddenswitch.proto3.net.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.*;
import com.hiddenswitch.proto3.net.amazon.*;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.lambdaworks.crypto.SCryptUtil;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class AccountsImpl extends Service<AccountsImpl> implements Accounts {
	private Pattern usernamePattern = Pattern.compile("[A-Za-z0-9_]+");

	@Override
	public void start() {
		Broker.of(this, Accounts.class, vertx.eventBus());
	}

	@Override
	public void stop() {
		super.stop();
	}

	public CreateAccountResponse createAccount(String emailAddress, String password, String username) {
		CreateAccountRequest request = new CreateAccountRequest();
		request.emailAddress = emailAddress;
		request.password = password;
		request.name = username;
		return this.createAccount(request);
	}

	public LoginResponse login(String username, String password) {
		LoginRequest request = new LoginRequest();
		request.setPassword(password);
		request.setUserId(username);
		return this.login(request);
	}

	@Override
	@Suspendable
	public CreateAccountResponse createAccount(CreateAccountRequest request) {
		CreateAccountResponse response = new CreateAccountResponse();

		if (!isValidName(request.name)) {
			response.invalidName = true;
			return response;
		}

		if (!isValidEmailAddress(request.emailAddress)) {
			response.invalidEmailAddress = true;
			return response;
		}

		if (!isValidPassword(request.password)) {
			response.invalidPassword = true;
			return response;
		}

		Profile profile = create();
		profile.setEmailAddress(request.emailAddress);
		profile.setName(request.name);
		String userId = save(profile);

		LoginToken loginToken = setPassword(userId, request.password);

		response.userId = userId;
		response.loginToken = loginToken;

		return response;
	}

	@Override
	@Suspendable
	public LoginResponse login(LoginRequest request) {
		if (request.getToken() != null
				&& request.getUserId() != null) {
			boolean isAuthorized = isAuthorizedWithToken(request.getUserId(), request.getToken());
			if (isAuthorized) {
				return new LoginResponse(null, getRecord(request.getUserId()));
			} else {
				return new LoginResponse(true, true);
			}
		}
		if (request.getUserId() == null) {
			return new LoginResponse(true, false);
		}
		if (request.getPassword() == null) {
			return new LoginResponse(false, true);
		}
		AuthorizationRecord record = getAuthorizationRecord(request.getUserId());
		if (record == null) {
			return new LoginResponse(true, false);
		}

		if (request.getPassword() != null
				&& !SCryptUtil.check(request.getPassword(), record.getScrypt())) {
			return new LoginResponse(false, true);
		}

		LoginToken token = null;
		token = loginInternal(request, record);
		UserRecord userRecord = getRecord(request.getUserId());

		return new LoginResponse(token, userRecord);
	}

	private LoginToken loginInternal(LoginRequest request, AuthorizationRecord record) {
		LoginToken token = LoginToken.createSecure();
		List<HashedLoginToken> hashedTokens = new ArrayList<>();
		hashedTokens.add(new HashedLoginToken(token));
		hashedTokens.addAll(record.getLoginTokens());
		record.setLoginTokens(hashedTokens);
		record.setUserId(request.getUserId());
		save(record);
		return token;
	}

	public boolean isAuthorizedWithToken(String userId, String token) {
		if (token == null
				|| userId == null) {
			return false;
		}
		AuthorizationRecord record = getAuthorizationRecord(userId);
		if (record == null) {
			return false;
		}
		for (HashedLoginToken loginToken : record.getLoginTokens()) {
			if (SCryptUtil.check(token, loginToken.getHashedLoginToken())) {
				return true;
			}
		}

		return false;
	}

	private String save(Profile profile) {
		UserRecord record = new UserRecord();
		record.setId(profile.getName());
		record.setProfile(profile);
		getDatabase().save(record);
		return record.getId();
	}

	private boolean isValidPassword(String password) {
		return password != null && password.length() >= 1;
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

	private Profile create() {
		return new Profile();
	}

	private LoginToken setPassword(String userId, String password) {
		AuthorizationRecord record = getAuthorizationRecord(userId);

		if (record == null) {
			record = new AuthorizationRecord();
		}

		record.setUserId(userId);
		record.setScrypt(SCryptUtil.scrypt(password, 16384, 8, 1));

		LoginToken token = LoginToken.createSecure();

		// Invalidates all existing login tokens
		record.setLoginTokens(Collections.singletonList(new HashedLoginToken(token)));
		save(record);

		return token;
	}

	private String save(AuthorizationRecord record) {
		getDatabase().save(record);
		return record.getUserId();
	}

	private AuthorizationRecord getAuthorizationRecord(String userId) {
		return getDatabase().load(AuthorizationRecord.class, userId);
	}

	public Profile get(String id) {
		UserRecord record = getRecord(id);
		if (record == null) {
			return null;
		}

		return record.getProfile();
	}

	private UserRecord getRecord(String id) {
		return getDatabase().load(UserRecord.class, id);
	}

	public PlayerProfile getProfileForId(String userId) {
		Profile user = get(userId);
		if (user == null) {
			return null;
		}
		PlayerProfile profile = new PlayerProfile();
		profile.name = user.getName();
		return profile;
	}

	private Pattern getUsernamePattern() {
		return usernamePattern;
	}
}
