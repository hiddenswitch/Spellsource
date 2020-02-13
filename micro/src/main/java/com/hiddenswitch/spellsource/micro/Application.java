package com.hiddenswitch.spellsource.micro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.hiddenswitch.spellsource.core.JsonConfiguration;
import io.micronaut.runtime.Micronaut;

public class Application {

	public static void main(String[] args) {
		JsonConfiguration.configureJson();
		var application = Micronaut.run(Application.class);
		var objectMapper = application.getBean(ObjectMapper.class);
		objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
	}
}