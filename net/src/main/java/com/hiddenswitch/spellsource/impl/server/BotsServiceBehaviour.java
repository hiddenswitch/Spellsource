package com.hiddenswitch.spellsource.impl.server;

import co.paralleluniverse.fibers.Suspendable;
import com.hiddenswitch.spellsource.Bots;
import com.hiddenswitch.spellsource.impl.GameId;
import com.hiddenswitch.spellsource.models.MulliganRequest;
import com.hiddenswitch.spellsource.models.RequestActionRequest;
import com.hiddenswitch.spellsource.models.RequestActionResponse;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.UtilityBehaviour;
import net.demilich.metastone.game.cards.Card;
import org.slf4j.Logger;

import java.util.List;

public class BotsServiceBehaviour extends UtilityBehaviour {
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(BotsServiceBehaviour.class);

	@Override
	public String getName() {
		return "Bot";
	}

	@Override
	@Suspendable
	public List<Card> mulligan(GameContext context, Player player, List<Card> cards) {
		return Bots.mulligan(new MulliganRequest(cards)).discardedCards;
	}

	@Override
	@Suspendable
	public GameAction requestAction(GameContext context, Player player, List<GameAction> validActions) {
		RequestActionRequest request = new RequestActionRequest(new GameId(context.getGameId()), player.getId(), validActions, context.getDeckFormat(), context.getGameStateCopy());

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
}
