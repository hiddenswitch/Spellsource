package com.hiddenswitch.spellsource.net.applications;

import com.hiddenswitch.spellsource.net.Broadcaster;

/**
 * The main entry point of a local game server.
 * <p>
 * Starts a local services cluster, tries to migrate and broadcasts over UDP to Unity3D-based clients the IP address of
 * this server once it's ready to be connected to.
 */
public class LocalClustered {
	public static void main(String[] args) {
		Applications.startServer(vertx -> vertx.result().deployVerticle(Broadcaster.create(), done -> {
			Applications.LOGGER.info("main: Broadcaster deployed");
		}));
	}
}
