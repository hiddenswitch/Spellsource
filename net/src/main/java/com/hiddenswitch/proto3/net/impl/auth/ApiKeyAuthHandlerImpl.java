package com.hiddenswitch.proto3.net.impl.auth;

import com.hiddenswitch.proto3.net.ApiKeyAuthHandler;
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
	public void handle(RoutingContext context) {
		User user = context.user();
		if (user != null) {
			// Already authenticated in, just authorise
			authorise(user, context);
		} else {
			final HttpServerRequest request = context.request();

			if (skip != null && context.normalisedPath().startsWith(skip)) {
				context.next();
				return;
			}

			final String token = request.headers().get(getHeader());

			if (token == null) {
				log.warn("No {} header was found", getHeader());
				context.fail(401);
				return;
			}

			authProvider.authenticate(new JsonObject().put("token", token).put("options", options), res -> {
				if (res.succeeded()) {
					final User user2 = res.result();
					context.setUser(user2);
					authorise(user2, context);
				} else {
					log.warn("Authentication failure.", res.cause());
					context.fail(401);
				}
			});
		}
	}
}
