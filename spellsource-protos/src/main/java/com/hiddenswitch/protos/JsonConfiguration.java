package com.hiddenswitch.protos;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import io.vertx.core.json.jackson.DatabindCodec;

import static io.vertx.core.json.jackson.DatabindCodec.mapper;

public class JsonConfiguration {
	public static void configureJson() {
		DatabindCodec.mapper().setAnnotationIntrospector(new JacksonAnnotationIntrospector());
		DatabindCodec.mapper().registerModule(new ProtobufModule())
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
	}
}
