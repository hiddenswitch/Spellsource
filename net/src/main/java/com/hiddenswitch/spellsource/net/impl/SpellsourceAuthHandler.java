package com.hiddenswitch.spellsource.net.impl;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.Accounts;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthenticationHandler;
import io.vertx.ext.web.handler.impl.AuthenticationHandlerImpl;
import io.vertx.ext.web.handler.impl.HttpStatusException;
import org.jetbrains.annotations.NotNull;

import static io.vertx.ext.sync.Sync.fiber;
import static io.vertx.ext.sync.Sync.await;

/**
 * A HTTP header based authentication method that uses login tokens to authorize users.
 */
public class SpellsourceAuthHandler extends AuthenticationHandlerImpl<SpellsourceAuthHandler> implements AuthenticationProvider {
	public static final String HEADER = "X-Auth-Token";

	private SpellsourceAuthHandler() {
		super(null, "");
	}

	public static AuthenticationHandler create() {
		return new SpellsourceAuthHandler();
	}

	@Override
	@Suspendable
	public void authenticate(JsonObject jsonObject, Handler<AsyncResult<User>> handler) {
		String token = toToken(jsonObject);

		fiber(() -> Accounts.getWithToken(token))
				.compose(record -> {
					if (record == null) {
						return Future.failedFuture(new SecurityException("invalid login"));
					}
					return Future.succeededFuture((User) record);
				})
				.onComplete(handler);
	}

	@NotNull
	public static String toToken(JsonObject jsonObject) {
		String userId = jsonObject.getString("userId");
		String secret = jsonObject.getString("secret");
		String token = userId + ":" + secret;
		return token;
	}

	@Override
	@Suspendable
	public void parseCredentials(RoutingContext context, Handler<AsyncResult<Credentials>> handler) {
		try {
			String apiKey;
			var request = context.request();
			if (request.headers().contains(SpellsourceAuthHandler.HEADER)) {
				apiKey = request.getHeader(SpellsourceAuthHandler.HEADER);
			} else if (request.params().contains(SpellsourceAuthHandler.HEADER)) {
				apiKey = request.params().get(SpellsourceAuthHandler.HEADER);
			} else {
				throw new IllegalArgumentException("No " + SpellsourceAuthHandler.HEADER + " header or param specified.");
			}

			String[] components = apiKey.split(":");
			String userId = components[0];
			String secret = components[1];

			Credentials credentials = new Credentials() {
				@Override
				public JsonObject toJson() {
					return new JsonObject().put("userId", userId).put("secret", secret);
				}
			};

			handler.handle(Future.succeededFuture(credentials));
		} catch (Throwable throwable) {
			handler.handle(Future.failedFuture(throwable));
		}
	}

	@Override
	protected AuthenticationProvider getAuthProvider(RoutingContext ctx) {
		return this;
	}

	@Suspendable
	public static void authorize(RoutingContext context) {
		try {
			var auth = SpellsourceAuthHandler.create();
			var credentials = await(auth.parseCredentials(context));
			var account = Accounts.getWithToken(toToken(credentials.toJson()));
			if (account == null) {
				throw new HttpStatusException(403, "no account");
			}
			context.setUser(account);
		} catch (Throwable t) {
			throw new HttpStatusException(403, t);
		}
	}
}
