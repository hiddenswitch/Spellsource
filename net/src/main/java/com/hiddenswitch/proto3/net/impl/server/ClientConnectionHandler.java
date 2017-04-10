package com.hiddenswitch.proto3.net.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.common.Client;
import net.demilich.metastone.game.Player;

/**
 * Created by bberman on 3/25/17.
 */
public interface ClientConnectionHandler {
	@Suspendable
	void onPlayerConnected(Player player, Client client);

	@Suspendable
	void onPlayerReconnected(Player player, Client client);
}
