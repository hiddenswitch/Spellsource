package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource;
import net.demilich.metastone.game.GameContext;

/**
 * Just as the game starts, before mulligans
 */
public class GameInitializedEvent extends BasicGameEvent {

	public GameInitializedEvent(GameContext context, int targetPlayerId, int sourcePlayerId) {
		super(Spellsource.GameEventTypeMessage.GameEventType.GAME_INITIALIZED, context, targetPlayerId, sourcePlayerId);
	}

}
