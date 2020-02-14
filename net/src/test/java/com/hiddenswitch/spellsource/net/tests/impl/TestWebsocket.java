package com.hiddenswitch.spellsource.net.tests.impl;

import io.vertx.core.Handler;

public interface TestWebsocket {
	void setMessageHandler(Handler<String> msgHandler);

	void sendMessage(String message);

	void close();

	boolean isOpen();

	TestWebsocket setCloseHandler(Runnable closeHandler);
}
