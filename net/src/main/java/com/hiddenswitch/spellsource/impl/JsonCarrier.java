package com.hiddenswitch.spellsource.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import io.jaegertracing.spi.Codec;
import io.opentracing.propagation.Binary;
import io.opentracing.propagation.TextMap;
import io.opentracing.propagation.TextMapInject;
import io.vertx.core.buffer.Buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;

public class JsonCarrier implements TextMap {
	private JsonGenerator gen;
	private JsonNode node;

	public JsonCarrier(JsonGenerator gen) {
		this.gen = gen;
	}

	public JsonCarrier(JsonNode node) {
		this.node = node;
	}

	@Override
	public Iterator<Map.Entry<String, String>> iterator() {
		return Iterators.transform(node.fields(), entry -> Maps.immutableEntry(entry.getKey(), entry.getValue().asText()));
	}

	@Override
	public void put(String key, String value) {
		try {
			gen.writeFieldName(key);
			gen.writeObject(value);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}

