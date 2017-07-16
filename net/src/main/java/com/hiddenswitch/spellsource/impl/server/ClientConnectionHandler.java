package com.hiddenswitch.spellsource.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.Client;
import net.demilich.metastone.game.Player;

/**
 * A delegate that needs to know when players have connected, and with which {@link Client}.
 */
public interface ClientConnectionHandler {
	@Suspendable
	void onPlayerConnected(Player player, Client client);

	@Suspendable
	void onPlayerReconnected(Player player, Client client);
}
