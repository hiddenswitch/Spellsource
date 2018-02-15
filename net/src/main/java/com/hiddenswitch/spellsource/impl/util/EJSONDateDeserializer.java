package com.hiddenswitch.spellsource.impl.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

public class EJSONDateDeserializer extends StdDeserializer<Date> {
	protected EJSONDateDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		ObjectNode dateEJSON = p.readValueAsTree();
		if (dateEJSON == null) {
			return null;
		}
		String date = dateEJSON.get("$date").asText();
		try {
			return StdDateFormat.getInstance().parse(date);
		} catch (ParseException e) {
			ctxt.handleWeirdStringValue(Date.class, date, "Invalid date specified");
			return null;
		}
	}
}
