package com.hiddenswitch.spellsource.util;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.io.*;
import java.util.Base64;

public class Serialization {
//	private static ThreadLocal<FSTConfiguration> fsts = ThreadLocal.withInitial(FSTConfiguration::createDefaultConfiguration);

	public static String serialize(Object object) {
		return Json.encode(object);
	}

	public static byte[] serializeBytes(Object object) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		serialize(object, bos);
		return bos.toByteArray();
	}

	public static <T> T deserialize(String json, Class<T> classOfT) {
		return Json.decodeValue(json, classOfT);
	}


	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] buffer) throws IOException, ClassNotFoundException {
		return deserialize(new ByteArrayInputStream(buffer));
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(InputStream stream) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(stream);
		T result = (T) ois.readObject();
		ois.close();
		return result;
	}

	public static <T> T deserialize(InputStream stream, Class<? extends T> returnClass) throws IOException, ClassNotFoundException {
		ObjectInputStream ois = new ObjectInputStream(stream);
		T result = returnClass.cast(ois.readObject());
		ois.close();
		return result;
	}

	public static void serialize(Object obj, OutputStream output) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(output);
		oos.writeObject(obj);
		oos.flush();
		oos.close();
	}

	/*
	@SuppressWarnings("unchecked")
	public static <T> T deserialize(InputStream stream) throws IOException, ClassNotFoundException {
		FSTObjectInput ois = fsts.get().getObjectInput(stream);
		T result = (T) ois.readObject();
		stream.close();
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(InputStream stream, Class<? extends T> returnClass) throws Exception {
		FSTObjectInput ois = fsts.get().getObjectInput(stream);
		T result = (T) ois.readObject(returnClass);
		return result;
	}

	public static void serialize(Object obj, OutputStream stream) throws IOException {
		FSTObjectOutput oos = fsts.get().getObjectOutput(stream);
		oos.writeObject(obj, obj.getClass());
		oos.flush();
	}
	*/

	public static <T> T deserialize(JsonObject body, Class<? extends T> returnClass) {
		return body.mapTo(returnClass);
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserializeBase64(String serializedBase64) {
		byte bytes[] = Base64.getDecoder().decode(serializedBase64);

		try {
			return /*(T) fsts.get().getObjectInput(bytes).readObject()*/ deserialize(bytes);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> String serializeBase64(T src) {
		try {
			return Base64.getEncoder().encodeToString(/*fsts.get().asByteArray(src)*/serializeBytes(src));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
