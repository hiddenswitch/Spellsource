package com.hiddenswitch.spellsource.util;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by bberman on 2/6/17.
 */
public class QuickJson {
	public static JsonObject json(final Object... args) {
		return jsonPut(new JsonObject(), args);
	}

	public static JsonObject jsonPut(JsonObject existing, final Object... args) {
		if (args == null
				|| args.length == 0) {
			return new JsonObject();
		}

		if (args.length == 1) {
			return toJson(args[0]);
		}

		if (args.length % 2 != 0) {
			throw new IllegalArgumentException("Must have an even number of arguments.");
		}

		JsonObject j = existing;

		for (int i = 0; i < args.length; i += 2) {
			j.put((String) args[i], args[i + 1]);
		}

		return j;
	}

	@SuppressWarnings("unchecked")
	public static JsonObject toJson(final Object arg) {
		return new JsonObject(Json.mapper.convertValue(arg, Map.class));
	}

	public static <T> T fromJson(JsonObject json, Class<T> classOfT) {
		if (json == null) {
			return null;
		}
		return Json.mapper.convertValue(json.getMap(), classOfT);
	}

	public static <T> List<T> fromJson(List<JsonObject> jsons, Class<T> elementClass) {
		if (jsons == null) {
			return Collections.emptyList();
		}
		return jsons.stream().map(o -> fromJson(o, elementClass)).collect(Collectors.toList());
	}

	public static <T> List<T> fromJson(JsonArray json, Class<T> listElement) {
		if (json == null) {
			return Collections.emptyList();
		}
		return json.stream().map(o -> {
			JsonObject jo = (JsonObject) o;
			return fromJson(jo, listElement);
		}).collect(Collectors.toList());
	}
}

