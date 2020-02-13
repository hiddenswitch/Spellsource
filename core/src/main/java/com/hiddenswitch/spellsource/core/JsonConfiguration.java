package com.hiddenswitch.spellsource.core;

import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import io.vertx.core.json.jackson.DatabindCodec;

public class JsonConfiguration {
	public static void configureJson() {
		DatabindCodec.mapper().setAnnotationIntrospector(new JacksonAnnotationIntrospector());
	}
}
