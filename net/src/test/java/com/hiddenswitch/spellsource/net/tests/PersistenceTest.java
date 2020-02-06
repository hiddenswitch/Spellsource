package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.client.ApiException;
import com.hiddenswitch.spellsource.client.models.DecksPutRequest;
import com.hiddenswitch.spellsource.client.models.DecksPutResponse;
import com.hiddenswitch.spellsource.net.Inventory;
import com.hiddenswitch.spellsource.net.Spellsource;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import com.hiddenswitch.spellsource.net.impl.Mongo;
import com.hiddenswitch.spellsource.net.impl.QuickJson;
import com.hiddenswitch.spellsource.net.tests.impl.UnityClient;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import net.demilich.metastone.game.events.GameEventType;
import net.demilich.metastone.game.targeting.EntityReference;
import net.demilich.metastone.game.cards.Attribute;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PersistenceTest extends SpellsourceTestBase {

	@Test
	@Ignore
	public void testMinionatePersistenceApi(TestContext context) {
		ConcurrentLinkedQueue<Long> queue = new ConcurrentLinkedQueue<Long>();
		Vertx vertx = contextRule.vertx();
		Spellsource.spellsource().persistAttribute("reserved-attribute-1", GameEventType.TURN_END, Attribute.RESERVED_INTEGER_4, persistenceContext -> {
			// Save the turn number to this yogg attribute
			long updated = persistenceContext.update(EntityReference.ALL_MINIONS, persistenceContext.event().getGameContext().getTurn());
			queue.add(updated);
		});


		// Start a game and assert that there are entities with all random yogg
		vertx.executeBlocking(fut -> {
			UnityClient client = new UnityClient(context);
			client.createUserAccount();
			// The user needs a deck of persistent effect cards
			DecksPutResponse decksPutResponse;
			try {

				decksPutResponse = client.getApi().decksPut(new DecksPutRequest()
						.deckList("### Persistence Test Deck\n" +
								"Hero Class: Violet\n" +
								"30x Persistence Test Minion"));
			} catch (ApiException e) {
				fut.fail(e.getMessage());
				return;
			}
			client.matchmakeQuickPlay(decksPutResponse.getDeckId());
			client.waitUntilDone();
			assertTrue(client.getTurnsPlayed() > 0);
			assertTrue(client.isGameOver());
			fut.complete();
		}, context.asyncAssertSuccess(also -> {
			context.assertTrue(queue.stream().anyMatch(l -> l > 0L), "Any number of the entities updated was greater than zero.");
			Mongo.mongo().client().count(Inventory.INVENTORY,
					QuickJson.json("facts." + Attribute.RESERVED_INTEGER_4.toKeyCase(), QuickJson.json("$exists", true)),
					context.asyncAssertSuccess(count -> {
						context.assertTrue(count > 0L, "There is at least one inventory item that has the attribute that we configured to listen for.");
					}));
		}));
	}

}
