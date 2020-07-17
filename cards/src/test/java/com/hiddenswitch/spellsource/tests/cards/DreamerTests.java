package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.CardCatalogue;
import net.demilich.metastone.game.targeting.Zones;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Execution(ExecutionMode.CONCURRENT)
public class DreamerTests extends TestBase {

	@Test
	public void testCacklingSpinster() {
		runGym((context, player, opponent) -> {
			var shouldBeInDeck1 = receiveCard(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			var shouldBeInDeck2 = receiveCard(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			var shouldBeInHand1 = shuffleToDeck(context, player, CardCatalogue.getOneOneNeutralMinionCardId());
			var shouldBeInHand2 = shuffleToDeck(context, opponent, CardCatalogue.getOneOneNeutralMinionCardId());
			playCard(context, player, "minion_cackling_spinster");
			assertEquals(shouldBeInHand1, player.getHand().get(0));
			assertEquals(shouldBeInDeck1, player.getDeck().get(0));
			assertEquals(shouldBeInHand2, opponent.getHand().get(0));
			assertEquals(shouldBeInDeck2, opponent.getDeck().get(0));
		});
	}

	@Test
	public void testNightmareWraith() {
		runGym((context, player, opponent) -> {
			var shouldBeSummoned = receiveCard(context, opponent, "minion_test_2_3");
			var nightmareWraith = playMinionCard(context, player, "minion_nightmare_wraith");
			assertEquals(shouldBeSummoned.getBaseAttack(), nightmareWraith.getAttack());
			assertEquals(shouldBeSummoned.getBaseHp(), nightmareWraith.getMaxHp());
			assertNotEquals(Zones.HAND, shouldBeSummoned.getZone());
		});
	}

	@Test
	public void testFeastOfSouls() {
		runGym((context, player, opponent) -> {
			var BUFF = 3;
			var SURVIVORS = 4;
			var TOTAL_MINIONS = BUFF + SURVIVORS;
			for (var i = 0; i < BUFF; i++) {
				playMinionCard(context, player, 0, TOTAL_MINIONS);
			}
			for (var i = 0; i < SURVIVORS; i++) {
				playMinionCard(context, player, 0, TOTAL_MINIONS + 1);
			}
			playCard(context, player, "spell_feast_of_souls");
			assertEquals(player.getMinions().size(), SURVIVORS);
			for (var minion : player.getMinions()) {
				assertEquals(minion.getBaseAttack() + BUFF, minion.getAttack());
				assertEquals(minion.getBaseHp() - TOTAL_MINIONS + BUFF, minion.getHp());
			}
			var target = playMinionCard(context, player, 1, 1);
			destroy(context, target);
			for (var minion : player.getMinions()) {
				assertEquals(minion.getBaseAttack() + BUFF, minion.getAttack(), "enchantment should be gone");
				assertEquals(minion.getBaseHp() - TOTAL_MINIONS + BUFF, minion.getHp(), "enchantment should be gone");
			}
		});
	}
}
