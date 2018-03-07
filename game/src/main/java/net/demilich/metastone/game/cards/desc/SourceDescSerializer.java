package net.demilich.metastone.game.cards.desc;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.*;

import net.demilich.metastone.game.spells.desc.source.CardSource;
import net.demilich.metastone.game.spells.desc.source.CardSourceArg;
import net.demilich.metastone.game.spells.desc.source.CardSourceDesc;

public class SourceDescSerializer implements JsonDeserializer<CardSourceDesc>, JsonSerializer<CardSourceDesc> {
	@SuppressWarnings("unchecked")
	@Override
	public CardSourceDesc deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		if (!(json instanceof JsonObject)) {
			throw new JsonParseException("SourceDescSerializer parser expected an JsonObject but found " + json + " instead");
		}
		JsonObject jsonData = (JsonObject) json;
		String cardSourceClassName = CardSource.class.getPackage().getName() + "." + jsonData.get("class").getAsString();
		Class<? extends CardSource> cardSourceClass;
		try {
			cardSourceClass = (Class<? extends CardSource>) Class.forName(cardSourceClassName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new JsonParseException("SourceDescSerializer parser encountered an invalid class: " + cardSourceClassName);
		}

		Map<CardSourceArg, Object> arguments = new CardSourceDesc(cardSourceClass);
		parseArgument(CardSourceArg.TARGET_PLAYER, jsonData, arguments, ParseValueType.TARGET_PLAYER);
		parseArgument(CardSourceArg.COLLECTION_NAME, jsonData, arguments, ParseValueType.STRING);
		parseArgument(CardSourceArg.INVERT, jsonData, arguments, ParseValueType.BOOLEAN);
		parseArgument(CardSourceArg.SOURCE, jsonData, arguments, ParseValueType.TARGET_REFERENCE);
		parseArgument(CardSourceArg.DISTINCT, jsonData, arguments, ParseValueType.BOOLEAN);

		return new CardSourceDesc(arguments);
	}

	private void parseArgument(CardSourceArg arg, JsonObject jsonData, Map<CardSourceArg, Object> arguments, ParseValueType valueType) {
		String argName = ParseUtils.toCamelCase(arg.toString());
		if (!jsonData.has(argName)) {
			return;
		}
		Object value = ParseUtils.parse(argName, jsonData, valueType);
		arguments.put(arg, value);
	}

	@Override
	public JsonElement serialize(CardSourceDesc src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject result = new JsonObject();
		result.add("class", new JsonPrimitive(src.getDescClass().getSimpleName()));
		for (CardSourceArg attribute : CardSourceArg.values()) {
			if (attribute == CardSourceArg.CLASS) {
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
