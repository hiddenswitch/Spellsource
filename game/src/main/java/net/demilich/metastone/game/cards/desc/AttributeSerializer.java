package net.demilich.metastone.game.cards.desc;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.*;

import net.demilich.metastone.game.utils.Attribute;
import net.demilich.metastone.game.utils.AttributeMap;

public class AttributeSerializer implements JsonDeserializer<AttributeMap>, JsonSerializer<AttributeMap> {

	@Override
	public AttributeMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		AttributeMap map = new AttributeMap();

		for (Map.Entry<String, JsonElement> element : json.getAsJsonObject().entrySet()) {
			JsonElement primitive = element.getValue().getAsJsonPrimitive();
			map.textSet(Attribute.valueOf(element.getKey()), primitive);
		}

		return map;
	}

	private void parseAttribute(Attribute attribute, JsonObject jsonData, Map<Attribute, Object> map, ParseValueType valueType) {
		String argName = attribute.toString();
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		Boolean bool = value instanceof Boolean ? (Boolean) value : null;
		if (bool != null && bool) {
			value = 1;
		}
		map.put(attribute, value);
	}

	@Override
	public JsonElement serialize(AttributeMap src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		for (Attribute attribute : Attribute.values()) {
			if (!src.containsKey(attribute)) {
				continue;
			}
			String argName = attribute.toString();
			Object value = src.get(attribute);
			switch (attribute) {
				case BATTLECRY:
				case DEATHRATTLES:
					value = true;
				default:
					break;
			}
			result.add(argName, context.serialize(value));
		}
		return result;
	}
}
