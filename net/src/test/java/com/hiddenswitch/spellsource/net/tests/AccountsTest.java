package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.net.Accounts;
import com.hiddenswitch.spellsource.net.impl.UserId;
import com.hiddenswitch.spellsource.net.models.ChangePasswordRequest;
import com.hiddenswitch.spellsource.net.models.LoginRequest;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxTestContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

import static io.vertx.ext.sync.Sync.invoke;
import static io.vertx.ext.sync.Sync.invoke0;
import static net.demilich.metastone.tests.util.TestBase.assertThrows;
import static org.junit.jupiter.api.Assertions.*;

public class AccountsTest extends SpellsourceTestBase {

	@Test
	public void testChangePassword(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var account = createRandomAccount();
			assertTrue(Accounts.isAuthorizedWithToken(account.getUserId(), account.getLoginToken().getSecret()));
			Accounts.changePassword(ChangePasswordRequest.request(new UserId(account.getUserId()), "test"));
			assertFalse(Accounts.isAuthorizedWithToken(account.getUserId(), account.getLoginToken().getSecret()), "should log out user");
		}, context, vertx);
	}

	@Test
	public void testChangePasswordRejectsInvalidPassword(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var account = createRandomAccount();
			assertTrue(Accounts.isAuthorizedWithToken(account.getUserId(), account.getLoginToken().getSecret()));
			assertThrows(SecurityException.class, () -> Accounts.changePassword(ChangePasswordRequest.request(new UserId(account.getUserId()), "*")));
			assertTrue(Accounts.isAuthorizedWithToken(account.getUserId(), account.getLoginToken().getSecret()), "should not log out user");
		}, context, vertx);
	}

	@Test
	public void testCreateAccount(Vertx vertx, VertxTestContext context) throws Exception {
		runOnFiberContext(() -> {
			var response = Accounts.createAccount(getEmailAddress(), "destructoid", getUsername());
			assertNotNull(response.getLoginToken());
			assertFalse(response.isInvalidEmailAddress());
			assertFalse(response.isInvalidName());
			assertFalse(response.isInvalidPassword());
			assertNotNull(response.getLoginToken().getToken());
			assertTrue(response.getLoginToken().getExpiresAt().after(Date.from(Instant.now())));
		}, context, vertx);
	}

	@Test
	public void testLogin(Vertx vertx, VertxTestContext context) throws Exception {
		runOnFiberContext(() -> {
			var emailAddress = getEmailAddress();
			Accounts.createAccount(emailAddress, "password", getUsername());
			var loginResponse = Accounts.login(emailAddress, "password");
			assertNotNull(loginResponse.getToken());
			assertNotNull(loginResponse.getToken().getToken());
			assertFalse(loginResponse.isBadPassword());
			assertFalse(loginResponse.isBadEmail());

			var badEmail = new LoginRequest();
			badEmail.setEmail(RandomStringUtils.randomAlphanumeric(32) + "test@fidoas.com");
			badEmail.setPassword("*****dddd");
			var badEmailResponse = Accounts.login(badEmail);
			assertTrue(badEmailResponse.isBadEmail());
			assertNull(badEmailResponse.getToken());

			var badPassword = new LoginRequest();
			badPassword.setEmail(emailAddress);
			badPassword.setPassword("*****dddd");
			var basPasswordResponse = Accounts.login(badPassword);
			assertTrue(basPasswordResponse.isBadPassword());
			assertNull(basPasswordResponse.getToken());
		}, context, vertx);
	}

	@NotNull
	public String getEmailAddress() {
		return "a" + RandomStringUtils.randomAlphanumeric(32) + "test@test.com";
	}

	@Test
	public void testIsAuthorizedWithToken(Vertx vertx, VertxTestContext context) throws Exception {
		runOnFiberContext(() -> {
			var response = Accounts.createAccount(getEmailAddress(), "password", getUsername());
			var secret = response.getLoginToken().getSecret();
			assertTrue(Accounts.isAuthorizedWithToken(response.getUserId(), secret));
			assertFalse(Accounts.isAuthorizedWithToken(response.getUserId(), null));
			assertFalse(Accounts.isAuthorizedWithToken(response.getUserId(), ""));
			assertFalse(Accounts.isAuthorizedWithToken(response.getUserId(), "a"));
			assertFalse(Accounts.isAuthorizedWithToken("A", null));
			assertFalse(Accounts.isAuthorizedWithToken("A", ""));
			assertFalse(Accounts.isAuthorizedWithToken("A", "b"));
		}, context, vertx);
	}

	@NotNull
	public String getUsername() {
		return RandomStringUtils.randomAlphanumeric(32) + "username";
	}

	@Test
	public void testGet(Vertx vertx, VertxTestContext context) throws Exception {
		runOnFiberContext(() -> {
			var emailAddress = getEmailAddress();
			var username = getUsername();
			var response = Accounts.createAccount(emailAddress, "password", username);
			var profile = Accounts.get(response.getUserId());
			assertNotNull(profile);
			assertEquals(profile.getEmails().get(0).getAddress(), emailAddress);
			assertEquals(profile.getUsername(), username);
			assertNull(Accounts.get("a"));
			assertNull(Accounts.get((String) null));
		}, context, vertx);
	}


	@Test
	public void testPasswordReset(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			try (var client = new UnityClient(context)) {
				invoke0(client::createUserAccount);
				var token = Accounts.createResetToken(client.getUserId().toString()).getToken();
				invoke0(() -> client.getApi().postPasswordReset(token, "test", "test"));
				try (var client2 = new UnityClient(context)) {
					var res2 = invoke(client2.getApi()::login, new com.hiddenswitch.spellsource.client.models.LoginRequest().email(client.getAccount().getEmail()).password("test"));
					assertNotNull(res2.getLoginToken());
				}
			}
		}, context, vertx);
	}

	@Test
	public void testPasswordResetWrongToken(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			try (var client = new UnityClient(context)) {
				invoke0(client::createUserAccount);
				var token = "faketoken";
				invoke0(() -> client.getApi().postPasswordReset(token, "test", "test"));

				try (var client2 = new UnityClient(context)) {
					try {
						invoke(client2.getApi()::login, new com.hiddenswitch.spellsource.client.models.LoginRequest().email(client.getAccount().getEmail()).password("test"));
						fail("should not reach");
					} catch (RuntimeException ex) {
						assertTrue(((ApiException) ex.getCause()).getResponseBody().contains("Bad password"));
					}
				}
			}
		}, context, vertx);
	}

	@Test
	public void testRemoveAccount(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var account = createRandomAccount();
			Accounts.removeAccount(new UserId(account.getUserId()));
			assertNull(Accounts.get(account.getUserId()));
		}, context, vertx);
	}

	@Test
	public void testRemoveAccounts(Vertx vertx, VertxTestContext context) {
		runOnFiberContext(() -> {
			var account1 = createRandomAccount();
			var account2 = createRandomAccount();
			Accounts.removeAccounts(Arrays.asList(new UserId(account1.getUserId()), new UserId(account2.getUserId())));
			assertNull(Accounts.get(account1.getUserId()));
			assertNull(Accounts.get(account2.getUserId()));
		}, context, vertx);
	}
}