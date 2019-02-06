package com.hiddenswitch.spellsource.util;

import com.hiddenswitch.spellsource.impl.UserId;
import io.vertx.core.http.ServerWebSocket;

public interface ConnectionContext {
	ServerWebSocket socket();

	UserId user();
}
