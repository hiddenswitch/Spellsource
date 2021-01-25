package com.hiddenswitch.spellsource.util;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.io.*;

/**
 * Provides utilities for serializing objects, especially Java objects.
 */
public class Serialization {

	/**
	 * Serializes the object to a String using JSON.
	 *
	 * @param object
	 * @return
	 */
	public static String serialize(Object object) {
		return Json.encode(object);
	}

	/**
	 * Deserializes the specified JSON into the specified class.
	 *
	 * @param json
	 * @param classOfT
	 * @param <T>
	 * @return
	 */
	public static <T> T deserialize(String json, Class<T> classOfT) {
		return Json.decodeValue(json, classOfT);
	}

	/**
	 * Deserializes from a Java input stream using Java serialization.
	 *
	 * @param stream
	 * @param <T>
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(InputStream stream) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(stream);
		T result = (T) ois.readObject();
		ois.close();
		return result;
	}

	/**
	 * Deserializes to a specific class using Java serialization.
	 *
	 * @param stream
	 * @param returnClass
	 * @param <T>
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static <T> T deserialize(InputStream stream, Class<? extends T> returnClass) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(stream);
		T result = returnClass.cast(ois.readObject());
		ois.close();
		return result;
	}

	/**
	 * Serializes an object using Java serialization to the specified output stream.
	 *
	 * @param obj
	 * @param output
	 * @throws IOException
	 */
	public static void serialize(Object obj, OutputStream output) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(output);
		oos.writeObject(obj);
		oos.flush();
		oos.close();
	}

	/**
	 * Deserializes a Vertx {@code JsonObject}, which is basically a {@link java.util.Map}, to the specified class.
	 *
	 * @param body
	 * @param returnClass
	 * @param <T>
	 * @return
	 */
	public static <T> T deserialize(JsonObject body, Class<? extends T> returnClass) {
		return body.mapTo(returnClass);
	}

}
