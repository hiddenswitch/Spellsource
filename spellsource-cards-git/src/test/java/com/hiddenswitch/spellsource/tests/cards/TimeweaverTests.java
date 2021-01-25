package com.hiddenswitch.spellsource.tests.cards;

import net.demilich.metastone.game.cards.Attribute;
import net.demilich.metastone.game.cards.Card;
import net.demilich.metastone.game.decks.GameDeck;
import net.demilich.metastone.game.entities.heroes.HeroClass;
import net.demilich.metastone.game.entities.minions.Minion;
import com.hiddenswitch.spellsource.rpc.Spellsource.ZonesMessage.Zones;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@Execution(ExecutionMode.CONCURRENT)
public class TimeweaverTests extends TestBase {

	@Test
	public void testShadowPuppetry() {
		runGym((context, player, opponent) -> {
			context.endTurn();
			Minion test1 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion test2 = playMinionCard(context, opponent, "minion_neutral_test");
			Minion test3 = playMinionCard(context, opponent, "minion_neutral_test");
			context.endTurn();
			test1.setAttack(0);
			test3.setAttack(0);
			playCard(context, player, "spell_shadow_puppetry", test2);
			assertTrue(test1.isDestroyed());
			assertTrue(test3.isDestroyed());
			assertFalse(test2.isDestroyed());
		});
	}

	@Test
	public void testTemporalEchoes() {
		runGym((context, player, opponent) -> {
			String CHOSEN = "minion_black_test";
			overrideDiscover(context, player, CHOSEN);
			putOnTopOfDeck(context, player, "minion_neutral_test");
			putOnTopOfDeck(context, player, "minion_neutral_test");
			putOnTopOfDeck(context, player, CHOSEN);
			playCard(context, player, "spell_temporal_echoes");
			assertEquals(player.getHand().get(0).getCardId(), CHOSEN);
			assertEquals(player.getDeck().size(), 2);
			assertTrue(player.getDeck().stream().allMatch(c -> c.getCardId().equals(CHOSEN)));
		});
	}

	@Test
	public void testAfterimage() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_divine_shield_test");
			playCard(context, player, "secret_afterimage");
			context.endTurn();
			Minion attacker = playMinionCard(context, opponent, "minion_test_rush");
			attack(context, opponent, attacker, target);
			assertEquals(player.getMinions().size(), 2);
			assertEquals(player.getMinions().get(1).getAttack(), 0);
			assertEquals(player.getMinions().get(1).getHp(), 1);
			assertTrue(player.getMinions().get(0).hasAttribute(Attribute.DIVINE_SHIELD));
			assertFalse(player.getMinions().get(1).hasAttribute(Attribute.DIVINE_SHIELD));
		});
	}

	@Test
	public void testFlickerAcrossTheVeil() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			target.setHp(1);
			playCard(context, player, "spell_flicker_across_the_veil", target);
			context.endTurn();
			assertEquals(player.getMinions().size(), 0);
			context.endTurn();
			assertEquals(player.getMinions().size(), 1);
			assertEquals(player.getMinions().get(0).getHp(), 2);
		});
	}

	@RepeatedTest(100)
	public void testBountifulPorzora() {
		runGym((context, player, opponent) -> {
			shuffleToDeck(context, player, "minion_neutral_test");
			shuffleToDeck(context, player, "minion_neutral_test");
			player.getHero().setHp(10);
			playCard(context, player, "token_bountiful_porzora");
			assertEquals(player.getDeck().size(), 0);
			assertEquals(player.getHero().getHp(), player.getHero().getMaxHp());
		}, GameDeck.EMPTY, GameDeck.EMPTY);

		runGym((context, player, opponent) -> {
			// We drew a card at the start of the turn
			assertEquals(player.getDeck().size(), 1);
			// The deck began with two cards. Determine which card was on top based on which one we drew
			assertEquals(player.getHand().size(), 1);
			boolean drewBlack = player.getHand().get(0).getCardId().equals("minion_black_test");
			playCard(context, player, "token_bountiful_porzora");
			assertEquals(player.getDeck().size(), 2);
			int blackIndex = drewBlack ? 1 : 0;
			int blueIndex = drewBlack ? 0 : 1;
			assertEquals(player.getDeck().get(blackIndex).getCardId(), "minion_black_test");
			assertEquals(player.getDeck().get(blueIndex).getCardId(), "minion_blue_test");
		}, new GameDeck(HeroClass.ANY, Arrays.asList("minion_black_test", "minion_blue_test")), new GameDeck(HeroClass.ANY, Arrays.asList("minion_black_test", "minion_blue_test")));
	}

	@Test
	public void testPowerEverlasting() {
		runGym((context, player, opponent) -> {
			Minion target = playMinionCard(context, player, "minion_neutral_test");
			Minion doesNotMatch = playMinionCard(context, player, "minion_blue_test");
			Minion destroyed = playMinionCard(context, player, "minion_neutral_test");
			destroy(context, destroyed);
			Card inHand = receiveCard(context, player, "minion_neutral_test");
			Card inDeck = receiveCard(context, player, "minion_neutral_test");
			playCard(context, player, "spell_power_everlasting", target);
			assertEquals(target.getAttack(), target.getBaseAttack() + 2);
			target = playMinionCard(context, player, inHand);
			assertEquals(target.getAttack(), target.getBaseAttack() + 2);
			context.endTurn();
			context.endTurn();
			assertEquals(inDeck.getZone(), Zones.HAND);
			target = playMinionCard(context, player, inDeck);
			assertEquals(target.getAttack(), target.getBaseAttack() + 2);
			assertEquals(doesNotMatch.getAttack(), doesNotMatch.getBaseAttack());
		});
	}

	@Test
	public void testMarbleSpellstoneUpgradesOnceAtEndOfTurn() {
		runGym((context, player, opponent) -> {
			receiveCard(context, player, "spell_lesser_marble_spellstone");
			context.endTurn();
			assertEquals(player.getHand().get(0).getCardId(), "spell_marble_spellstone");
			context.endTurn();
			assertEquals(player.getHand().get(0).getCardId(), "spell_marble_spellstone");
			player.setMana(0);
			context.endTurn();
			assertEquals(player.getHand().get(0).getCardId(), "spell_marble_spellstone");
			context.endTurn();
			context.endTurn();
			assertEquals(player.getHand().get(0).getCardId(), "spell_greater_marble_spellstone");
		});
	}

	@Test
	public void testDelayFate() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "secret_delay_fate");
			context.endTurn();
			int initialMana = opponent.getMana();
			playCard(context, opponent, "spell_test_summon_tokens");
			assertTrue(opponent.getMinions().isEmpty());
			assertEquals(opponent.getMana(), initialMana);

		});
	}

	@Test
	public void testMysteriousQuestgiver() {
		runGym((context, player, opponent) -> {
			playCard(context, player, "minion_mysterious_questgiver");
			assertEquals(3, player.getHand().size());
		}, "TIME", "TIME");
	}
}
