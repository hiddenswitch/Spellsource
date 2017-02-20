package com.hiddenswitch.proto3.net.impl.auth;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Accounts;
import com.hiddenswitch.proto3.net.amazon.LoginRequest;
import com.hiddenswitch.proto3.net.amazon.LoginResponse;
import com.hiddenswitch.proto3.net.impl.util.AuthenticationException;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.Result;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

/**
 * Created by bberman on 1/26/17.
 */
public class UsernamePasswordAuthProvider implements AuthProvider {
	protected Vertx vertx;
	protected ServiceProxy<Accounts> accounts;

	public UsernamePasswordAuthProvider(Vertx vertx) {
		if (vertx == null) {
			throw new NullPointerException("vertx must not be null.");
		}

		this.vertx = vertx;
		accounts = Broker.proxy(Accounts.class, vertx.eventBus());
	}

	@Override
	@Suspendable
	public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
		accounts.async((AsyncResult<LoginResponse> response) -> {
			if (response.failed()) {
				resultHandler.handle(Future.failedFuture(response.cause()));
				return;
			}

			if (response.result().isBadPassword()
					|| response.result().isBadEmail()) {
				resultHandler.handle(Future.failedFuture(new AuthenticationException(response.result())));
			} else {
				resultHandler.handle(Future.succeededFuture(response.result().getRecord()));
			}
		}).login(new LoginRequest().withEmail(authInfo
				.getString("username"))
				.withPassword(authInfo.getString("password")));
	}
}
