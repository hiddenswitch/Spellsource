package net.demilich.metastone.game.spells.trigger;

import net.demilich.metastone.game.cards.CardType;
import net.demilich.metastone.game.entities.Entity;
import net.demilich.metastone.game.entities.EntityType;
import net.demilich.metastone.game.events.AfterSpellCastedEvent;
import net.demilich.metastone.game.events.GameEvent;
import com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum;;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;

/**
 * Fires after the {@link EventTriggerArg#TARGET_PLAYER} casts a spell.
 * <p>
 * Permits all the constraints on the card being cast from {@link AbstractCardTrigger}.
 * <p>
 * The {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_TARGET} will be set to the <b>selected
 * target</b> of the spell, if it takes a target. The {@link net.demilich.metastone.game.targeting.EntityReference#EVENT_SOURCE}
 * is the <b>card</b> that was played. This is different than other triggers.
 * <p>
 * If this trigger is on the card that it intends to target, it will be in the graveyard and therefore any enchantment
 * attached to it will not be active. To do something when the spell card this trigger is written on is cast, use {@link
 * SpellCastedTrigger}.
 *
 * @see AfterSpellCastedEvent
 */
public class AfterSpellCastedTrigger extends AbstractCardTrigger {

	public AfterSpellCastedTrigger(EventTriggerDesc desc) {
		super(desc);
		EventTriggerDesc clone = desc.clone();
		clone.put(EventTriggerArg.CARD_TYPE, CardType.SPELL);
		setDesc(clone);
	}

	@Override
	protected boolean innerQueues(GameEvent event, Entity host) {
		AfterSpellCastedEvent spellCastedEvent = (AfterSpellCastedEvent) event;

		EntityType targetEntityType = (EntityType) getDesc().get(EventTriggerArg.TARGET_ENTITY_TYPE);
		if (targetEntityType != null
				&& (spellCastedEvent.getEventTarget() == null || targetEntityType != spellCastedEvent.getEventTarget().getEntityType())) {
			return false;
		}

		return true;
	}

	@Override
	public com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum interestedIn() {
		return com.hiddenswitch.spellsource.client.models.GameEvent.EventTypeEnum.AFTER_SPELL_CASTED;
	}

}
