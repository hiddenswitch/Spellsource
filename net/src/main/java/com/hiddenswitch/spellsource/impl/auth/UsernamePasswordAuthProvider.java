package com.hiddenswitch.spellsource.impl.auth;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Accounts;
import com.hiddenswitch.spellsource.models.LoginRequest;
import com.hiddenswitch.spellsource.models.LoginResponse;
import com.hiddenswitch.spellsource.impl.util.AuthenticationException;
import com.hiddenswitch.spellsource.util.Rpc;
import com.hiddenswitch.spellsource.util.RpcClient;
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
	protected RpcClient<Accounts> accounts;

	public UsernamePasswordAuthProvider(Vertx vertx) {
		if (vertx == null) {
			throw new NullPointerException("vertx must not be null.");
		}
		accounts = Rpc.connect(Accounts.class, vertx.eventBus());
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
