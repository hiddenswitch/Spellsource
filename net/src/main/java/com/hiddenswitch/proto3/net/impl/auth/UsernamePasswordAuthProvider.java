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
				resultHandler.handle(new Result<>(response.cause()));
				return;
			}

			if (response.result().isBadPassword()
					|| response.result().isBadUsername()) {
				resultHandler.handle(new Result<>(new AuthenticationException(response.result()), null));
			} else {
				resultHandler.handle(new Result<>(response.result().getRecord()));
			}
		}).login(new LoginRequest().withUserId(authInfo
				.getString("username"))
				.withPassword(authInfo.getString("password")));
	}
}
