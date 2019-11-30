package com.hiddenswitch.spellsource.impl;

import co.paralleluniverse.strands.SuspendableAction1;
import com.hiddenswitch.spellsource.Accounts;
import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.Sync;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;

import java.util.Set;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

/**
 * A HTTP header based authentication method that uses login tokens to authorize users.
 */
public class SpellsourceAuthHandler implements AuthHandler {
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
			if (context.request().headers().contains("X-Auth-Token")) {
				apiKey = context.request().getHeader("X-Auth-Token");
			} else if (context.request().params().contains("X-Auth-Token")) {
				apiKey = context.request().params().get("X-Auth-Token");
			} else {
				throw new IllegalArgumentException("No X-Auth-Token header or param specified.");
			}

			String[] components = apiKey.split(":");
			String userId = components[0];
			String secret = components[1];
			handler.handle(Future.succeededFuture(new JsonObject()
					.put("userId", userId)
					.put("secret", secret)));
		} catch (Throwable ignored) {
			handler.handle(Future.failedFuture(ignored));
		}
	}

	@Override
	public void authorize(User user, Handler<AsyncResult<Void>> handler) {
		throw new UnsupportedOperationException("authorities are not supported");
	}

	@Override
	public void handle(RoutingContext event) {
		parseCredentials(event, credentials -> {
			if (credentials.failed()) {
				String message = "Parse failed: " + credentials.cause().getMessage();
				fail(event, message);
				return;
			}

			String userId = credentials.result().getString("userId");
			String secret = credentials.result().getString("secret");
			String token = userId + ":" + secret;

			Vertx.currentContext().runOnContext(v1 -> {
				Vertx.currentContext().runOnContext(suspendableHandler(v2 -> {
					UserRecord record = Accounts.getWithToken(token);
					event.setUser(record);
					if (record == null) {
						fail(event, "Try logging in again.");
						return;
					}
					event.next();
				}));
			});
		});
	}

	private void fail(RoutingContext event, String message) {
		event.fail(403);
	}

	public static AuthHandler create() {
		return new SpellsourceAuthHandler();
	}
}
