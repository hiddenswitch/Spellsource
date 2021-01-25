package net.demilich.metastone.game.events;

import net.demilich.metastone.game.GameContext;

/**
 * Just before the game starts. Mulligans have occurred.
 */
public class PreGameStartEvent extends BasicGameEvent {

	public PreGameStartEvent(GameContext context, int playerId) {
		super(com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType.PRE_GAME_START, context, playerId, -1);
	}
}
