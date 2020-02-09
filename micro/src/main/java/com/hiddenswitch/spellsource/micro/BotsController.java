package com.hiddenswitch.spellsource.micro;

import com.fasterxml.jackson.core.JsonParseException;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.reactivex.Single;
import net.demilich.metastone.game.cards.CardCatalogue;

import javax.inject.Inject;

@Controller("/bots")
public class BotsController {
	static {
		// Forces the card catalogue to be initialized at compile time, which is the only time when we need access to it.
		// This includes all the classes that make up the "Desc" hierarchy of reflection-based card JSON parsing.
		CardCatalogue.loadCardsFromFilesystemDirectories("../cards/src/main/resources/cards", "../game/src/main/resources/cards");
	}

	@Inject
	BotsService botsService;

	@Post("/request")
	public Single<HttpResponse<Response>> request(@Body Single<Payload> payload) {
		return payload
				.map(each -> {
					Response response = new Response();
					response.setActions(botsService.request(each));
					return response;
				})
				.map(HttpResponse::created);
	}

	@Error
	public HttpResponse<JsonError> jsonError(HttpRequest request, JsonParseException jsonParseException) {
		JsonError error = new JsonError(jsonParseException.getMessage())
				.link(Link.SELF, Link.of(request.getUri()));

		return HttpResponse.<JsonError>status(HttpStatus.BAD_REQUEST, "invalid JSON")
				.body(error);
	}
}
