package com.hiddenswitch.spellsource.impl;

import com.hiddenswitch.spellsource.Accounts;
import com.hiddenswitch.spellsource.models.LoginResponse;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hiddenswitch.spellsource.util.Sync.defer;

public class SpellsourceAuthProvider implements AuthProvider {

	private static Logger LOGGER = LoggerFactory.getLogger(SpellsourceAuthProvider.class);
	private final Accounts.Authorities[] authorities;

	public static SpellsourceAuthProvider create(Accounts.Authorities... authorities) {
		return new SpellsourceAuthProvider(authorities);
	}

	private SpellsourceAuthProvider(Accounts.Authorities... authorities) {
		this.authorities = authorities;
	}

	@Override
	public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
		String email = authInfo.getString("username");
		if (email == null) {
			authInfo.getString("email");
		}
		String password = authInfo.getString("password");

		if (email == null || email.isEmpty()) {
			resultHandler.handle(Future.failedFuture(new NullPointerException("no email")));
			return;
		}

		defer(v -> {
			LoginResponse res;
			try {
				res = Accounts.login(email, password);
			} catch (Throwable any) {
				resultHandler.handle(Future.failedFuture(any));
				return;
			}

			if (res.succeeded()) {
				boolean authorized = res.getRecord().isAuthorized(authorities);
				resultHandler.handle(authorized ? Future.succeededFuture(res.getRecord()) : Future.failedFuture(new SecurityException("not authorized")));
			} else {
				resultHandler.handle(Future.failedFuture(res.isBadEmail() ? "bad email" : (res.isBadPassword() ? "bad password" : "unknown")));
			}
		});
	}
}
