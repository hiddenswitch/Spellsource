package com.hiddenswitch.proto3.net.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import net.demilich.metastone.game.Player;

/**
 * Created by bberman on 3/25/17.
 */
public interface ClientConnectionHandler {
	@Suspendable
	void onPlayerConnected(Player player, ServerClientConnection client);

	@Suspendable
	void onPlayerReconnected(Player player, ServerClientConnection client);
}
