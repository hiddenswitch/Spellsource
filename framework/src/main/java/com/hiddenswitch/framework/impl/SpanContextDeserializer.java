package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;

import java.io.IOException;

public class SpanContextDeserializer extends StdDeserializer<SpanContext> {

	protected SpanContextDeserializer() {
		super(SpanContext.class);
	}

	@Override
	public SpanContext deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = ctxt.readValue(p, JsonNode.class);
		return GlobalTracer.get().extract(Format.Builtin.TEXT_MAP, new JsonCarrier(node));
	}
}

