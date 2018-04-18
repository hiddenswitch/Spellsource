package com.hiddenswitch.spellsource;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.models.CreateAccountResponse;
import com.hiddenswitch.spellsource.models.LoginRequest;
import com.hiddenswitch.spellsource.models.LoginResponse;
import com.hiddenswitch.spellsource.util.Logging;
import io.vertx.ext.unit.TestContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.sql.Date;
import java.time.Instant;

import static org.junit.Assert.*;

public class AccountsTest extends SpellsourceTestBase {
	@Test
	public void testCreateAccount(TestContext context) throws Exception {
		sync(() -> {
			Logging.setLoggingLevel(Level.ERROR);
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
			assertThrows(() -> Accounts.get(null));
		});
	}

	public static void assertThrows(ThrowingRunnable runnable) {
		assertThrows(Throwable.class, runnable);
	}

	/**
	 * Asserts that {@code runnable} throws an exception of type {@code throwableClass} when executed. If it does not
	 * throw an exception, an {@link AssertionError} is thrown. If it throws the wrong type of exception, an {@code
	 * AssertionError} is thrown describing the mismatch; the exception that was actually thrown can be obtained by
	 * calling {@link AssertionError#getCause}.
	 *
	 * @param throwableClass the expected type of the exception
	 * @param runnable       A function that is expected to throw an exception when invoked
	 * @since 6.9.5
	 */
	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
	@Suspendable
	public static <T extends Throwable> void assertThrows(Class<T> throwableClass, ThrowingRunnable runnable) {
		expectThrows(throwableClass, runnable);
	}

	@Suspendable
	public static <T extends Throwable> T expectThrows(Class<T> throwableClass, ThrowingRunnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			if (throwableClass.isInstance(t)) {
				return throwableClass.cast(t);
			} else {
				String mismatchMessage = String.format("Expected %s to be thrown, but %s was thrown",
								throwableClass.getSimpleName(), t.getClass().getSimpleName());

				final AssertionError cause = new AssertionError(mismatchMessage, t);
				fail(cause.getMessage());
				return null;
			}
		}
		String message = String.format("Expected %s to be thrown, but nothing was thrown",
						throwableClass.getSimpleName());
		fail(new AssertionError(message).getMessage());
		return null;
	}

	public interface ThrowingRunnable {
		@Suspendable
		void run() throws Throwable;
	}
}