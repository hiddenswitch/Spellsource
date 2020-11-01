package com.hiddenswitch.framework.tests;

import com.hiddenswitch.framework.Client;
import com.hiddenswitch.spellsource.rpc.*;
import io.vertx.core.*;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.Deck;
import net.demilich.metastone.game.decks.DeckFormat;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DecksTests extends FrameworkTestBase {

	@Test
	public void testCreateDeck(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);
		client.createAndLogin()
				.compose(ignored -> createRandomDeck(client))
				.compose(decksPutResponse -> {
					testContext.verify(() -> {
						assertEquals(30, decksPutResponse.getCollection().getInventoryCount(), "should have added cards");
					});

					return Future.succeededFuture();
				})
				.onComplete(testContext.completing());
	}

	@Test
	public void testCreateManyDecks(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);
		client.createAndLogin()
				.compose(ignored -> CompositeFuture.all(IntStream.range(0, 100).mapToObj(i -> createRandomDeck(client)).collect(Collectors.toList())))
				.onComplete(testContext.completing());
	}

	@Test
	public void testUpdateDecks(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);

		client.createAndLogin()
				.compose(ignored -> createRandomDeck(client))
				.compose(decksPutResponse -> {
					var replacement = "spell_lunstone";
					var toReplace = decksPutResponse.getCollection().getInventory(10).getId();
					var service = client.legacy();
					var promise = Promise.<DecksGetResponse>promise();
					service.decksUpdate(DecksUpdateRequest.newBuilder()
							.setDeckId(decksPutResponse.getDeckId())
							.setUpdateCommand(DecksUpdateCommand.newBuilder()
									.addPullAllInventoryIds(toReplace)
									.setPushCardIds(DecksUpdateCommand.PushCardIdsMessage.newBuilder()
											.addEach(replacement)
											.build())
									.build()).build(), promise);
					return promise.future().onSuccess(decksGetResponse -> {
						testContext.verify(() -> {
							assertTrue(decksGetResponse.getCollection().getInventoryList().stream().anyMatch(cr -> cr.getEntity().getCardId().equals(replacement)), "should have added lunstone");
							assertTrue(decksGetResponse.getCollection().getInventoryList().stream().noneMatch(cr -> cr.getId().equals(toReplace)), "should have removed by id");
						});
					});
				})
				.onComplete(testContext.completing());
	}

	/*
	@Test
	public void testUpdateDecksWithCardIds(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);
		client.createAndLogin()
				.compose(ignored -> {
					var service = client.legacy();

					var decksPutResponsePromise = Promise.<DecksPutResponse>promise();
					service.decksPut(DecksPutRequest.newBuilder()
							.setName("Test Deck 2")
							.setHeroClass(HeroClass.TEST)
							.setFormat(DeckFormat.spellsource().getName()).build(), decksPutResponsePromise);


				})
				.onComplete(testContext.completing());
	}*/

	@NotNull
	private Future<DecksPutResponse> createRandomDeck(Client client) {
		var service = client.legacy();
		var promise = Promise.<DecksPutResponse>promise();
		var randomDeck = Deck.randomDeck();
		service.decksPut(DecksPutRequest.newBuilder()
				.setName("Test Deck")
				.setFormat(randomDeck.getFormat().getName())
				.setHeroClass(randomDeck.getHeroClass())
				.addAllCardIds(randomDeck.getCards().stream().map(Card::getCardId).collect(Collectors.toList()))
				.build(), promise);
		return promise.future();
	}
}
