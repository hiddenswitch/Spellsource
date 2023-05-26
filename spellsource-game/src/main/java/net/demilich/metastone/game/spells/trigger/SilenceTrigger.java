package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.events.SilenceEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires whenever an {@link Actor} is {@link Attribute#SILENCED}.
 * <p>
 * The {@link SilenceEvent} populates the silenced actor into {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_TARGET}.
 *
 * <table>
 *   <caption>Values at the time of firing</caption>
 *   <tr>
 *     <td>Field</td>
 *     <td>Value</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.targeting.EntityReference#EVENT_SOURCE}</td>
 *     <td>the {@link net.demilich.metastone.game.Player} entity of the player that performed the silencing</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.targeting.EntityReference#EVENT_TARGET}</td>
 *     <td>the target of the silencing</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#TARGET_PLAYER}</td>
 *     <td>the player that performed the increase</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#SOURCE_PLAYER}</td>
 *     <td>-1</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider}</td>
 *     <td>0</td>
 *   </tr>
 * </table>
 */
public class SilenceTrigger extends EventTrigger {

	public SilenceTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.SILENCE;
	}

}
