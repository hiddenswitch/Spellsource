package com.hiddenswitch.spellsource.util;

public interface LocalMongo {
	static LocalMongo create() {
		return null;
	}

	String getUrl();

	void start();

	void stop() throws Exception;

	int getPort();
}
