package com.hiddenswitch.spellsource.net.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilities for working with JSON.
 */
public interface QuickJson {

	@SafeVarargs
	static <T> JsonArray array(final T... args) {
		JsonArray arr = new JsonArray();
		for (T arg : args) {
			// Insert photo of Pink Wojack here going "aaaaaaAAAAAAAHHHHH"
			if (arg.getClass().isPrimitive()
					|| arg.getClass().isAssignableFrom(String.class)) {
				arr.add(arg);
			} else {
				arr.add(JsonObject.mapFrom(arg));
			}
		}
		return arr;
	}

	static JsonObject json(final Object... args) {
		return jsonPut(new JsonObject(), args);
	}

	static JsonObject jsonPut(JsonObject existing, final Object... args) {
		if (args == null
				|| args.length == 0) {
			return new JsonObject();
		}

		if (args.length == 1) {
			if (args[0] instanceof JsonObject) {
				return (JsonObject) args[0];
			}
			return JsonObject.mapFrom(args[0]);
		}

		if (args.length % 2 != 0) {
			throw new IllegalArgumentException("Must have an even number of arguments.");
		}

		JsonObject j = existing;

		for (int i = 0; i < args.length; i += 2) {
			Object value = args[i + 1];
			if (value != null && value.getClass().isEnum()) {
				// Serialize as a string when converting an enum value to a JSON object.
				value = value.toString();
			}
			j.put((String) args[i], value);
		}

		return j;
	}

	static <T> T fromJson(JsonObject json, Class<T> classOfT) {
		if (json == null) {
			return null;
		}
		return DatabindCodec.mapper().convertValue(json.getMap(), classOfT);
	}

	static <T> List<T> fromJson(List<JsonObject> jsons, Class<T> elementClass) {
		if (jsons == null) {
			return Collections.emptyList();
		}
		return jsons.stream().map(o -> fromJson(o, elementClass)).collect(Collectors.toList());
	}

	static <T> List<T> fromJson(JsonArray json, Class<T> listElement) {
		if (json == null) {
			return Collections.emptyList();
		}
		return json.stream().map(o -> {
			JsonObject jo = (JsonObject) o;
			return fromJson(jo, listElement);
		}).collect(Collectors.toList());
	}

}

