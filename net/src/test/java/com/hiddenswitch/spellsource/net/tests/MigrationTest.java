package com.hiddenswitch.spellsource.net.tests;

import com.hiddenswitch.spellsource.net.Decks;
import com.hiddenswitch.spellsource.net.Inventory;
import com.hiddenswitch.spellsource.net.Logic;
import com.hiddenswitch.spellsource.net.impl.util.CollectionRecord;
import com.hiddenswitch.spellsource.net.models.CollectionTypes;
import com.hiddenswitch.spellsource.net.models.InitializeUserRequest;
import com.hiddenswitch.spellsource.net.tests.impl.SpellsourceTestBase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.unit.TestContext;
import org.junit.Ignore;
import org.junit.Test;

import static com.hiddenswitch.spellsource.net.impl.Mongo.mongo;
import static com.hiddenswitch.spellsource.net.impl.QuickJson.json;

public class MigrationTest extends SpellsourceTestBase {

	@Test
	@Ignore("migration completed")
	public void testMigrateAllDecks(TestContext testContext) {
		sync(() -> {
			var account = createRandomAccount();
			Logic.initializeUser(InitializeUserRequest.create(account.getUserId()));
			// Remove the validation record to test adding it in
			mongo().updateCollectionWithOptions(Inventory.COLLECTIONS, json(), json("$unset", json(CollectionRecord.VALIDATION_REPORT, null)), new UpdateOptions().setMulti(true));
			/**
			 * {"$unset": {"fieldName": 1}}
			 */
			var decks = mongo().find(Inventory.COLLECTIONS, json(CollectionRecord.TYPE, CollectionTypes.DECK.toString()));
			testContext.assertTrue(decks.size() > 0, "should find decks");
			for (JsonObject deck : decks) {
				testContext.assertNull(deck.getJsonObject(CollectionRecord.VALIDATION_REPORT), "should not have validation report because we removed it");
			}
			Decks.validateAllDecks();
			decks = mongo().find(Inventory.COLLECTIONS, json(CollectionRecord.TYPE, CollectionTypes.DECK.toString()));
			for (JsonObject deck : decks) {
				testContext.assertNotNull(deck.getJsonObject(CollectionRecord.VALIDATION_REPORT), "SHOULD have validation report because we removed it");
			}
			var decksDeserialized = mongo().find(Inventory.COLLECTIONS, json(CollectionRecord.TYPE, CollectionTypes.DECK.toString()), CollectionRecord.class);
			for (var deck : decksDeserialized) {
				testContext.assertNotNull(deck.getValidationReport(), "SHOULD have validation report because we removed it");
			}
		}, testContext);
	}
}
