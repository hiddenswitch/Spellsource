package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.TargetPlayer;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

import java.util.Map;

/**
 * Fires at the start of the game.
 * <p>
 * Use to implement "Start of Game" effects.
 * <p>
 * The player whose game is starting is the {@code targetPlayer}.
 */
public class GameStartTrigger extends EventTrigger {
	public static EventTriggerDesc create(TargetPlayer targetPlayer) {
		Map<EventTriggerArg, Object> arguments = new EventTriggerDesc(GameStartTrigger.class);
		arguments.put(EventTriggerArg.TARGET_PLAYER, targetPlayer);
		return new EventTriggerDesc(arguments);
	}

	public GameStartTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.GAME_START;
	}

}
