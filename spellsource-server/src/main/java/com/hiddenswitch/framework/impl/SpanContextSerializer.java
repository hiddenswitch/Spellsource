package com.hiddenswitch.framework.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.opentracing.SpanContext;
import io.opentracing.propagation.Format;
import io.opentracing.util.GlobalTracer;

import java.io.IOException;

public class SpanContextSerializer extends StdSerializer<SpanContext> {

	public SpanContextSerializer() {
		super(SpanContext.class);
	}

	@Override
	public void serialize(SpanContext value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		GlobalTracer.get().inject(value, Format.Builtin.TEXT_MAP, new JsonCarrier(gen));
		gen.writeEndObject();
	}
}

