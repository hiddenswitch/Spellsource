package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.common.NetworkBehaviour;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.logic.GameLogic;

import java.util.Collections;
import java.util.List;

import static io.vertx.ext.sync.Sync.fiberHandler;

/**
 * Created by bberman on 11/23/16.
 */
public class GameLogicAsync extends GameLogic {
	private boolean mulliganEnabled = true;

	@Override
	@Suspendable
	protected List<Card> mulligan(Player player, boolean begins) throws UnsupportedOperationException {
		return Sync.awaitFiber(r -> mulliganAsync(player, begins, r1 -> r.handle(Future.succeededFuture(r1))));
	}

	@Override
	@Suspendable
	protected void mulliganAsync(Player player, boolean begins, Handler<List<Card>> callback) {
		FirstHand firstHand = new FirstHand(player, begins).invoke();

		Behaviour networkBehaviour = context.getBehaviours().get(player.getId());

		if (mulliganEnabled) {
			networkBehaviour.mulliganAsync(context, player, firstHand.getStarterCards(), (List<Card> discardedCards) -> {
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

		mulliganAsync(player, begins, fiberHandler(o -> {
			startGameForPlayer(player);
			callback.handle(o);
		}));
	}

	public boolean isMulliganEnabled() {
		return mulliganEnabled;
	}

	public void setMulliganEnabled(boolean mulliganEnabled) {
		this.mulliganEnabled = mulliganEnabled;
	}
}
