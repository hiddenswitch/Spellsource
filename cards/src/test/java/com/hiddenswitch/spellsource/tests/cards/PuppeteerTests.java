package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.entities.minions.Minion;
import net.demilich.metastone.game.targeting.Zones;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class PuppeteerTests extends TestBase {

	@Test
	public void testBellringerJuriso() {
		runGym((context, player, opponent) -> {
			for (String cardId : new String[]{"minion_test_deathrattle", "minion_test_deathrattle_2", "minion_test_deathrattle_3"}) {
				Minion inGraveyardAftermath = playMinionCard(context, player, cardId);
				destroy(context, inGraveyardAftermath);
			}
			// Also destroy the extra minion on the board from the minion_test_deathrattle_2
			destroy(context, player.getMinions().get(0));
			assertEquals(player.getMinions().size(), 0);

			Card shouldBeDrawn = shuffleToDeck(context, player, "spell_lunstone");

			// control deathrattle
			playCard(context, player, "minion_test_deathrattle");
			int opponentHp = opponent.getHero().getHp();
			playCard(context, player, "minion_bellringer_juriso");
			assertEquals(shouldBeDrawn.getZone(), Zones.HAND, "Should have triggered a minion_test_deathrattle");
			assertEquals(opponent.getHero().getHp(), opponentHp - 1, "should have triggered a minion_test_deathrattle_3");
			assertEquals(player.getMinions().size(), 3);
			assertEquals(player.getMinions().get(2).getSourceCard().getCardId(), "minion_neutral_test", "should have triggered a minion_test_deathrattle_2");
		});
	}

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
