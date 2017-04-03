package com.hiddenswitch.proto3.net.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.proto3.net.Bots;
import com.hiddenswitch.proto3.net.common.GameState;
import com.hiddenswitch.proto3.net.common.Client;
import com.hiddenswitch.proto3.net.impl.util.ServerGameContext;
import com.hiddenswitch.proto3.net.models.*;
import com.hiddenswitch.proto3.net.util.Broker;
import com.hiddenswitch.proto3.net.util.ServiceProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.TurnState;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.events.GameEvent;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by bberman on 12/9/16.
 */
public class AIServiceConnection implements Client {
	final int playerId;
	final ServiceProxy<Bots> bots;
	final WeakReference<ServerGameContext> context;

	public AIServiceConnection(ServerGameContext context, EventBus eventBus, int playerId) {
		this.bots = Broker.proxy(Bots.class, eventBus);

		this.context = new WeakReference<>(context);
		this.playerId = playerId;
	}

	@Override
	public void onGameEvent(GameEvent event) {
	}

	@Override
	public void onGameEnd(Player winner) {
		final ServerGameContext gc = context.get();
		bots.async((AsyncResult<NotifyGameOverResponse> result) -> {
			// Do nothing
		}).notifyGameOver(new NotifyGameOverRequest(gc.getGameId()));
	}

	@Override
	public void setPlayers(Player localPlayer, Player remotePlayer) {
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
		final ServerGameContext gc = context.get();
		bots.async((AsyncResult<RequestActionResponse> result) -> {
			if (gc == null) {
				return;
			}
			if (result.result() == null) {
				throw new NullPointerException("A bot did not reply with an action. GameContext: \n" + gc.toLongString() + "\nActions:\n" + actions.toString());
			} else {
				gc.onActionReceived(messageId, result.result().gameAction);
			}
		}).requestAction(new RequestActionRequest(state, playerId, actions, gc.getDeckFormat()));
	}

	@Override
	@Suspendable
	public void onMulligan(String messageId, Player player, List<Card> cards) {
		final ServerGameContext gc = context.get();
		bots.async((AsyncResult<MulliganResponse> result) -> {
			if (gc == null) {
				return;
			}
			gc.onMulliganReceived(messageId, player, result.result().discardedCards);
		}).mulligan(new MulliganRequest(cards));
	}
}
