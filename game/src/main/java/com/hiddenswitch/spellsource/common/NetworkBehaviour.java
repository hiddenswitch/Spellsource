package com.hiddenswitch.spellsource.common;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.AbstractBehaviour;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

/**
 * Represents a behaviour that delegates its requests to a networking interface provided by a {@link GameContext}.
 */
public class NetworkBehaviour extends AbstractBehaviour implements Serializable {
	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(NetworkBehaviour.class);

	public NetworkBehaviour() {
	}

	@Override
	public String getName() {
		return "Network behaviour";
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		logger.debug("Requesting mulligan from wrapped behaviour using blocking behaviour. Player: {}, cards: {}", player, cards);
		return Sync.awaitFiber(done -> context.networkRequestMulligan(player, cards, result -> done.handle(Future.succeededFuture(result))));
	}

	@Override
	@Suspendable
	public void mulliganAsync(GameContext context, Player player, List<Card> cards, Handler<List<Card>> handler) {
		logger.debug("Requesting mulligan from network. Player: {}, cards: {}", player, cards);
		context.networkRequestMulligan(player, cards, handler);
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		logger.debug("Requesting action from network using blocking behaviour.");
		return Sync.awaitFiber(done -> requestActionAsync(context, player, validActions, result -> done.handle(Future.succeededFuture(result))));
	}

	@Suspendable
	@Override
	public void requestActionAsync(GameContext context, Player player, List<GameAction> validActions, Handler<GameAction> handler) {
		logger.debug("Requesting action from network. Player: {}, validActions: {}", player, validActions);
		context.networkRequestAction(context.getGameStateCopy(), player.getId(), validActions, handler);
	}

	@Override
	@Suspendable
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
	}

	@Override
	@Suspendable
	public void onGameOverAuthoritative(GameContext context, int playerId, int winningPlayerId) {
		if (winningPlayerId != -1) {
			context.sendGameOver(context.getPlayer(playerId), context.getPlayer(winningPlayerId));
		} else {
			context.sendGameOver(context.getPlayer(playerId), null);
		}
	}

	@Override
	public boolean isHuman() {
		return true;
	}
}
