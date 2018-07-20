package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.logic.GameLogic;

import java.util.Collections;
import java.util.List;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

/**
 * Created by bberman on 11/23/16.
 */
public class NetworkedGameLogic extends GameLogic {
	private boolean mulliganEnabled = true;

	@Override
	@Suspendable
	protected List<Card> mulligan(Player player, boolean begins) {
		return Sync.awaitFiber(r -> mulliganAsync(player, begins, r1 -> r.handle(Future.succeededFuture(r1))));
	}

	@Override
	@Suspendable
	protected void mulliganAsync(Player player, boolean begins, Handler<List<Card>> callback) {
		FirstHand firstHand = new FirstHand(player, begins).invoke();

		Behaviour behaviour = context.getBehaviours().get(player.getId());

		if (isMulliganEnabled()) {
			behaviour.mulliganAsync(context, player, firstHand.getStarterCards(), (List<Card> discardedCards) -> {
				logger.debug("Discarded cards from {}: {}", player.getName(), discardedCards.stream().map(Card::toString).reduce((a, b) -> a + ", " + b));
				handleMulligan(player, begins, firstHand, discardedCards);
				callback.handle(discardedCards);
			});
		} else {
			handleMulligan(player, begins, firstHand, Collections.emptyList());
			callback.handle(Collections.emptyList());
		}
	}

	@Override
	@Suspendable
	public List<Card> init(int playerId, boolean begins) throws UnsupportedOperationException {
		return Sync.awaitFiber(r -> initAsync(playerId, begins, r1 -> r.handle(Future.succeededFuture(r1))));
	}

	@Override
	@Suspendable
	public void initAsync(int playerId, boolean begins, Handler<List<Card>> callback) {
		Player player = context.getPlayer(playerId);

		mulliganAsync(player, begins, suspendableHandler(callback::handle));
	}

	public boolean isMulliganEnabled() {
		return mulliganEnabled;
	}

	public void setMulliganEnabled(boolean mulliganEnabled) {
		this.mulliganEnabled = mulliganEnabled;
	}

	@Override
	@Suspendable
	public GameAction requestAction(Player player, List<GameAction> actions) {
		try {
			return super.requestAction(player, actions);
		} catch (Throwable throwable) {
			logger.error("requestAction {}: An error occurred requesting an action through the game logic, the first action or end turn was chosen", context.getGameId(), throwable);
			return actions.stream().filter(e -> e.getActionType() == ActionType.END_TURN).findFirst().orElse(actions.get(0));
		}
	}
}
