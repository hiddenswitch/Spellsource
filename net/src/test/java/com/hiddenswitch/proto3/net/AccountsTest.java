package com.hiddenswitch.proto3.net;

import ch.qos.logback.classic.Level;
import com.hiddenswitch.proto3.net.amazon.UserRecord;
import com.hiddenswitch.proto3.net.impl.AccountsImpl;
import com.hiddenswitch.proto3.net.impl.auth.TokenAuthProvider;
import com.hiddenswitch.proto3.net.models.CreateAccountResponse;
import com.hiddenswitch.proto3.net.amazon.LoginRequest;
import com.hiddenswitch.proto3.net.amazon.LoginResponse;
import com.hiddenswitch.proto3.net.amazon.Profile;
import com.hiddenswitch.proto3.net.util.Result;
import com.hiddenswitch.proto3.net.util.ServiceTest;
import io.vertx.core.AsyncResult;
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
		final AuthProvider tokenAuthProvider = new TokenAuthProvider(vertx);
		final String username = "doctorpangloss";
		final String password = "destructoid";
		final String emailAddress = "benjamin.s.berman@gmail.com";
		final CreateAccountResponse response = service.createAccount(emailAddress, password, username);

		getContext().assertNotNull(response);

		wrapSync(context, () -> {
			User user = Sync.awaitResult(done -> tokenAuthProvider.authenticate(
					new JsonObject()
							.put("username", username)
							.put("password", password),
					done));

			getContext().assertNotNull(user);
			final UserRecord userRecord = (UserRecord) user;
			getContext().assertNotNull(userRecord);
			getContext().assertNotNull(userRecord.getProfile().getEmailAddress());
			getContext().assertEquals(emailAddress, userRecord.getProfile().getEmailAddress());
			getContext().assertNotNull(response.loginToken);
			getContext().assertNotNull(response.loginToken.token);

			final String token = response.loginToken.token;
			User userTokened = Sync.awaitResult(done -> {

				tokenAuthProvider.authenticate(
						new JsonObject()
								.put("username", username)
								.put("token", token),
						done);
			});
			getContext().assertNotNull(userTokened);
		});
	}

	@Test
	public void testCreateAccount() throws Exception {
		setLoggingLevel(Level.ERROR);
		CreateAccountResponse response = service.createAccount("benjamin.s.berman@gmail.com", "destructoid", "doctorpangloss");
		assertNotNull(response.loginToken);
		assertFalse(response.invalidEmailAddress);
		assertFalse(response.invalidName);
		assertFalse(response.invalidPassword);
		assertNotNull(response.loginToken.token);
		assertTrue(response.loginToken.expiresAt.after(Date.from(Instant.now())));
	}

	@Test
	public void testLogin() throws Exception {
		setLoggingLevel(Level.ERROR);
		CreateAccountResponse response = service.createAccount("test@test.com", "password", "username");
		LoginResponse loginResponse = service.login("username", "password");
		assertNotNull(loginResponse.getToken());
		assertNotNull(loginResponse.getToken().token);
		assertFalse(loginResponse.isBadPassword());
		assertFalse(loginResponse.isBadUsername());

		LoginRequest badUsername = new LoginRequest();
		badUsername.setUserId("blah");
		badUsername.setPassword("*****dddd");
		LoginResponse badUsernameResponse = service.login(badUsername);
		assertTrue(badUsernameResponse.isBadUsername());
		assertNull(badUsernameResponse.getToken());

		LoginRequest badPassword = new LoginRequest();
		badPassword.setUserId("username");
		badPassword.setPassword("*****dddd");
		LoginResponse basPasswordResponse = service.login(badPassword);
		assertTrue(basPasswordResponse.isBadPassword());
		assertNull(basPasswordResponse.getToken());
	}

	@Test
	public void testIsAuthorizedWithToken() throws Exception {
		setLoggingLevel(Level.ERROR);
		CreateAccountResponse response = service.createAccount("test@test.com", "password", "username");
		assertTrue(service.isAuthorizedWithToken(response.userId, response.loginToken.token));
		assertFalse(service.isAuthorizedWithToken(response.userId, null));
		assertFalse(service.isAuthorizedWithToken(response.userId, ""));
		assertFalse(service.isAuthorizedWithToken(response.userId, "a"));
		assertFalse(service.isAuthorizedWithToken("A", null));
		assertFalse(service.isAuthorizedWithToken("A", ""));
		assertFalse(service.isAuthorizedWithToken("A", "b"));
	}

	@Test
	public void testGet() throws Exception {
		setLoggingLevel(Level.ERROR);
		CreateAccountResponse response = service.createAccount("test@test.com", "password", "username");
		Profile profile = service.get(response.userId);
		assertNotNull(profile);
		assertEquals(profile.getEmailAddress(), "test@test.com");
		assertEquals(profile.getName(), "username");
		Profile profile1 = service.get("a");
		assertNull(profile1);
		assertThrows(() -> service.get(null));
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
	public static <T extends Throwable> void assertThrows(Class<T> throwableClass, ThrowingRunnable runnable) {
		expectThrows(throwableClass, runnable);
	}

	public static <T extends Throwable> T expectThrows(Class<T> throwableClass, ThrowingRunnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			if (throwableClass.isInstance(t)) {
				return throwableClass.cast(t);
			} else {
				String mismatchMessage = String.format("Expected %s to be thrown, but %s was thrown",
						throwableClass.getSimpleName(), t.getClass().getSimpleName());

				throw new AssertionError(mismatchMessage, t);
			}
		}
		String message = String.format("Expected %s to be thrown, but nothing was thrown",
				throwableClass.getSimpleName());
		throw new AssertionError(message);
	}

	@Override
	public void deployServices(Vertx vertx, Handler<AsyncResult<AccountsImpl>> done) {
		AccountsImpl instance = new AccountsImpl();
		vertx.executeBlocking(fut -> {
			instance.withEmbeddedConfiguration();
			fut.complete();
		}, then -> vertx.deployVerticle(instance, andThen -> {
			if (instance.getDynamo() == null
					|| andThen.failed()) {
				throw new RuntimeException("The embedded DynamoDB server failed to start.");
			}
			done.handle(new Result<>(instance));
		}));

	}

	public interface ThrowingRunnable {
		void run() throws Throwable;
	}

	@Test
	public void testGetProfileForId() throws Exception {
		setLoggingLevel(Level.ERROR);
		service.createAccount("test@test.com", "password", "username");
		assertEquals(service.getProfileForId("username").name, "username");
		assertNull(service.getProfileForId("a"));
		assertThrows(() -> {
			service.getProfileForId(null);
		});
	}
}