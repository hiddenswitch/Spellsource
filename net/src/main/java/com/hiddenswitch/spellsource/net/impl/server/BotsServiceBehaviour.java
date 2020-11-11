package com.hiddenswitch.spellsource.net.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.net.Bots;
import com.hiddenswitch.spellsource.net.impl.GameId;
import com.hiddenswitch.spellsource.net.impl.util.ServerGameContext;
import com.hiddenswitch.spellsource.net.models.BotMulliganRequest;
import com.hiddenswitch.spellsource.net.models.RequestActionRequest;
import com.hiddenswitch.spellsource.net.models.RequestActionResponse;
import io.opentracing.SpanContext;
import io.vertx.core.*;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.UtilityBehaviour;
import net.demilich.metastone.game.cards.Card;
import org.slf4j.Logger;

import java.util.List;

public class BotsServiceBehaviour extends UtilityBehaviour implements Closeable {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(BotsServiceBehaviour.class);
	private final GameId gameId;

	public BotsServiceBehaviour(GameId gameId) {
		this.gameId = gameId;
	}


	@Override
	public String getName() {
		return "Bot";
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		return Bots.mulligan(new BotMulliganRequest(cards)).discardedCards;
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		SpanContext spanContext = null;
		if (context instanceof ServerGameContext) {
			spanContext = ((ServerGameContext) context).getSpanContext();
		}
		RequestActionRequest request = new RequestActionRequest(new GameId(context.getGameId()), player.getId(), validActions, context.getDeckFormat(), context.getGameStateCopy(), spanContext);

		try {
			RequestActionResponse response = Bots.requestAction(request);
			return response.gameAction;
		} catch (Throwable cause) {
			logger.error("requestAction: The AI threw an exception while trying to get an action: ", cause);
			for (int i = validActions.size() - 1; i >= 0; i--) {
				GameAction action = validActions.get(i);
				if (action.getActionType() == ActionType.END_TURN) {
					return action;
				}
			}
			return validActions.get(0);
		}
	}

	@Override
	public boolean isHuman() {
		return false;
	}

	@Override
	public void close(Promise<Void> completionHandler) {
		// Remove the index plan entry
		Bots.removeIndex(gameId).future().onComplete(h -> {
			if (h.succeeded()) {
				completionHandler.handle(Future.succeededFuture());
			} else {
				completionHandler.handle(Future.failedFuture(h.cause()));
			}
		});
	}
}
