package com.hiddenswitch.spellsource.net.impl;

import com.hiddenswitch.spellsource.net.impl.UserId;
import io.vertx.core.http.ServerWebSocket;

public interface ConnectionContext {
	ServerWebSocket socket();

	UserId user();
}
