package com.hiddenswitch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.hiddenswitch.spellsource.micro.Application;
import com.hiddenswitch.spellsource.micro.BotsService;
import com.hiddenswitch.spellsource.micro.Payload;
import com.hiddenswitch.spellsource.micro.Response;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import net.demilich.metastone.game.cards.CardCatalogue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest(application = Application.class)
public class BotsServiceTest {
	static {
		CardCatalogue.loadCardsFromPackage();
	}

	@Inject
	private EmbeddedServer server;

	@Inject
	@Client("/")
	private HttpClient client;

	@Test
	public void testBotRequest() throws IOException {
		ClassPathResourceLoader loader = new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
		Optional<URL> resource = loader.getResource("classpath:testrequest.json");
		var requestBody = Resources.toString(resource.get(), Charset.defaultCharset());
		var mapper = new ObjectMapper();
		var payload = mapper.readValue(requestBody, Payload.class);
		Response res = client.toBlocking()
				.retrieve(HttpRequest.POST("/bots/request", payload)
						.contentType(MediaType.APPLICATION_JSON), Response.class);
		assertTrue(res.getActions().size() > 0);
	}
}
