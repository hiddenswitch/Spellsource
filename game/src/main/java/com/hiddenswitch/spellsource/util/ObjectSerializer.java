package com.hiddenswitch.spellsource.util;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;

public class ObjectSerializer<T extends Serializable> implements JsonSerializer<T>, JsonDeserializer<T> {

	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (json.isJsonNull()) {
			return null;
		}
		JsonObject object = json.getAsJsonObject();
		final String javaSerialized = object.get("javaSerialized").getAsString();
		return Serialization.deserializeBase64(javaSerialized);
	}

	@Override
	public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
		if (src == null) {
			return JsonNull.INSTANCE;
		}
		String s = Serialization.serializeBase64(src);
		JsonObject object = new JsonObject();
		object.addProperty("javaSerialized", s);
		return object;
	}

}
