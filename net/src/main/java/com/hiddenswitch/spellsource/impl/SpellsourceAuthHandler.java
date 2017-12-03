package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.Accounts;
import com.hiddenswitch.spellsource.util.Rpc;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.AuthHandler;

import java.util.Set;

class SpellsourceAuthHandler implements AuthHandler {
	EventBus bus;

	public SpellsourceAuthHandler(EventBus bus) {
		this.bus = bus;
	}

	@Override
	public AuthHandler addAuthority(String authority) {
		// Authorities are not supported
		throw new UnsupportedOperationException();
	}

	@Override
	public AuthHandler addAuthorities(Set<String> authorities) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
		try {
			String apiKey = context.request().getHeader("X-Auth-Token");
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
		if (user == null) {
			handler.handle(Future.failedFuture(new SecurityException("Null user in SpellsourceAuthHandler")));
		} else {
			// We don't support authorities, so go ahead and authorize the user.
			handler.handle(Future.succeededFuture());
		}
	}

	@Override
	public void handle(RoutingContext event) {
		parseCredentials(event, credentials -> {
			if (credentials.failed()) {
				final String message = "Parse failed: " + credentials.cause().getMessage();
				fail(event, message);
				return;
			}

			final String userId = credentials.result().getString("userId");
			final String secret = credentials.result().getString("secret");
			final String token = userId + ":" + secret;

			Rpc.connect(Accounts.class, bus)
					.promise(accounts -> accounts.getWithToken(token))
					.setHandler(getResult -> {
						if (getResult.failed()) {
							fail(event, "Try logging in again.");
							return;
						}

						if (getResult.result() == null) {
							fail(event, "Try logging in again.");
							return;
						}

						event.setUser(getResult.result());
						event.next();
					});
		});
	}

	private void fail(RoutingContext event, String message) {
		event.fail(403);
	}
}
