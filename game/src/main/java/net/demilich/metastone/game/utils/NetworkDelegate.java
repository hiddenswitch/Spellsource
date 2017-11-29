package net.demilich.metastone.game.utils;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.common.GameplayRequest;
import io.vertx.core.Handler;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.util.List;

public interface NetworkDelegate {
	/**
	 * Makes a request over the network for a game action. Unsupported in this game context.
	 *
	 * @param state    The game state to send.
	 * @param playerId The player ID to request from.
	 * @param actions  The valid actions to choose from.
	 * @param callback A handler for the response.
	 */
	@Suspendable
	void networkRequestAction(GameState state, int playerId, List<GameAction> actions, Handler<GameAction> callback);

	/**
	 * If possible, makes a request over the network for which cards to mulligan. Unsupported in this game context.
	 *
	 * @param player       The player to request from.
	 * @param starterCards The cards the player started with.
	 * @param callback     A handler for the response.
	 */
	void networkRequestMulligan(Player player, List<Card> starterCards, Handler<List<Card>> callback);

	/**
	 * Notifies the recipient player that the {@code winner} player won the game.
	 *
	 * @param recipient The player to notify.
	 * @param winner    The winner.
	 */
	void sendGameOver(Player recipient, Player winner);
}
