package net.demilich.metastone.game.cards.desc;

import net.demilich.metastone.game.spells.desc.trigger.EventTriggerArg;
import net.demilich.metastone.game.spells.desc.trigger.EventTriggerDesc;
import net.demilich.metastone.game.spells.trigger.EventTrigger;

public class EventTriggerDescDeserializer extends DescDeserializer<EventTriggerDesc, EventTriggerArg, EventTrigger> {

	public EventTriggerDescDeserializer() {
		super(EventTriggerDesc.class);
	}

	@Override
	protected EventTriggerDesc createDescInstance() {
		return new EventTriggerDesc();
	}

	@Override
	public void init(SerializationContext ctx) {
		ctx.add(EventTriggerArg.RACE, ParseValueType.STRING);
		ctx.add(EventTriggerArg.CARD_TYPE, ParseValueType.CARD_TYPE);
		ctx.add(EventTriggerArg.TARGET_PLAYER, ParseValueType.TARGET_PLAYER);
		ctx.add(EventTriggerArg.SOURCE_PLAYER, ParseValueType.TARGET_PLAYER);
		ctx.add(EventTriggerArg.SOURCE_ENTITY_TYPE, ParseValueType.ENTITY_TYPE);
		ctx.add(EventTriggerArg.TARGET_ENTITY_TYPE, ParseValueType.ENTITY_TYPE);
		ctx.add(EventTriggerArg.SOURCE_TYPE, ParseValueType.CARD_TYPE);
		ctx.add(EventTriggerArg.ACTION_TYPE, ParseValueType.ACTION_TYPE);
		ctx.add(EventTriggerArg.HOST_TARGET_TYPE, ParseValueType.TARGET_TYPE);
		ctx.add(EventTriggerArg.REQUIRED_ATTRIBUTE, ParseValueType.ATTRIBUTE);
		ctx.add(EventTriggerArg.QUEUE_CONDITION, ParseValueType.CONDITION);
		ctx.add(EventTriggerArg.FIRE_CONDITION, ParseValueType.CONDITION);
		ctx.add(EventTriggerArg.TARGET, ParseValueType.TARGET_REFERENCE);
		ctx.add(EventTriggerArg.VALUE, ParseValueType.VALUE);
	}

	@Override
	protected Class<EventTrigger> getAbstractComponentClass() {
		return EventTrigger.class;
	}

	@Override
	protected Class<EventTriggerArg> getEnumType() {
		return EventTriggerArg.class;
	}
}
