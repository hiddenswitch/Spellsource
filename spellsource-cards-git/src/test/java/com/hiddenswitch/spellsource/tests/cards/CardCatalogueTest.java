package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
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

		Assertions.assertTrue(ClasspathCardCatalogue.INSTANCE.getAll().size() > 1000);
		Assertions.assertTrue(ClasspathCardCatalogue.INSTANCE.getBaseClasses(ClasspathCardCatalogue.INSTANCE.spellsource()).size() > 5);
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
		assertEquals("minion_exampler_test", ClasspathCardCatalogue.INSTANCE.addOrReplaceCard(json));
		assertNotNull(ClasspathCardCatalogue.INSTANCE.getCardById("minion_exampler_test"));
		ClasspathCardCatalogue.INSTANCE.removeCard("minion_exampler_test");
		assertThrows(NullPointerException.class, () -> ClasspathCardCatalogue.INSTANCE.getCardById("minion_exampler_test"));
	}
}
