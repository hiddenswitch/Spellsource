package com.hiddenswitch.spellsource.impl.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.io.IOException;
import java.util.Date;

public class EJSONDateSerializer extends StdSerializer<Date> {
	public EJSONDateSerializer() {
		this(null);
	}

	protected EJSONDateSerializer(Class<Date> t) {
		super(t);
	}

	@Override
	public void serialize(Date value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (value == null) {
			gen.writeNull();
			return;
		}
		gen.writeStartObject();
		gen.writeStringField("$date", StdDateFormat.getInstance().format(value));
		gen.writeEndObject();
	}
}
