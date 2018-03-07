package net.demilich.metastone.game.spells.desc.trigger;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.*;

import net.demilich.metastone.game.cards.desc.ParseUtils;
import net.demilich.metastone.game.cards.desc.ParseValueType;
import net.demilich.metastone.game.spells.trigger.EventTrigger;

public class EventTriggerDescSerializer implements JsonDeserializer<EventTriggerDesc>, JsonSerializer<EventTriggerDesc> {

	@SuppressWarnings("unchecked")
	@Override
	public EventTriggerDesc deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException("Trigger parser expected an JsonObject but found " + json + " instead");
		}
		JsonObject jsonData = (JsonObject) json;
		String triggerClassName = EventTrigger.class.getPackage().getName() + "." + jsonData.get("class").getAsString();
		Class<? extends EventTrigger> triggerClass;
		try {
			triggerClass = (Class<? extends EventTrigger>) Class.forName(triggerClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new JsonParseException("Trigger parser encountered an invalid class: " + triggerClassName);
		}
		Map<EventTriggerArg, Object> arguments = new EventTriggerDesc(triggerClass);
		parseArgument(EventTriggerArg.RACE, jsonData, arguments, ParseValueType.RACE);
		parseArgument(EventTriggerArg.CARD_TYPE, jsonData, arguments, ParseValueType.CARD_TYPE);
		parseArgument(EventTriggerArg.TARGET_PLAYER, jsonData, arguments, ParseValueType.TARGET_PLAYER);
		parseArgument(EventTriggerArg.SOURCE_PLAYER, jsonData, arguments, ParseValueType.TARGET_PLAYER);
		parseArgument(EventTriggerArg.SOURCE_ENTITY_TYPE, jsonData, arguments, ParseValueType.ENTITY_TYPE);
		parseArgument(EventTriggerArg.TARGET_ENTITY_TYPE, jsonData, arguments, ParseValueType.ENTITY_TYPE);
		parseArgument(EventTriggerArg.SOURCE_TYPE, jsonData, arguments, ParseValueType.CARD_TYPE);
		parseArgument(EventTriggerArg.ACTION_TYPE, jsonData, arguments, ParseValueType.ACTION_TYPE);
		parseArgument(EventTriggerArg.HOST_TARGET_TYPE, jsonData, arguments, ParseValueType.TARGET_TYPE);
		parseArgument(EventTriggerArg.REQUIRED_ATTRIBUTE, jsonData, arguments, ParseValueType.ATTRIBUTE);
		parseArgument(EventTriggerArg.QUEUE_CONDITION, jsonData, arguments, ParseValueType.CONDITION);
		parseArgument(EventTriggerArg.FIRE_CONDITION, jsonData, arguments, ParseValueType.CONDITION);
		parseArgument(EventTriggerArg.TARGET, jsonData, arguments, ParseValueType.TARGET_REFERENCE);
		parseArgument(EventTriggerArg.VALUE, jsonData, arguments, ParseValueType.VALUE);

		return new EventTriggerDesc(arguments);
	}

	private void parseArgument(EventTriggerArg arg, JsonObject jsonData, Map<EventTriggerArg, Object> arguments, ParseValueType valueType) {
		String argName = ParseUtils.toCamelCase(arg.toString());
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		arguments.put(arg, value);
	}

	@Override
	public JsonElement serialize(EventTriggerDesc src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		result.add("class", new JsonPrimitive(src.getDescClass().getSimpleName()));
		for (EventTriggerArg attribute : EventTriggerArg.values()) {
			if (attribute == EventTriggerArg.CLASS) {
				continue;
			}
			if (!src.containsKey(attribute)) {
				continue;
			}
			String argName = ParseUtils.toCamelCase(attribute.toString());
			result.add(argName, context.serialize(src.get(attribute)));
		}
		return result;
	}
}
