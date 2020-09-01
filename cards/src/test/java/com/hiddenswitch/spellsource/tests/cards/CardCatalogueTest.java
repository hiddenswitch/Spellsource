package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.decks.DeckFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class CardCatalogueTest {
	@Test
	public void testCardCatalogueLoads() {
		CardCatalogue.loadCardsFromPackage();

		Assertions.assertTrue(CardCatalogue.getAll().size() > 1000);
		Assertions.assertTrue(CardCatalogue.getBaseClasses(DeckFormat.spellsource()).size() > 5);
	}

	@Test
	public void testAddOrReplaceCards() throws IOException {
		var json = "{\n" +
				"  \"name\": \"Exampler Test\",\n" +
				"  \"baseManaCost\": 1,\n" +
				"  \"type\": \"MINION\",\n" +
				"  \"heroClass\": \"ANY\",\n" +
				"  \"baseAttack\": 4,\n" +
				"  \"baseHp\": 4,\n" +
				"  \"rarity\": \"EPIC\",\n" +
				"  \"description\": \"Opener: Summon a 5/5 Skeleton for your opponent\",\n" +
				"  \"battlecry\": {\n" +
				"    \"targetSelection\": \"NONE\",\n" +
				"    \"spell\": {\n" +
				"      \"class\": \"SummonSpell\",\n" +
				"      \"card\": \"token_skeletal_enforcer\",\n" +
				"      \"targetPlayer\": \"OPPONENT\"\n" +
				"    }\n" +
				"  },\n" +
				"  \"attributes\": {\n" +
				"    \"BATTLECRY\": true\n" +
				"  },\n" +
				"  \"collectible\": false,\n" +
				"  \"set\": \"CUSTOM\",\n" +
				"  \"fileFormatVersion\": 1\n" +
				"}";
		assertEquals("minion_exampler_test", CardCatalogue.addOrReplaceCard(json));
		assertNotNull(CardCatalogue.getCardById("minion_exampler_test"));
		CardCatalogue.removeCard("minion_exampler_test");
		assertThrows(NullPointerException.class, () -> CardCatalogue.getCardById("minion_exampler_test"));
	}
}
