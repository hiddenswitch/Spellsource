package com.hiddenswitch.spellsource.impl.auth;

import com.hiddenswitch.spellsource.util.ApiKeyAuthHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;

/**
 * Created by bberman on 2/17/17.
 */
public class ApiKeyAuthHandlerImpl extends AuthHandlerImpl implements ApiKeyAuthHandler {
	private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthHandlerImpl.class);
	private final String skip;
	private final JsonObject options;

	public ApiKeyAuthHandlerImpl(AuthProvider authProvider, String skip) {
		super(authProvider);
		this.skip = skip;
		options = new JsonObject();
	}

	private static void ignore(AsyncResult<Void> event) {
	}

	@Override
	public ApiKeyAuthHandlerImpl setHeader(String header) {
		options.put("header", header);
		return this;
	}

	@Override
	public CharSequence getHeader() {
		return options.containsKey("header") ? options.getString("header") : HttpHeaders.AUTHORIZATION;
	}

	@Override
	public void handle(final RoutingContext context) {
		User user = context.user();
		if (user != null) {
			// Already authenticated in, just authorise
			authorize(user, (then) -> {
				if (then.succeeded()) {
					context.next();
				} else {
					context.fail(then.cause());
				}
			});
		} else {
			if (skip != null && context.normalisedPath().startsWith(skip)) {
				context.next();
				return;
			}

			parseCredentials(context, credentials -> authProvider.authenticate(credentials.result(), res -> {
				if (res.succeeded()) {
					final User user2 = res.result();
					context.setUser(user2);
					authorize(user2, (then) -> {
						if (then.succeeded()) {
							context.next();
						} else {
							context.fail(then.cause());
						}
					});
				} else {
					log.warn("Authentication failure.", res.cause());
					context.fail(401);
				}
			}));
		}
	}

	@Override
	public void parseCredentials(RoutingContext context, Handler<AsyncResult<JsonObject>> handler) {
		final HttpServerRequest request = context.request();
		final String token = request.headers().get(getHeader());
		if (token == null) {
			handler.handle(Future.failedFuture(String.format("No %s header was found", getHeader())));
		} else {
			handler.handle(Future.succeededFuture(new JsonObject().put("token", token).put("options", options)));
		}
	}
}
