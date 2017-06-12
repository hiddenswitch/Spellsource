package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.impl.util.UserRecord;
import com.hiddenswitch.proto3.net.impl.AccountsImpl;
import com.hiddenswitch.proto3.net.impl.auth.TokenAuthProvider;
import com.hiddenswitch.proto3.net.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.models.LoginRequest;
import com.hiddenswitch.proto3.net.models.LoginResponse;
import com.hiddenswitch.proto3.net.impl.ServiceTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.sync.Sync;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.sql.Date;
import java.time.Instant;

import static org.junit.Assert.*;

public class AccountsTest extends ServiceTest<AccountsImpl> {
	@Test
	public void testAuthProvider(TestContext context) throws Exception {
		final String username = "doctorpangloss";
		final String password = "destructoid";
		final String emailAddress = "benjamin.s.berman@gmail.com";

		wrapSync(context, () -> {
			final AuthProvider tokenAuthProvider = new TokenAuthProvider(vertx);
			final CreateAccountResponse response = service.createAccount(emailAddress, password, username);
			getContext().assertNotNull(response);

			User user = Sync.awaitResult(done -> tokenAuthProvider.authenticate(
					new JsonObject()
							.put("username", emailAddress)
							.put("password", password),
					done));

			getContext().assertNotNull(user);
			final UserRecord userRecord = (UserRecord) user;
			getContext().assertNotNull(userRecord);
			getContext().assertNotNull(userRecord.getProfile().getEmailAddress());
			getContext().assertEquals(emailAddress, userRecord.getProfile().getEmailAddress());
			getContext().assertNotNull(response.getLoginToken());
			getContext().assertNotNull(response.getLoginToken().getToken());

			final String token = response.getLoginToken().getToken();
			User userTokened = Sync.awaitResult(done -> {
				tokenAuthProvider.authenticate(
						new JsonObject()
								.put("token", token),
						done);
			});
			getContext().assertNotNull(userTokened);
		});
	}

	@Test
	public void testCreateAccount(TestContext context) throws Exception {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			setLoggingLevel(Level.ERROR);
			CreateAccountResponse response = service.createAccount("benjamin.s.berman@gmail.com", "destructoid", "doctorpangloss");
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
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			CreateAccountResponse response = service.createAccount("test@test.com", "password", "username");
			LoginResponse loginResponse = service.login("test@test.com", "password");
			assertNotNull(loginResponse.getToken());
			assertNotNull(loginResponse.getToken().getToken());
			assertFalse(loginResponse.isBadPassword());
			assertFalse(loginResponse.isBadEmail());

			LoginRequest badEmail = new LoginRequest();
			badEmail.setEmail("test@fidoas.com");
			badEmail.setPassword("*****dddd");
			LoginResponse badEmailResponse = service.login(badEmail);
			assertTrue(badEmailResponse.isBadEmail());
			assertNull(badEmailResponse.getToken());

			LoginRequest badPassword = new LoginRequest();
			badPassword.setEmail("test@test.com");
			badPassword.setPassword("*****dddd");
			LoginResponse basPasswordResponse = service.login(badPassword);
			assertTrue(basPasswordResponse.isBadPassword());
			assertNull(basPasswordResponse.getToken());
		});
	}

	@Test
	public void testIsAuthorizedWithToken(TestContext context) throws Exception {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			CreateAccountResponse response = service.createAccount("test@test.com", "password", "username");
			final String secret = response.getLoginToken().getSecret();
			getContext().assertTrue(service.isAuthorizedWithToken(response.getUserId(), secret));
			getContext().assertFalse(service.isAuthorizedWithToken(response.getUserId(), null));
			getContext().assertFalse(service.isAuthorizedWithToken(response.getUserId(), ""));
			getContext().assertFalse(service.isAuthorizedWithToken(response.getUserId(), "a"));
			getContext().assertFalse(service.isAuthorizedWithToken("A", null));
			getContext().assertFalse(service.isAuthorizedWithToken("A", ""));
			getContext().assertFalse(service.isAuthorizedWithToken("A", "b"));
		});
	}

	@Test
	public void testGet(TestContext context) throws Exception {
		setLoggingLevel(Level.ERROR);
		wrapSync(context, () -> {
			CreateAccountResponse response = service.createAccount("test@test.com", "password", "username");
			UserRecord profile = service.get(response.getUserId());
			getContext().assertNotNull(profile);
			getContext().assertEquals(profile.getProfile().getEmailAddress(), "test@test.com");
			getContext().assertEquals(profile.getProfile().getDisplayName(), "username");
			assertThrows(() -> service.get("a"));
			assertThrows(() -> service.get(null));
		});
	}

	public static void assertThrows(ThrowingRunnable runnable) {
		assertThrows(Throwable.class, runnable);
	}

	/**
	 * Asserts that {@code runnable} throws an exception of type {@code throwableClass} when
	 * executed. If it does not throw an exception, an {@link AssertionError} is thrown. If it
	 * throws the wrong type of exception, an {@code AssertionError} is thrown describing the
	 * mismatch; the exception that was actually thrown can be obtained by calling {@link
	 * AssertionError#getCause}.
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
				getContext().fail(cause);
				return null;
			}
		}
		String message = String.format("Expected %s to be thrown, but nothing was thrown",
				throwableClass.getSimpleName());
		getContext().fail(new AssertionError(message));
		return null;
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<AccountsImpl>> done) {
		AccountsImpl instance = new AccountsImpl();

		vertx.deployVerticle(instance, andThen -> {
			done.handle(Future.succeededFuture(instance));
		});
	}

	public interface ThrowingRunnable {
		@Suspendable
		void run() throws Throwable;
	}
}