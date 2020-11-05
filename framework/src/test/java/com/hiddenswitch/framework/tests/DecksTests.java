package com.hiddenswitch.framework.tests;

import com.google.protobuf.Empty;
import com.hiddenswitch.framework.Client;
import com.hiddenswitch.framework.Legacy;
import com.hiddenswitch.framework.tests.impl.FrameworkTestBase;
import com.hiddenswitch.spellsource.rpc.*;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import net.demilich.metastone.game.cards.Card;
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
				.onSuccess(decksPutResponse -> {
					testContext.verify(() -> {
						assertEquals(30, decksPutResponse.getCollection().getInventoryCount(), "should have added cards");
					});
				})
				.onComplete(client::close)
				.onComplete(testContext.completing());
	}

	@Test
	public void testCreateManyDecks(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);
		client.createAndLogin()
				.compose(ignored -> CompositeFuture.all(IntStream.range(0, 100).mapToObj(i -> createRandomDeck(client)).collect(Collectors.toList())))
				.onComplete(client::close)
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
					return service.decksUpdate(DecksUpdateRequest.newBuilder()
							.setDeckId(decksPutResponse.getDeckId())
							.setUpdateCommand(DecksUpdateCommand.newBuilder()
									.addPullAllInventoryIds(toReplace)
									.setPushCardIds(DecksUpdateCommand.PushCardIdsMessage.newBuilder()
											.addEach(replacement)
											.build())
									.build()).build())
							.onSuccess(decksGetResponse -> {
								testContext.verify(() -> {
									assertTrue(decksGetResponse.getCollection().getInventoryList().stream().anyMatch(cr -> cr.getEntity().getCardId().equals(replacement)), "should have added lunstone");
									assertTrue(decksGetResponse.getCollection().getInventoryList().stream().noneMatch(cr -> cr.getId().equals(toReplace)), "should have removed by id");
								});
							});
				})
				.onComplete(client::close)
				.onComplete(testContext.completing());
	}


	@Test
	public void testUpdateDecksWithCardIds(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);
		client.createAndLogin()
				.compose(ignored1 -> {
					var service = client.legacy();

					return service.decksPut(DecksPutRequest.newBuilder()
							.setName("Test Deck 2")
							.setHeroClass(HeroClass.TEST)
							.setFormat(DeckFormat.spellsource().getName()).build())
							.compose(decksPutResponse -> service.decksUpdate(DecksUpdateRequest.newBuilder()
									.setDeckId(decksPutResponse.getCollection().getId())
									.setUpdateCommand(DecksUpdateCommand.newBuilder()
											.setPushCardIds(DecksUpdateCommand.PushCardIdsMessage.newBuilder()
													.addEach("spell_test_summon_tokens")
													.addEach("spell_test_summon_tokens")
													.addEach("spell_test_summon_tokens")
													.addEach("minion_test_3_2")
													.build())
											.build())
									.build()))
							.onSuccess(decksGetResponse -> {
								testContext.verify(() -> {
									assertEquals(4L, decksGetResponse.getCollection().getInventoryList().stream().map(CardRecord::getId).distinct().count());
								});
							})
							.compose(decksGetResponse -> service.decksUpdate(DecksUpdateRequest.newBuilder()
									.setDeckId(decksGetResponse.getCollection().getId())
									.setUpdateCommand(DecksUpdateCommand.newBuilder()
											.addPullAllCardIds("spell_test_summon_tokens")
											.addPullAllCardIds("spell_test_summon_tokens")
											.addPullAllCardIds("minion_test_3_2")
											.build())
									.build()))
							.onSuccess(decksGetResponse -> {
								testContext.verify(() -> {
									assertEquals(1, decksGetResponse.getCollection().getInventoryCount(), "should only remove two of the three copies of spell test summon tokens");
									assertEquals("spell_test_summon_tokens", decksGetResponse.getCollection().getInventory(0).getEntity().getCardId());
								});
							});
				})
				.onComplete(client::close)
				.onComplete(testContext.completing());
	}

	@Test
	public void testDeleteDecks(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);
		client.createAndLogin()
				.compose(ignored -> createRandomDeck(client))
				.compose(decksPutResponse -> {
					var service = client.legacy();
					return service
							.decksGetAll(Empty.getDefaultInstance())
							.onSuccess(decksGetAll -> {
								testContext.verify(() -> {
									assertEquals(Legacy.getPremadeDecks().size() + 1, decksGetAll.getDecksCount(), "should have only premades deck + newly created deck");
								});
							})
							.compose(ignored -> service.decksDelete(DecksDeleteRequest.newBuilder()
									.setDeckId(decksPutResponse.getDeckId()).build()))
							.compose(ignored -> service.decksGetAll(Empty.getDefaultInstance()))
							.onSuccess(decksGetAll -> {
								testContext.verify(() -> {
									assertEquals(Legacy.getPremadeDecks().size(), decksGetAll.getDecksCount(), "should have deleted deck");
								});
							});
				})
				.onComplete(client::close)
				.onComplete(testContext.completing());
	}

	/*
	@Test
	public void testDeletePremadeDecks(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client1 = new Client(vertx, webClient);
		var client2 = new Client(vertx, webClient);
	}*/

	@Test
	public void testGetPremadeDecks(WebClient webClient, Vertx vertx, VertxTestContext testContext) {
		var client = new Client(vertx, webClient);
		client.createAndLogin()
				.compose(ignored -> {
					var service = client.legacy();
					return service.decksGetAll(Empty.getDefaultInstance());
				})
				.onSuccess(decksGetAll -> {
					testContext.verify(() -> {
						assertEquals(Legacy.getPremadeDecks().size(), decksGetAll.getDecksCount(), "should have only premade decks");
					});
				})
				.onComplete(client::close)
				.onComplete(testContext.completing());
	}

	@NotNull
	private Future<DecksPutResponse> createRandomDeck(Client client) {
		var service = client.legacy();
		var randomDeck = Deck.randomDeck();
		return service.decksPut(DecksPutRequest.newBuilder()
				.setName("Test Deck")
				.setFormat(randomDeck.getFormat().getName())
				.setHeroClass(randomDeck.getHeroClass())
				.addAllCardIds(randomDeck.getCards().stream().map(Card::getCardId).collect(Collectors.toList()))
				.build());
	}
}