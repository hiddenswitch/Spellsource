package com.hiddenswitch.spellsource.net.impl;

import com.hiddenswitch.spellsource.net.Accounts;
import com.hiddenswitch.spellsource.net.impl.util.UserRecord;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;

import java.util.Set;

import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;

/**
 * A HTTP header based authentication method that uses login tokens to authorize users.
 */
public class SpellsourceAuthHandler implements AuthHandler {
	public static final String HEADER = "X-Auth-Token";

	private SpellsourceAuthHandler() {
	}

	@Override
	public AuthHandler addAuthority(String authority) {
		// Authorities are not supported
		throw new UnsupportedOperationException("authorities are not supported");
	}

	@Override
	public AuthHandler addAuthorities(Set<String> authorities) {
		throw new UnsupportedOperationException("authorities are not supported");
	}

	@Override
	public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
		try {
			String apiKey;
			if (context.request().headers().contains(SpellsourceAuthHandler.HEADER)) {
				apiKey = context.request().getHeader(SpellsourceAuthHandler.HEADER);
			} else if (context.request().params().contains(SpellsourceAuthHandler.HEADER)) {
				apiKey = context.request().params().get(SpellsourceAuthHandler.HEADER);
			} else {
				throw new IllegalArgumentException("No " + SpellsourceAuthHandler.HEADER + " header or param specified.");
			}

			String[] components = apiKey.split(":");
			String userId = components[0];
			String secret = components[1];
			handler.handle(Future.succeededFuture(new JsonObject()
					.put("userId", userId)
					.put("secret", secret)));
		} catch (Throwable throwable) {
			handler.handle(Future.failedFuture(throwable));
		}
	}

	@Override
	public void authorize(User user, Handler<AsyncResult<Void>> handler) {
		throw new UnsupportedOperationException("authorities are not supported");
	}

	@Override
	public void handle(RoutingContext event) {
		parseCredentials(event, Sync.fiber(credentials -> {
			if (credentials.failed()) {
				event.fail(403, credentials.cause());
				return;
			}

			String userId = credentials.result().getString("userId");
			String secret = credentials.result().getString("secret");
			String token = userId + ":" + secret;


			UserRecord record = Accounts.getWithToken(token);
			event.setUser(record);
			if (record == null) {
				event.fail(403, new SecurityException("invalid login"));
				return;
			}
			event.next();
		}));
	}

	public static AuthHandler create() {
		return new SpellsourceAuthHandler();
	}
}
