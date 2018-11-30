package com.hiddenswitch.spellsource.util;

import java.util.concurrent.Future;

public interface TestWebsocket {
	void setMessageHandler(TestWebsocket.MessageHandler msgHandler);

	void sendMessage(String message);

	void close();

	boolean isOpen();

	TestWebsocket setCloseHandler(Runnable closeHandler);

	@FunctionalInterface
	interface MessageHandler {
		void handleMessage(String message);
	}
}
