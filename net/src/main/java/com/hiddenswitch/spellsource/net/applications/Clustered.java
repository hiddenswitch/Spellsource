package com.hiddenswitch.spellsource.net.applications;

/**
 * The main entry point of the game server.
 * <p>
 * Starts a clustered service, then tries to migrate the database.
 */
public class Clustered {
	public static void main(String args[]) {
		Applications.startServer(vertx -> {
		});
	}
}

