package com.hiddenswitch.proto3.net.impl.auth;

import com.hiddenswitch.proto3.net.amazon.LoginRequest;
import com.hiddenswitch.proto3.net.amazon.LoginResponse;
import com.hiddenswitch.proto3.net.amazon.UserRecord;
import com.hiddenswitch.proto3.net.impl.util.AuthenticationException;
import com.hiddenswitch.proto3.net.util.Result;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;

/**
 * Created by bberman on 1/30/17.
 */
public class TokenAuthProvider extends UsernamePasswordAuthProvider {
	public TokenAuthProvider(Vertx vertx) {
		super(vertx);
	}

	@Override
	public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
		if (authInfo.containsKey("password")
				&& authInfo.containsKey("username")) {
			super.authenticate(authInfo, resultHandler);
		} else if (authInfo.containsKey("token")) {
			authenticateWithToken(authInfo, resultHandler);
		} else {
			resultHandler.handle(Future.failedFuture(new AuthenticationException(null)));
		}
	}

	private void authenticateWithToken(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
		accounts.async((AsyncResult<UserRecord> response) -> {
			if (response.failed()
					|| response.result() == null) {
				resultHandler.handle(Future.failedFuture(response.cause()));
				return;
			}
			resultHandler.handle(Future.succeededFuture(response.result()));
		}).getWithToken(authInfo.getString("token"));
	}
}
