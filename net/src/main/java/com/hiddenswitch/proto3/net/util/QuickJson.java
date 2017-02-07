package com.hiddenswitch.proto3.net.util;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * Created by bberman on 2/6/17.
 */
public class QuickJson {
	public static JsonObject json(final Object... args) {
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

		JsonObject j = new JsonObject();

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
		return Json.mapper.convertValue(json.getMap(), classOfT);
	}
}
