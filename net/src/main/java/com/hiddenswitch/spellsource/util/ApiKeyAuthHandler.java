package com.hiddenswitch.spellsource.util;

import com.hiddenswitch.spellsource.impl.auth.ApiKeyAuthHandlerImpl;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.handler.AuthHandler;

/**
 * Created by bberman on 2/17/17.
 */
public interface ApiKeyAuthHandler extends AuthHandler {
	static ApiKeyAuthHandler create(AuthProvider authProvider) {
		return new ApiKeyAuthHandlerImpl(authProvider, null);
	}

	static ApiKeyAuthHandler create(AuthProvider authProvider, String headerForToken) {
		return new ApiKeyAuthHandlerImpl(authProvider, null).setHeader(headerForToken);
	}

	ApiKeyAuthHandlerImpl setHeader(String header);

	CharSequence getHeader();
}
