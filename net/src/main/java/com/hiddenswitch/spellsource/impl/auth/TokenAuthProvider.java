package com.hiddenswitch.spellsource.impl.auth;

import com.hiddenswitch.spellsource.impl.util.UserRecord;
import com.hiddenswitch.spellsource.impl.util.AuthenticationException;
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
