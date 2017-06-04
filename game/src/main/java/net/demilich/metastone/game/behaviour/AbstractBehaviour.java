package net.demilich.metastone.game.behaviour;

import java.io.Serializable;
import java.util.List;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Handler;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

public abstract class AbstractBehaviour implements Behaviour, Serializable {
	public Behaviour clone() {
		try {
			return (Behaviour) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	@Suspendable
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
	}

	@Override
	@Suspendable
	public void mulliganAsync(GameContext context, Player player, List<Card> cards, Handler<List<Card>> handler) {
		final List<Card> mulligan = mulligan(context, player, cards);
		if (handler != null) {
			handler.handle(mulligan);
		}
	}

	@Override
	@Suspendable
	public void requestActionAsync(GameContext context, Player player, List<GameAction> validActions, Handler<GameAction> handler) {
		GameAction action = requestAction(context, player, validActions);
		if (handler != null) {
			handler.handle(action);
		}
	}

	@Override
	@Suspendable
	public void onGameOverAuthoritative(GameContext context, int playerId, int winningPlayerId) {
	}
}
