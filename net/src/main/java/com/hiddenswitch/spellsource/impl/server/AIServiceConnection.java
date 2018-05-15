package com.hiddenswitch.spellsource.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Bots;
import com.hiddenswitch.spellsource.common.GameState;
import com.hiddenswitch.spellsource.common.Writer;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.impl.util.ServerGameContext;
import com.hiddenswitch.spellsource.models.*;
import com.hiddenswitch.spellsource.util.RpcClient;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.utils.TurnState;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.Notification;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Handles the marshalling between a {@link Writer} and the {@link Bots} service.
 * <p>
 * This method should typically not be overridden.
 */
public class AIServiceConnection implements Writer {
	final int playerId;
	final WeakReference<ServerGameContext> context;
	static final Logger logger = LoggerFactory.getLogger(AIServiceConnection.class);
	/**
	 * Make the default timeout slightly shorter than the networking timeout
	 */
	final long timeout = RpcClient.DEFAULT_TIMEOUT - 800;

	public AIServiceConnection(ServerGameContext context, EventBus eventBus, int playerId) {

		this.context = new WeakReference<>(context);
		this.playerId = playerId;
	}

	@Override
	public void onNotification(Notification event, GameState gameState) {
	}

	@Override
	@Suspendable
	public void onGameEnd(GameState gameState, Player winner) {
	}

	@Override
	public void onActivePlayer(Player activePlayer) {
	}

	@Override
	public void onTurnEnd(Player activePlayer, int turnNumber, TurnState turnState) {
	}

	@Override
	public void onUpdate(GameState state) {
	}

	@Override
	@Suspendable
	public void onRequestAction(final String messageId, final GameState state, final List<GameAction> actions) {
		ServerGameContext gc = context.get();
		if (gc == null) {
			throw new NullPointerException();
		}
		RequestActionRequest request = new RequestActionRequest(new GameId(gc.getGameId()), playerId, actions, gc.getDeckFormat(), state);

		try {
			RequestActionResponse response = Bots.requestAction(request);
			gc.onActionReceived(messageId, response.gameAction);
		} catch (RuntimeException cause) {
			logger.error("onRequestAction: The AI threw an exception while trying to get an action: ", cause);
			GameAction endTurn;
			for (int i = actions.size() - 1; i >= 0; i--) {
				GameAction action = actions.get(i);
				if (action.getActionType() == ActionType.END_TURN) {
					gc.onActionReceived(messageId, action);
					return;
				}
			}
			gc.onActionReceived(messageId, actions.get(0));
		}
	}

	@Override
	@Suspendable
	public void onMulligan(String messageId, GameState state, List<Card> cards, int playerId) {
		final ServerGameContext gc = context.get();
		if (gc == null) {
			throw new NullPointerException();
		}
		gc.onMulliganReceived(messageId, gc.getPlayer(playerId), Bots.mulligan(new MulliganRequest(cards)).discardedCards);
	}

	@Override
	public void close() {
	}

	@Override
	public Object getPrivateSocket() {
		return this;
	}

	@Override
	public void lastEvent() {
	}

	@Override
	public boolean isOpen() {
		return true;
	}
}
