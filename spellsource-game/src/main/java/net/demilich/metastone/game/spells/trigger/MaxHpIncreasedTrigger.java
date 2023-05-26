package net.demilich.metastone.game.spells.trigger;

import com.hiddenswitch.spellsource.rpc.Spellsource.GameEventTypeMessage.GameEventType;
import net.demilich.metastone.game.entities.Actor;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.events.GameEvent;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires whenever the matching entity's {@link net.demilich.metastone.game.cards.Attribute#MAX_HP} is increased via the
 * {@link net.demilich.metastone.game.logic.GameLogic#setHpAndMaxHp(Actor, int)} effect.
 * <p>
 * Does not trigger off of {@link net.demilich.metastone.game.spells.ModifyAttributeSpell}.
 * <table>
 *   <caption>Values at the time of firing</caption>
 *   <tr>
 *     <td>Field</td>
 *     <td>Value</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.targeting.EntityReference#EVENT_SOURCE}</td>
 *     <td>{@code null}</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.targeting.EntityReference#EVENT_TARGET}</td>
 *     <td>the target of the increase</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#TARGET_PLAYER}</td>
 *     <td>the owner of the target</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg#SOURCE_PLAYER}</td>
 *     <td>the player that performed the increase</td>
 *   </tr>
 *   <tr>
 *     <td>{@link net.demilich.metastone.game.spells.desc.valueprovider.EventValueProvider}</td>
 *     <td>the amount {@link net.demilich.metastone.game.cards.Attribute#MAX_HP} was increased</td>
 *   </tr>
 * </table>
 * @see net.demilich.metastone.game.events.MaxHpIncreasedEvent
 */
public final class MaxHpIncreasedTrigger extends EventTrigger {

	public MaxHpIncreasedTrigger(EventTriggerDesc desc) {
		super(desc);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Enchantment enchantment, Entity host) {
		return true;
	}

	@Override
	public GameEventType interestedIn() {
		return GameEventType.MAX_HP_INCREASED;
	}
}
