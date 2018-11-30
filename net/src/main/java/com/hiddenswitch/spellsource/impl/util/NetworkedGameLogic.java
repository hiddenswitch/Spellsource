package com.hiddenswitch.spellsource.impl.util;

import co.paralleluniverse.fibers.Suspendable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxException;
import io.vertx.ext.sync.Sync;
import net.demilich.metastone.game.Player;
import net.demilich.metastone.game.actions.ActionType;
import net.demilich.metastone.game.actions.GameAction;
import net.demilich.metastone.game.behaviour.Behaviour;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.logic.GameLogic;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.hiddenswitch.spellsource.util.Sync.suspendableHandler;

/**
 * Created by bberman on 11/23/16.
 */
public class NetworkedGameLogic extends GameLogic {

	@Override
	@Suspendable
	public GameAction requestAction(Player player, List<GameAction> actions) {
		try {
			return super.requestAction(player, actions);
		} catch (VertxException throwable) {
			if (throwable.getCause() instanceof InterruptedException) {
				logger.error("requestAction {}: Gameplay was interrupted", context.getGameId());
				throw throwable;
			} else {
				logger.error("requestAction {}: An error occurred requesting an action through the game logic, the first action or end turn was chosen", context.getGameId(), throwable);
				return actions.stream().filter(e -> e.getActionType() == ActionType.END_TURN).findFirst().orElse(actions.get(0));
			}
		}
	}
}
