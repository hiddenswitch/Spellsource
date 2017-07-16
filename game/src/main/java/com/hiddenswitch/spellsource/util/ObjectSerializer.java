package com.hiddenswitch.spellsource.util;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Base64;

public class ObjectSerializer<T extends Serializable> implements JsonSerializer<T>, JsonDeserializer<T> {

	@Override
	@SuppressWarnings("unchecked")
	public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		if (json.isJsonNull()) {
			return null;
		}
		JsonObject object = json.getAsJsonObject();
		final String javaSerialized = object.get("javaSerialized").getAsString();
		return deserializeBase64(javaSerialized);
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserializeBase64(String javaSerialized) {
		byte bytes[] = Base64.getDecoder().decode(javaSerialized);

		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		ObjectInputStream objectInputStream = null;
		T returnContext;
		try {
			objectInputStream = new ObjectInputStream(inputStream);
			returnContext = (T) objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			returnContext = null;
		}
		return returnContext;
	}

	@Override
	public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
		if (src == null) {
			return JsonNull.INSTANCE;
		}
		String s = null;
		try {
			s = serializeBase64(src);
		} catch (IOException e) {
			return JsonNull.INSTANCE;
		}
		JsonObject object = new JsonObject();
		object.addProperty("javaSerialized", s);
		return object;
	}

	public static <T> String serializeBase64(T src) throws IOException {
		String s;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ObjectOutputStream o = new ObjectOutputStream(output);
		o.writeObject(src);
		o.flush();
		s = Base64.getEncoder().encodeToString(output.toByteArray());
		return s;
	}
}
