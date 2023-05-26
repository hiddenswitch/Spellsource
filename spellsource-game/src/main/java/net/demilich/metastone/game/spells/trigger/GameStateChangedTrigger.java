package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires for <b>all</b> events.
 * <p>
 * Use sparingly.
 *
 * <table>
 *   <caption>Values at the time of firing</caption>
 *   <tr>
 *     <td>Field</td>
 *     <td>Value</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.targeting.EntityReference#EVENT_SOURCE}</td>
 *     <td>The source of the event, or {@code null} if there is none</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.targeting.EntityReference#EVENT_TARGET}</td>
 *     <td>The target of the event, or {@code null} if there is none</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#TARGET_PLAYER}</td>
 *     <td>The target player of the event, typically the owner of the {@code target}, or -1 if there was no target or
 *     target player (atypical)</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#SOURCE_PLAYER}</td>
 *     <td>The source player of the event, typically the owner of the {@code source}, or -1 if there was no source
 *     or source player (pretty typical)</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider}</td>
 *     <td>The value of the event, or {@code 0} if there is none.</td>
 *   </tr>
 * </table>
 */
public class GameStateChangedTrigger extends EventTrigger {

	public GameStateChangedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.ALL;
	}
}
