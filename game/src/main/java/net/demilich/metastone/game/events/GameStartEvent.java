package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.Player;

/**
 * The game has started. Fires once for each player.
 */
public class GameStartEvent extends BasicGameEvent {

	public GameStartEvent(GameContext context, Player player) {
		super(EventTypeEnum.GAME_START, true, context, null, player.getId(), -1);
	}
}
