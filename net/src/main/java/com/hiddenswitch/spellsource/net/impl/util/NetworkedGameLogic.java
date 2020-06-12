package com.hiddenswitch.spellsource.net.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.base.Throwables;
import net.demilich.metastone.game.Player;
import com.hiddenswitch.spellsource.client.models.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.logic.GameLogic;

import java.util.List;

import static com.hiddenswitch.spellsource.net.impl.Sync.fiber;

/**
 * Handles interrupting a request action.
 */
public class NetworkedGameLogic extends GameLogic {

	@Override
	@Suspendable
	public GameAction requestAction(Player player, List<GameAction> actions) {
		try {
			return super.requestAction(player, actions);
		} catch (Throwable baseThrowable) {
			Throwable throwable = Throwables.getRootCause(baseThrowable);
			if (throwable instanceof InterruptedException) {
				LOGGER.error("requestAction {}: Gameplay was interrupted", context.getGameId());
				throw baseThrowable;
			} else {
				LOGGER.error("requestAction {}: An error occurred requesting an action through the game logic, the first action or end turn was chosen", context.getGameId(), throwable);
				return actions.stream().filter(e -> e.getActionType() == ActionType.END_TURN).findFirst().orElse(actions.get(0));
			}
		}
	}
}
