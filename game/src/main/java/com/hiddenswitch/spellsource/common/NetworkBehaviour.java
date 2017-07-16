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

public class NetworkBehaviour extends AbstractBehaviour implements Serializable {
	private static final long serialVersionUID = 1L;

	private Logger logger = LoggerFactory.getLogger(NetworkBehaviour.class);
	private Behaviour wrapBehaviour;

	public NetworkBehaviour(Behaviour wrapBehaviour) {
		this.wrapBehaviour = wrapBehaviour;
	}

	@Override
	public String getName() {
		return getWrapBehaviour().getName();
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		if (isServer()) {
			logger.debug("Requesting mulligan from wrapped behaviour using blocking behaviour. Player: {}, cards: {}", player, cards);
			List<Card> mulliganResults = Sync.awaitFiber(done -> context.networkRequestMulligan(player, cards, result -> done.handle(Future.succeededFuture(result))));
			return mulliganResults;
		} else {
			logger.debug("Requesting mulligan from wrapped behaviour. Player: {}, cards: {}", player, cards);
			return getWrapBehaviour().mulligan(context, player, cards);
		}
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
		if (isServer()) {
			logger.debug("Requesting action from network using blocking behaviour.");
			GameAction action = Sync.awaitFiber(done -> requestActionAsync(context, player, validActions, result -> done.handle(Future.succeededFuture(result))));
			return action;
		} else {
			logger.debug("Requesting action from wrapped behaviour. Player: {}, validActions: {}", player, validActions);
			return getWrapBehaviour().requestAction(context, player, validActions);
		}
	}

	/**
	 * TODO: We're not necessarily in the server if we're running in a fiber, but this is only really relevant in the
	 * strange world that is the Java legacy client.
	 *
	 * @return {@code true} if this is running in the server context.
	 */
	private boolean isServer() {
		return Fiber.isCurrentFiber();
	}

	@Suspendable
	@Override
	public void requestActionAsync(GameContext context, Player player, List<GameAction> validActions, Handler<GameAction> handler) {
		if (isServer()) {
			logger.debug("Requesting action from network. Player: {}, validActions: {}", player, validActions);
			context.networkRequestAction(context.getGameStateCopy(), player.getId(), validActions, handler);
		} else {
			super.requestActionAsync(context, player, validActions, handler);
		}
	}

	@Override
	@Suspendable
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
		getWrapBehaviour().onGameOver(context, playerId, winningPlayerId);
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

	public Behaviour getWrapBehaviour() {
		return wrapBehaviour;
	}
}
