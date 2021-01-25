package net.demilich.metastone.game.events;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.GameContext;
import net.demilich.metastone.game.entities.heroes.Hero;

/**
 * An attempt to draw from an empty deck occurred and the drawing player took damage.
 * <p>
 * The {@link Hero} is the target.
 */
public final class FatigueEvent extends ValueEvent {


	public FatigueEvent(GameContext context, int targetPlayerId, int amount) {
		super(GameEventType.FATIGUE, true, context, targetPlayerId, -1, context.getPlayer(targetPlayerId).getHero(), amount);
	}

	@Override
	public String getDescription(GameContext context, int playerId) {
		return String.format("%s took %s fatigue damage", context.getPlayer(playerId).getName(), getValue());
	}
}
