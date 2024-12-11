package com.hiddenswitch.protos;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerFactory;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.vertx.core.json.jackson.DatabindCodec;
import org.curioswitch.common.protobuf.json.MessageMarshaller;
import org.curioswitch.common.protobuf.json.MessageMarshallerModule;

import java.util.Collection;
import java.util.stream.Stream;

public class Serialization {
	private static final MessageMarshaller marshaller = getMarshaller();
	private static final ObjectMapper yaml = new ObjectMapper(new YAMLFactory(), new DefaultSerializerProvider.Impl(), new DefaultDeserializationContext.Impl(new BeanDeserializerFactory(new DeserializerFactoryConfig())))
			.registerModule(MessageMarshallerModule.of(marshaller))
			.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);


	public static void configureSerialization() {
		configure(DatabindCodec.mapper());
	}

	public static ObjectMapper yamlMapper() {
		return yaml;
	}

	public static ObjectMapper configure(ObjectMapper mapper) {
		return mapper.registerModule(MessageMarshallerModule.of(marshaller))
				.setAnnotationIntrospector(new JacksonAnnotationIntrospector())
				.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
				.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
	}

	public static MessageMarshaller getMarshaller() {
		return getMarshallerBuilder().build();
	}

	public static MessageMarshaller.Builder getMarshallerBuilder() {
		var builder = MessageMarshaller.builder();
		Stream.of(com.hiddenswitch.framework.rpc.DefaultInstances.DEFAULT_INSTANCES,
						com.hiddenswitch.spellsource.rpc.DefaultInstances.DEFAULT_INSTANCES)
				.flatMap(Collection::stream)
				.forEach(builder::register);
		builder.preservingProtoFieldNames(true);
		return builder;
	}

	public static class JavaCamelCaseNamingStrategy extends PropertyNamingStrategies.NamingBase {
		@Override
		public String translate(String input) {
			return input;
		}
	}
}
