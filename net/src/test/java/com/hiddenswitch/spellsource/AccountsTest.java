package com.hiddenswitch.spellsource;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.UserId;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.Sync;
import com.hiddenswitch.spellsource.util.UnityClient;
import io.vertx.ext.unit.TestContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;

import static io.vertx.ext.sync.Sync.awaitResult;
import static net.demilich.metastone.tests.util.TestBase.assertThrows;
import static org.junit.Assert.*;

public class AccountsTest extends SpellsourceTestBase {

	@Test
	public void testChangePassword(TestContext context) {
		sync(() -> {
			CreateAccountResponse account = createRandomAccount();
			assertTrue(Accounts.isAuthorizedWithToken(account.getUserId(), account.getLoginToken().getSecret()));
			ChangePasswordResponse res = Accounts.changePassword(ChangePasswordRequest.request(new UserId(account.getUserId()), "io/vertx/test"));
			assertFalse("should log out user", Accounts.isAuthorizedWithToken(account.getUserId(), account.getLoginToken().getSecret()));
		}, context);
	}

	@Test
	public void testChangePasswordRejectsInvalidPassword(TestContext context) {
		sync(() -> {
			CreateAccountResponse account = createRandomAccount();
			assertTrue(Accounts.isAuthorizedWithToken(account.getUserId(), account.getLoginToken().getSecret()));
			assertThrows(SecurityException.class, () -> {
				ChangePasswordResponse res = Accounts.changePassword(ChangePasswordRequest.request(new UserId(account.getUserId()), "*"));
			});
			assertTrue("should not log out user", Accounts.isAuthorizedWithToken(account.getUserId(), account.getLoginToken().getSecret()));
		}, context);
	}

	@Test
	public void testCreateAccount(TestContext context) throws Exception {
		sync(() -> {
			CreateAccountResponse response = Accounts.createAccount(getEmailAddress(), "destructoid", getUsername());
			assertNotNull(response.getLoginToken());
			assertFalse(response.isInvalidEmailAddress());
			assertFalse(response.isInvalidName());
			assertFalse(response.isInvalidPassword());
			assertNotNull(response.getLoginToken().getToken());
			assertTrue(response.getLoginToken().getExpiresAt().after(Date.from(Instant.now())));
		}, context);
	}

	@Test
	public void testLogin(TestContext context) throws Exception {
		sync(() -> {
			String emailAddress = getEmailAddress();
			Accounts.createAccount(emailAddress, "password", getUsername());
			LoginResponse loginResponse = Accounts.login(emailAddress, "password");
			assertNotNull(loginResponse.getToken());
			assertNotNull(loginResponse.getToken().getToken());
			assertFalse(loginResponse.isBadPassword());
			assertFalse(loginResponse.isBadEmail());

			LoginRequest badEmail = new LoginRequest();
			badEmail.setEmail(RandomStringUtils.randomAlphanumeric(32) + "test@fidoas.com");
			badEmail.setPassword("*****dddd");
			LoginResponse badEmailResponse = Accounts.login(badEmail);
			assertTrue(badEmailResponse.isBadEmail());
			assertNull(badEmailResponse.getToken());

			LoginRequest badPassword = new LoginRequest();
			badPassword.setEmail(emailAddress);
			badPassword.setPassword("*****dddd");
			LoginResponse basPasswordResponse = Accounts.login(badPassword);
			assertTrue(basPasswordResponse.isBadPassword());
			assertNull(basPasswordResponse.getToken());
		}, context);
	}

	@NotNull
	public String getEmailAddress() {
		return "a" + RandomStringUtils.randomAlphanumeric(32) + "test@test.com";
	}

	@Test
	public void testIsAuthorizedWithToken(TestContext context) throws Exception {
		sync(() -> {
			CreateAccountResponse response = Accounts.createAccount(getEmailAddress(), "password", getUsername());
			String secret = response.getLoginToken().getSecret();
			assertTrue(Accounts.isAuthorizedWithToken(response.getUserId(), secret));
			assertFalse(Accounts.isAuthorizedWithToken(response.getUserId(), null));
			assertFalse(Accounts.isAuthorizedWithToken(response.getUserId(), ""));
			assertFalse(Accounts.isAuthorizedWithToken(response.getUserId(), "a"));
			assertFalse(Accounts.isAuthorizedWithToken("A", null));
			assertFalse(Accounts.isAuthorizedWithToken("A", ""));
			assertFalse(Accounts.isAuthorizedWithToken("A", "b"));
		}, context);
	}

	@NotNull
	public String getUsername() {
		return RandomStringUtils.randomAlphanumeric(32) + "username";
	}

	@Test
	public void testGet(TestContext context) throws Exception {
		sync(() -> {
			String emailAddress = getEmailAddress();
			String username = getUsername();
			CreateAccountResponse response = Accounts.createAccount(emailAddress, "password", username);
			UserRecord profile = Accounts.get(response.getUserId());
			assertNotNull(profile);
			assertEquals(profile.getEmails().get(0).getAddress(), emailAddress);
			assertEquals(profile.getUsername(), username);
			assertNull(Accounts.get("a"));
			assertNull(Accounts.get((String) null));
		}, context);
	}


	@Test
	public void testPasswordReset(TestContext context) {
		sync(() -> {
			try (UnityClient client = new UnityClient(context)) {
				Sync.invoke0(client::createUserAccount);
				String token = Accounts.createResetToken(client.getUserId().toString()).getToken();
				Sync.invoke0(() -> client.getApi().postPasswordReset(token, "io/vertx/test", "io/vertx/test"));
				/*
				HttpResponse<Buffer> res = awaitResult(h -> WebClient.create(contextRule.vertx())
						.post(client.getApiClient().getBasePath() + "/reset/passwords/with-token")
						.addQueryParam("token", token)
						.sendForm(MultiMap.caseInsensitiveMultiMap()
								.add("password1", "test")
								.add("password2", "test"), h));
				*/

				try (UnityClient client2 = new UnityClient(context)) {
					com.hiddenswitch.spellsource.client.models.LoginResponse res2 = Sync.invoke(client2.getApi()::login, new com.hiddenswitch.spellsource.client.models.LoginRequest().email(client.getAccount().getEmail()).password("io/vertx/test"));
					context.assertNotNull(res2.getLoginToken());
				}
			}
		}, context);
	}

	@Test
	public void testPasswordResetWrongToken(TestContext context) {
		sync(() -> {
			try (UnityClient client = new UnityClient(context)) {
				Sync.invoke0(client::createUserAccount);
				String token = "faketoken";
				Sync.invoke0(() -> client.getApi().postPasswordReset(token, "io/vertx/test", "io/vertx/test"));
				/*
				HttpResponse<Buffer> res = awaitResult(h -> WebClient.create(contextRule.vertx())
						.post(client.getApiClient().getBasePath() + "/reset/passwords/with-token")
						.addQueryParam("token", token)
						.sendForm(MultiMap.caseInsensitiveMultiMap()
								.add("password1", "test")
								.add("password2", "test"), h));
				*/

				try (UnityClient client2 = new UnityClient(context)) {
					try {
						com.hiddenswitch.spellsource.client.models.LoginResponse res2 = Sync.invoke(client2.getApi()::login, new com.hiddenswitch.spellsource.client.models.LoginRequest().email(client.getAccount().getEmail()).password("io/vertx/test"));
						context.fail("should not reach");
					} catch (RuntimeException ex) {
						context.assertTrue(((ApiException) ex.getCause()).getResponseBody().contains("Bad password"));
					}
				}
			}
		}, context);
	}

	@Test
	public void testRemoveAccount(TestContext context) {
		sync(() -> {
			CreateAccountResponse account = createRandomAccount();
			Accounts.removeAccount(new UserId(account.getUserId()));
			context.assertNull(Accounts.get(account.getUserId()));
		}, context);
	}

	@Test
	public void testRemoveAccounts(TestContext context) {
		sync(() -> {
			CreateAccountResponse account1 = createRandomAccount();
			CreateAccountResponse account2 = createRandomAccount();
			Accounts.removeAccounts(Arrays.asList(new UserId(account1.getUserId()), new UserId(account2.getUserId())));
			context.assertNull(Accounts.get(account1.getUserId()));
			context.assertNull(Accounts.get(account2.getUserId()));
		}, context);
	}
}