package com.hiddenswitch.framework.tests;

import net.demilich.metastone.game.cards.catalogues.ClasspathCardCatalogue;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.tests.util.TestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class RogueCardTests extends TestBase {

	@BeforeAll
	public static void loadCardCatalogue() {
		ClasspathCardCatalogue.INSTANCE.loadCardsFromPackage();
	}

	@Test
	public void testEquipment() {
		runGym((context, player, opponent) -> {
			var guy = receiveCard(context, player, "minion_test_deal_1");

			playCard(context, player, guy, opponent.getHero());

			assertEquals(opponent.getHero().getMaxHp() - 2, opponent.getHero().getHp());

			assertFalse(player.getDeck().stream().anyMatch(card -> card.getCardId().equals("equipment_double_openers")));
			assertFalse(player.getHand().stream().anyMatch(card -> card.getCardId().equals("equipment_double_openers")));

		}, new GameDeck(ClasspathCardCatalogue.INSTANCE, HeroClass.ANY, Arrays.asList("equipment_double_openers", 
			"minion_test_deal_1", "minion_test_deal_1", "minion_test_deal_1", "minion_test_deal_1", "minion_test_deal_1")),
			new GameDeck(ClasspathCardCatalogue.INSTANCE, HeroClass.ANY, Arrays.asList("minion_test_deal_1", 
				"minion_test_deal_1", "minion_test_deal_1", "minion_test_deal_1", "minion_test_deal_1")));
	}

	@Test
	public void testLevel() {
		runGym((context, player, opponent) -> {
			assertEquals(15, player.getHero().getHp());

		}, new GameDeck(ClasspathCardCatalogue.INSTANCE, HeroClass.ANY, Arrays.asList("level_1", "minion_test_deal_1", 
			"minion_test_deal_1", "minion_test_deal_1", "minion_test_deal_1", "minion_test_deal_1")),
			new GameDeck(ClasspathCardCatalogue.INSTANCE, HeroClass.ANY, Arrays.asList("minion_test_deal_1", 
				"minion_test_deal_1", "minion_test_deal_1", "minion_test_deal_1", "minion_test_deal_1")));
	}


}
