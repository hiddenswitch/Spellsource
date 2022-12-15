package net.demilich.metastone.game.behaviour;

import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

/**
 * A base class for behaviours that implement no action or a default action when its methods are called.
 */
public abstract class AbstractBehaviour implements Behaviour, Serializable {
	public AbstractBehaviour clone() {
		try {
			return (AbstractBehaviour) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onGameOver(GameContext context, int playerId, int winningPlayerId) {
	}

	@Override
	public void mulliganAsync(GameContext context, Player player, List<Card> cards, Consumer<List<Card>> handler) {
		final List<Card> mulligan = mulligan(context, player, cards);
		if (handler != null) {
			handler.accept(mulligan);
		}
	}

	@Override
	public void requestActionAsync(GameContext context, Player player, List<GameAction> validActions, Consumer<GameAction> callback) {
		GameAction action = requestAction(context, player, validActions);
		if (callback != null) {
			callback.accept(action);
		}
	}

	@Override
	public boolean isHuman() {
		return false;
	}
}

