package com.hiddenswitch.spellsource;

import com.hiddenswitch.spellsource.impl.SpellsourceAuthProvider;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.AuthOptions;
import io.vertx.ext.auth.AuthProvider;

public interface Diagnostics {

	class SpellsourceAuthOptions implements AuthOptions {
		@Override
		public AuthOptions clone() {
			return new SpellsourceAuthOptions();
		}

		@Override
		public AuthProvider createProvider(Vertx vertx) {
			return SpellsourceAuthProvider.create(Accounts.Authorities.ADMINISTRATIVE);
		}
	}
}
