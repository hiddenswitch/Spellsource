package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.models.LoginRequest;
import com.hiddenswitch.spellsource.models.LoginResponse;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.tests.util.TestBase;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.sql.Date;
import java.time.Instant;

import static net.demilich.metastone.tests.util.TestBase.assertThrows;
import static org.junit.Assert.*;

public class AccountsTest extends SpellsourceTestBase {
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
		});
	}

	@Test
	public void testLogin(TestContext context) throws Exception {
		sync(() -> {
			final String emailAddress = getEmailAddress();
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
		});
	}

	@NotNull
	public String getEmailAddress() {
		return "a" + RandomStringUtils.randomAlphanumeric(32) + "test@test.com";
	}

	@Test
	public void testIsAuthorizedWithToken(TestContext context) throws Exception {
		sync(() -> {
			CreateAccountResponse response = Accounts.createAccount(getEmailAddress(), "password", getUsername());
			final String secret = response.getLoginToken().getSecret();
			assertTrue(Accounts.isAuthorizedWithToken(response.getUserId(), secret));
			assertFalse(Accounts.isAuthorizedWithToken(response.getUserId(), null));
			assertFalse(Accounts.isAuthorizedWithToken(response.getUserId(), ""));
			assertFalse(Accounts.isAuthorizedWithToken(response.getUserId(), "a"));
			assertFalse(Accounts.isAuthorizedWithToken("A", null));
			assertFalse(Accounts.isAuthorizedWithToken("A", ""));
			assertFalse(Accounts.isAuthorizedWithToken("A", "b"));
		});
	}

	@NotNull
	public String getUsername() {
		return RandomStringUtils.randomAlphanumeric(32) + "username";
	}

	@Test
	public void testGet(TestContext context) throws Exception {
		sync(() -> {
			final String emailAddress = getEmailAddress();
			final String username = getUsername();
			CreateAccountResponse response = Accounts.createAccount(emailAddress, "password", username);
			UserRecord profile = Accounts.get(response.getUserId());
			assertNotNull(profile);
			assertEquals(profile.getEmails().get(0).getAddress(), emailAddress);
			assertEquals(profile.getUsername(), username);
			assertThrows(() -> Accounts.get("a"));
			assertThrows(() -> Accounts.get((String) null));
		});
	}

}