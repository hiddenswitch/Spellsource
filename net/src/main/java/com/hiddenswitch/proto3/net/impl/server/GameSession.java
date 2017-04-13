package com.hiddenswitch.proto3.net.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.common.ClientConnectionConfiguration;
import com.hiddenswitch.proto3.net.common.Server;
import com.hiddenswitch.proto3.net.impl.util.ServerGameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;

import java.util.List;

public interface GameSession extends Server {

	boolean isGameReady();

	/**
	 * Returns the information the client needs to know whom to connect to and what message to send.
	 *
	 * @return {ClientConnectionConfiguration} Data for player 1.
	 */
	ClientConnectionConfiguration getConfigurationForPlayer1();

	/**
	 * Returns the information the client needs to know whom to connect to and what message to send.
	 *
	 * @return {ClientConnectionConfiguration} Data for player 2.
	 */
	ClientConnectionConfiguration getConfigurationForPlayer2();

	@Suspendable
	void kill();

	/**
	 * An identifier the matchmaking API can use to identify this particular game session for management tasks, like
	 * cleaning up a game (closing its sockets) or sending in-game messages.
	 *
	 * @return The ID
	 */
	String getGameId();

	long getNoActivityTimeout();

	ServerGameContext getGameContext();

	Player getPlayer(String userId);

	GameAction getActionForMessage(String messageId, int actionIndex);
}
