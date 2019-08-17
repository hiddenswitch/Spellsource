package com.hiddenswitch.spellsource;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.entities.minions.Minion;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class PuppeteerTests extends TestBase {
	@Test
	public void testKeywordBuffsIncorrectlyRetained() {
		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_deathrattle");
			playCard(context, player, "spell_morbid_mockery");
			Minion aftermathMinion = playMinionCard(context, player, player.getHand().get(0));
			assertTrue(aftermathMinion.hasAttribute(Attribute.TAUNT));
			playCard(context, player, "spell_test_add_to_hand", aftermathMinion);
			Minion shouldNotHaveTaunt = playMinionCard(context, player, player.getHand().get(0));
			assertFalse(shouldNotHaveTaunt.hasAttribute(Attribute.TAUNT));
			playCard(context, player, "spell_test_give_away", aftermathMinion);
			assertEquals(aftermathMinion.getOwner(), opponent.getId());
			playCard(context, player, "spell_test_return_to_hand", aftermathMinion);
			context.endTurn();
			aftermathMinion = playMinionCard(context, opponent, opponent.getHand().get(0));
			assertFalse(aftermathMinion.hasAttribute(Attribute.TAUNT));
		});

		runGym((context, player, opponent) -> {
			putOnTopOfDeck(context, player, "minion_test_deathrattle");
			playCard(context, player, "spell_morbid_mockery");
			Minion aftermathMinion = playMinionCard(context, player, player.getHand().get(0));
			assertTrue(aftermathMinion.hasAttribute(Attribute.TAUNT));
			playCard(context, player, "spell_test_give_away", aftermathMinion);
			assertEquals(aftermathMinion.getOwner(), opponent.getId());
			playCard(context, player, "spell_test_return_to_hand", aftermathMinion);
			context.endTurn();
			aftermathMinion = playMinionCard(context, opponent, opponent.getHand().get(0));
			assertFalse(aftermathMinion.hasAttribute(Attribute.TAUNT));
		});
	}
}
